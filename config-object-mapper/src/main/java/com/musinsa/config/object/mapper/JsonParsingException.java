package com.musinsa.config.object.mapper;

import java.io.Serial;

public class JsonParsingException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = -5926523519140303529L;

    public JsonParsingException() {
    }

    public JsonParsingException(String message) {
        super(message);
    }

    public JsonParsingException(String message, Throwable cause) {
        super(message, cause);
    }

    public JsonParsingException(Throwable cause) {
        super(cause);
    }
}