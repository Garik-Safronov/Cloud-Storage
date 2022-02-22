package com.geekbrains.cloud.core;

import lombok.Data;

@Data
public class UpdatePathRequest implements CloudMessage{

    private final String dirName;

    public UpdatePathRequest(String dirName) {
        this.dirName = dirName;
    }

    @Override
    public CommandType getType() {
        return CommandType.UPDATE_PATH_REQUEST;
    }
}
