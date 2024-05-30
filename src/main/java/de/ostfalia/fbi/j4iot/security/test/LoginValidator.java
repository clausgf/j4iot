package de.ostfalia.fbi.j4iot.security.test;

public interface LoginValidator {

    String getName();

    String getPassword();

    boolean loginIsValid(String name, String password);
}
