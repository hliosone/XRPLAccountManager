package org.example;

public enum MenuOptions {
    CREATE_ACCOUNT(1),
    FUND_ACCOUNT(2),
    SEND_PAYMENT(3),
    DELETE_ACCOUNT(4),
    VIEW_MY_ACCOUNTS(5),
    VIEW_CHAIN_ACCOUNT(6),
    QUIT_PROGRAM(7);

    private final int value;

    MenuOptions(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
