package com.geekbrains.cloud.core;

import lombok.Data;

@Data
public class PathResponse implements CloudMessage{

    private String newDir;

    public PathResponse(String newDir) {
        this.newDir = newDir;
    }

    @Override
    public CommandType getType() {
        return CommandType.PATH_RESPONSE;
    }
}
