package com.example.backend.domain.account.exception;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException() {
        super("가입된 유저가 아닙니다.");
    }
}
