package com.geekbrains.cloud.core;

import lombok.Data;

@Data
public class AuthMessage implements CloudMessage{

    private String login;
    private String password;

    public AuthMessage(String login, String password) {
        this.login = login;
        this.password = password;
    }

    @Override
    public CommandType getType() {
        return CommandType.AUTH;
    }
}
