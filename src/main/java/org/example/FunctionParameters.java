package org.example;

public enum FunctionParameters {
    TOTAL_BALANCE(1),
    RESERVED_BALANCE(2),
    AVAILABLE_BALANCE(3);

    private final int value;

    FunctionParameters(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
