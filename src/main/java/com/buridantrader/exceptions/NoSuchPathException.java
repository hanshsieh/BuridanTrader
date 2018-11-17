package com.buridantrader.exceptions;

import javax.annotation.Nullable;

/**
 * Thrown to indicate that there's no trading path between two currencies.
 */
public class NoSuchPathException extends IllegalArgumentException {
    public NoSuchPathException(@Nullable String message, @Nullable Throwable cause) {
        super(message, cause);
    }
    public NoSuchPathException(@Nullable String message) {
        super(message);
    }
}
