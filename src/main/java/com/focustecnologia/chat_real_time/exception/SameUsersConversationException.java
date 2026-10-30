package com.focustecnologia.chat_real_time.exception;

public class SameUsersConversationException extends RuntimeException {
    public SameUsersConversationException(String message) {
        super(message);
    }
}
