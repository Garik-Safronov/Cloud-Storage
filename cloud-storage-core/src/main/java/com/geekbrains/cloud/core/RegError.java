package com.geekbrains.cloud.core;

public class RegError implements CloudMessage{

    @Override
    public CommandType getType() {
        return CommandType.REG_ERROR;
    }
}
