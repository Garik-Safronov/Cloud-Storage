package com.geekbrains.cloud.core;

import lombok.Data;

@Data
public class AuthConfirm implements CloudMessage{

    @Override
    public CommandType getType() {
        return CommandType.AUTH_OK;
    }
}
