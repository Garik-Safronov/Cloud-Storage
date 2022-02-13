package com.geekbrains.cloud.core;

import lombok.Data;

@Data
public class PathUpRequest implements CloudMessage{

    @Override
    public CommandType getType() {
        return CommandType.PATH_UP;
    }
}
