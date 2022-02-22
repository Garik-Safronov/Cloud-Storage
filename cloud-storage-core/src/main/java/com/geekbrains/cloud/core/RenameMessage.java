package com.geekbrains.cloud.core;

import lombok.Data;

@Data
public class RenameMessage implements CloudMessage{

    private final String oldFileName;
    private final String newFileName;

    public RenameMessage(String oldFileName, String newFileName) {
        this.oldFileName = oldFileName;
        this.newFileName = newFileName;
    }

    @Override
    public CommandType getType() {
        return CommandType.RENAME;
    }
}
