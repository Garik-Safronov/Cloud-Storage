package com.geekbrains.cloud.core;

public class RegConfirm implements CloudMessage{

    @Override
    public CommandType getType() {
        return CommandType.REG_OK;
    }
}
