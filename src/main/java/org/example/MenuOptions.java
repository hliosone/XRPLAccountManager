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

    public static void printMenuOptions(){
        System.out.println("Choose an option:");
        System.out.println("1. Create Account");
        System.out.println("2. Fund Account");
        System.out.println("3. Send Payment");
        System.out.println("4. Delete Account");
        System.out.println("5. View My Account(s)");
        System.out.println("6. View On-Chain Account");
        System.out.println("7. Quit Program");
    }

    public static MenuOptions getMenuOptions(int value) {
        switch (value){
            case 1 -> {
                return MenuOptions.CREATE_ACCOUNT;
            }
            case 2 -> {
                return MenuOptions.FUND_ACCOUNT;
            }
            case 3 -> {
                return MenuOptions.SEND_PAYMENT;
            }
            case 4 -> {
                return MenuOptions.DELETE_ACCOUNT;
            }
            case 5 -> {
                return MenuOptions.VIEW_MY_ACCOUNTS;
            }
            case 6 -> {
                return MenuOptions.VIEW_CHAIN_ACCOUNT;
            }
            case 7 -> {
                return MenuOptions.QUIT_PROGRAM;
            }
            default -> {
                return null;
            }
        }
    }
}
