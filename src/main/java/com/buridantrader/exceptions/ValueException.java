package com.buridantrader.exceptions;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ValueException extends IllegalArgumentException {

    public enum Reason {
        TOO_SMALL,
        TOO_LARGE
    }

    private final Reason reason;

    public ValueException(@Nonnull Reason reason, @Nullable String message, @Nullable Throwable cause) {
        super(message, cause);
        this.reason = reason;
    }

    public ValueException(@Nonnull Reason reason, @Nullable String message) {
        this(reason, message, null);
    }

    @Nonnull
    public Reason getReason() {
        return reason;
    }
}
