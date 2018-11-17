package com.buridantrader.exceptions;

import javax.annotation.Nullable;

/**
 * Thrown to indicate that a value is too large or too small.
 */
public class ValueLimitException extends IllegalArgumentException {

    public ValueLimitException(@Nullable String message, @Nullable Throwable cause) {
        super(message, cause);
    }

    public ValueLimitException(@Nullable String message) {
        this(message, null);
    }

}
