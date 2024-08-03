package org.example;

public enum AccountInfosOptions {
    ACCOUNT_BALANCE(1),
    ACCOUNT_TRANSACTIONS(2);

    private final int value;

    AccountInfosOptions(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
