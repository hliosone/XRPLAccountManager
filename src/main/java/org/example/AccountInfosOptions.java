package org.example;

public enum AccountInfosOptions {
    ACCOUNT_BALANCE(1),
    ACCOUNT_TRANSACTIONS(2),
    ACCOUNT_ACTIVATOR(3),
    QUIT_ACCOUNT_MENU(4);
    private final int value;

    AccountInfosOptions(int value) {
        this.value = value;
    }

    public static void printAccountInfosMenu(){
        System.out.println("1. Account balance");
        System.out.println("2. Account transactions");
        System.out.println("3. Account activator");
        System.out.println("4. Quit account menu");
    }

    public int getValue() {
        return value;
    }
}
