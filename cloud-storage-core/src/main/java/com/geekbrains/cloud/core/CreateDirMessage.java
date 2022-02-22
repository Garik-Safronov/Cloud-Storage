package com.geekbrains.cloud.core;

import lombok.Data;

import java.nio.file.Path;

@Data
public class CreateDirMessage implements CloudMessage{

    private final String newDirName;

    public CreateDirMessage(String newDirName) {
        this.newDirName = newDirName;
    }

    @Override
    public CommandType getType() {
        return CommandType.MK_DIR;
    }
}
