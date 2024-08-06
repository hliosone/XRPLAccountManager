package org.example;

import org.xrpl.xrpl4j.client.JsonRpcClientErrorException;
import org.xrpl.xrpl4j.model.client.accounts.AccountTransactionsResult;
import org.xrpl.xrpl4j.model.client.accounts.AccountTransactionsTransactionResult;
import org.xrpl.xrpl4j.model.transactions.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

public enum TransactionsUtility {
    PAYMENT_TX(1),
    DELETE_TX(2);
    private final int value;

    TransactionsUtility(int value) {
        this.value = value;
    }

    public static void printTransactionTypes(){
        System.out.println("1. Payment");
        System.out.println("2. Delete Account");
    }

    public static TransactionsUtility getTransactionType(int value) {
        switch (value) {
            case 1 -> {
                return TransactionsUtility.PAYMENT_TX;
            }
            case 2 -> {
                return TransactionsUtility.DELETE_TX;
            }
            default -> {
                return null;
            }
        }
    }

    public static boolean userValidation() {
        Scanner scanner = new Scanner(System.in);
        String input;

        while (true) {
            System.out.print("Confirm the transaction ? (y/n): ");
            input = scanner.nextLine().trim().toLowerCase();

            if (input.equals("y")) {
                return true;
            } else if (input.equals("n")) {
                return false;
            } else {
                System.out.println("Invalid input. Please enter 'y' for yes or 'n' for no.");
            }
        }
    }

    public static boolean isTransactionOfType(Transaction transaction, TransactionsUtility type) {
        return switch (type) {
            case PAYMENT_TX -> transaction instanceof Payment;
            case DELETE_TX -> transaction instanceof AccountDelete;
        };
    }

    public static boolean isValidPaymentAmount(final BigDecimal amountToSend, final BigDecimal ledgerFee,
                                               final BigDecimal accountBalance){
        if (amountToSend.compareTo(BigDecimal.ZERO) <= 0) {
            System.out.println("Amount must be greater than zero.");
            return false;
        } else if (ledgerFee.add(amountToSend).compareTo(accountBalance) > 0) {
            LedgerErrorMessage.printError(LedgerErrorMessage.INSUFFICIENT_FUNDS);
            return false;
        } else { return true; }
    }

    public static void printTransaction(TransactionsUtility type, Transaction tx){
            if (type == TransactionsUtility.PAYMENT_TX) {
                Payment paymentTx = (Payment) tx;
                XrpCurrencyAmount amountInDrops = (XrpCurrencyAmount) paymentTx.amount();
                System.out.println(paymentTx.account() + " sent " + amountInDrops.toXrp() + " XRP to " + paymentTx.destination());
            } else if (type == TransactionsUtility.DELETE_TX){
                AccountDelete deleteTx = (AccountDelete) tx;
                System.out.println(deleteTx.account() + " performed an account deletion");
            }
    }

    public static void processInfosTransaction(ClientService rippledClient, Address accountAddress, int numberOfTxs,
                                               TransactionsUtility type) throws JsonRpcClientErrorException {
        AccountTransactionsResult transactionsResult = rippledClient.getAccountTransactions(accountAddress);
        List<AccountTransactionsTransactionResult<? extends Transaction>> txList = transactionsResult.transactions().stream().toList();

        if(!txList.isEmpty()){
            // Filter specific transactions
            List<Transaction> filteredTxList = txList.stream()
                    .map(result -> {
                        Transaction tx = result.resultTransaction().transaction(); // Remplace avec la méthode correcte
                        return tx;
                    })
                    .filter(tx -> tx != null && isTransactionOfType(tx, type))
                    .collect(Collectors.toList());

            List<Transaction> limitedTxList = filteredTxList.stream()
                    .limit(numberOfTxs)
                    .collect(Collectors.toList());

            if(!limitedTxList.isEmpty()){
                for (Transaction tx : limitedTxList) { printTransaction(type, tx); }
            } else {
                System.out.println("No " + type + " transactions found for " + accountAddress);
            }
        } else {
            System.out.println("Cannot find transactions with the specified parameters for the account");
        }
    }
}
