package grails.gorm.tests

import grails.persistence.Entity

import org.grails.datastore.gorm.proxy.GroovyProxyFactory
import org.grails.datastore.gorm.query.transform.ApplyDetachedCriteriaTransform
import grails.gorm.DetachedCriteria

/**
 * @author graemerocher
 */
@ApplyDetachedCriteriaTransform
class UpdateWithProxyPresentSpec extends GormDatastoreSpec {

    void "Test update entity with association proxies"() {
        given:
            session.mappingContext.setProxyFactory(new GroovyProxyFactory())
            def person = new Person(firstName:"Bob", lastName:"Builder")
            def petType = new PetType(name:"snake")
            def pet = new Pet(name:"Fred", type:petType, owner:person)
            person.addToPets(pet)
            person.save(flush:true)
            session.clear()

        when:
            person = Person.get(person.id)
            person.firstName = "changed"
            person.save(flush:true)
            session.clear()
            person = Person.get(person.id)
            def personPet = person.pets.iterator().next()

        then:
            person.firstName == "changed"
            personPet.name == "Fred"
            personPet.id == pet.id
            personPet.owner.id == person.id
            personPet.type.name == 'snake'
            personPet.type.id == petType.id
    }
}

@Entity
class Pet implements Serializable {
    Long id
    Long version
    String name
    Date birthDate = new Date()
    PetType type = new PetType(name:"Unknown")
    Person owner
    Integer age

    static mapping = {
        name index:true
    }

    static constraints = {
        owner nullable:true
        age nullable: true
    }
}

@Entity
@ApplyDetachedCriteriaTransform
class Person implements Serializable, Comparable<Person> {
    static simpsons = where {
         lastName == "Simpson"
    }

    Long id
    Long version
    String firstName
    String lastName
    Integer age = 0
    Set<Pet> pets = [] as Set
    static hasMany = [pets:Pet]


    static mapping = {
        firstName index:true
        lastName index:true
        age index:true
    }

    @Override
    int compareTo(Person t) {
        age <=> t.age
    }
}

@Entity
class PetType implements Serializable {
    Long id
    Long version
    String name

    static belongsTo = Pet
}
