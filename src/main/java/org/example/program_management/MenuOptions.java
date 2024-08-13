package org.example.program_management;

public enum MenuOptions {
    CREATE_ACCOUNT(1),
    IMPORT_ACCOUNT(2),
    FUND_ACCOUNT(3),
    SEND_PAYMENT(4),
    DELETE_ACCOUNT(5),
    VIEW_MY_ACCOUNTS(6),
    QUIT_PROGRAM(7);

    MenuOptions(int value) {
    }

    public static void printMenuOptions(){
        System.out.println("Choose an option:");
        System.out.println("1. Create Account");
        System.out.println("2. Import account");
        System.out.println("3. Fund Account");
        System.out.println("4. Send Payment");
        System.out.println("5. Delete Account");
        System.out.println("6. View My Account(s)");
        System.out.println("7. Quit Program");
    }

    public static MenuOptions getMenuOptions(int value) {
        switch (value){
            case 1 -> {
                return MenuOptions.CREATE_ACCOUNT;
            }
            case 2 -> {
                return MenuOptions.IMPORT_ACCOUNT;
            }
            case 3 -> {
                return MenuOptions.FUND_ACCOUNT;
            }
            case 4 -> {
                return MenuOptions.SEND_PAYMENT;
            }
            case 5 -> {
                return MenuOptions.DELETE_ACCOUNT;
            }
            case 6 -> {
                return MenuOptions.VIEW_MY_ACCOUNTS;
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
