package org.example.program_management;

public enum LedgerErrorMessage {
    ACCOUNT_NOT_FOUND(1),
    INSUFFICIENT_FUNDS(2),
    NO_MANAGED_ACCOUNTS(3);

    private final int value;

    LedgerErrorMessage(int value) {
        this.value = value;
    }

    public static void printError(LedgerErrorMessage errorType) {
        switch (errorType){
            case ACCOUNT_NOT_FOUND -> {
                System.out.println("The selected account is not activated!");
            }
            case INSUFFICIENT_FUNDS -> {
                System.out.println("Insufficient funds ! The total amount (including fees) " +
                        "exceeds the available balance.");
            }
            case NO_MANAGED_ACCOUNTS -> {
                System.out.println("You don't have any managed accounts !");
            }
            default -> {
                System.out.println("Unknown error !");
            }
        }
    }
}
