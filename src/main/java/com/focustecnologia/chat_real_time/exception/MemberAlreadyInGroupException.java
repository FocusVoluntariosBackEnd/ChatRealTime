package com.focustecnologia.chat_real_time.exception;

public class MemberAlreadyInGroupException extends RuntimeException {
    public MemberAlreadyInGroupException(String message) {
        super(message);
    }
}
