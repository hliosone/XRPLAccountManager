package org.example;

public enum LedgerErrorMessage {
    ACCOUNT_NOT_FOUND(1),
    INSUFFICIENT_FUNDS(2);

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
            default -> {
                System.out.println("Unknown error !");
            }
        }
    }
}
