package com.ajay.example.userApp;

public class UserNotFoundException extends  RuntimeException {
    public UserNotFoundException() {
        super("Users not found Exception");
    }
}
