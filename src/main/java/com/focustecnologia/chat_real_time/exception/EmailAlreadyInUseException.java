package com.focustecnologia.chat_real_time.exception;

public class EmailAlreadyInUseException extends RuntimeException {
    public EmailAlreadyInUseException() {
        super("Este e-mail já está cadastrado");
    }
}
