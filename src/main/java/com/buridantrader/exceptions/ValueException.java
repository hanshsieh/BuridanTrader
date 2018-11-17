package com.buridantrader.exceptions;

import javax.annotation.Nullable;

public class ValueException extends IllegalArgumentException {

    public ValueException(@Nullable String message, @Nullable Throwable cause) {
        super(message, cause);
    }

    public ValueException(@Nullable String message) {
        this(message, null);
    }

}
