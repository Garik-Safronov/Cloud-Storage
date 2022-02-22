package com.geekbrains.cloud.core;

import lombok.Data;

@Data
public class RegMessage implements CloudMessage{

    private String login;
    private String password;
    private String nickname;

    public RegMessage(String login, String password, String nickname) {
        this.login = login;
        this.password = password;
        this.nickname = nickname;
    }

    @Override
    public CommandType getType() {
        return CommandType.REG;
    }
}
