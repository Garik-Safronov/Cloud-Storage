package com.geekbrains.cloud.core;

import lombok.Data;

@Data
public class DeleteMessage implements CloudMessage{

    private final String fileName;

    public DeleteMessage(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public CommandType getType() {
        return CommandType.DELETE;
    }
}
