package com.bobocode;

import com.bobocode.entities.Person;

import static com.bobocode.bibernate.util.SqlUtils.createUpdateQuery;

public class Main {


    public static void main(String[] args) {

        Person person = new Person();
        person.setFirstName("Pavlo");
        person.setFirstName("Chechehov");
        String updateQuery = createUpdateQuery(person);

        System.out.println(updateQuery);
    }
}
