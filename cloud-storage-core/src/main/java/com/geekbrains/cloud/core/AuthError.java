package com.geekbrains.cloud.core;

public class AuthError implements CloudMessage{

    @Override
    public CommandType getType() {
        return CommandType.AUTH_ERROR;
    }
}
