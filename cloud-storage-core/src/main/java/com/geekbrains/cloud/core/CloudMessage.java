package com.geekbrains.cloud.core;

import java.io.Serializable;

public interface CloudMessage extends Serializable {
    CommandType getType();
}
