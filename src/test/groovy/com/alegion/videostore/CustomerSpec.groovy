package com.alegion.videostore

import com.github.javafaker.Faker
import spock.lang.Specification
import spock.lang.Unroll

@Unroll
class CustomerSpec extends Specification {

    Faker faker = Faker.instance()
    Customer customer

    void setup() {
        customer = new Customer(faker.funnyName().name())
    }

    def "it should generate statement for one day rentals"() throws Exception {
        given:
        Rental rental = getRental(getMovie(priceCode), daysRented)
        customer.addRental(rental)

        expect:
        customer.statement() == """Rental Record for ${customer.name}
\t${rental.movie.title}\t${charge}
Amount owed is ${charge}
You earned ${frequentRenterPoints} frequent renter points"""

        where:
        priceCode         | daysRented | charge | frequentRenterPoints
        Movie.REGULAR     | 1          | 2.0    | 1
        Movie.NEW_RELEASE | 1          | 3.0    | 1
        Movie.CHILDRENS   | 1          | 1.5    | 1
    }

    def "it should generate statement for multi-code, multi-day rental"() {
        given:
        def daysRented = 4
        customer.addRental(getRental(getMovie(Movie.CHILDRENS), daysRented))
        customer.addRental(getRental(getMovie(Movie.NEW_RELEASE), daysRented))
        customer.addRental(getRental(getMovie(Movie.REGULAR), daysRented))

        expect:
        customer.statement() == """Rental Record for ${customer.name}
\t${customer._rentals[0].movie.title}\t3.0
\t${customer._rentals[1].movie.title}\t12.0
\t${customer._rentals[2].movie.title}\t5.0
Amount owed is 20.0
You earned 4 frequent renter points"""
    }

    def "it should handle special pricing based on length of rental"() {
        given:
        Rental rental = getRental(getMovie(priceCode), daysRented)
        customer.addRental(rental)

        expect:
        customer.statement() == """Rental Record for ${customer.name}
\t${rental.movie.title}\t${charge}
Amount owed is ${charge}
You earned ${frequentRenterPoints} frequent renter points"""

        where:
        priceCode         | daysRented | charge | frequentRenterPoints
        Movie.REGULAR     | 3          | 3.5    | 1
        Movie.CHILDRENS   | 4          | 3.0    | 1
    }

    def "it should double frequent renter points for new release rentals longer than 1 day"() {
        given:
        customer.addRental(getRental(getMovie(Movie.NEW_RELEASE), 2))

        expect:
        customer.statement() == """Rental Record for ${customer.name}
\t${customer._rentals[0].movie.title}\t6.0
Amount owed is 6.0
You earned 2 frequent renter points"""
    }

    def getMovie(int priceCode) {
        new Movie(faker.book().title(), priceCode)
    }

    def getRental(Movie movie, int daysRented) {
        new Rental(movie, daysRented)
    }
}
