package org.example.program_management;

import org.example.secure.ClientService;
import org.example.utility.TransactionsUtility;
import org.xrpl.xrpl4j.client.JsonRpcClientErrorException;
import org.xrpl.xrpl4j.model.transactions.Address;

import java.util.Scanner;

public enum AccountInfosOptions {
    ACCOUNT_BALANCE,
    ACCOUNT_TRANSACTIONS,
    ACCOUNT_ACTIVATOR,
    QUIT_ACCOUNT_MENU;

    AccountInfosOptions() {}

    public static void printAccountInfosMenu(){
        System.out.println("1. Account balance");
        System.out.println("2. Account transactions");
        System.out.println("3. Account activator");
        System.out.println("4. Quit account menu");
    }

    public static void processAccountInfosOptions(ClientService client, Address accountAddress, int choice)
            throws JsonRpcClientErrorException {
        Scanner scanner = new Scanner(System.in);
        switch (choice) {
            case 1 -> {
                System.out.println("Choose the balance type:");
                FunctionParameters.printBalanceTypes();
                int balanceChoice = scanner.nextInt();
                FunctionParameters balanceType = FunctionParameters.getBalanceOptions(balanceChoice);
                System.out.println(FunctionParameters.getBalanceText(balanceChoice) + " "
                        + client.getAccountXrpBalance(accountAddress
                        , balanceType) + " XRP");
            }
            case 2 -> {
                System.out.println("Select the transaction type you want to search");
                TransactionsUtility.printTransactionTypes();
                int txChoice = scanner.nextInt();
                TransactionsUtility txType = TransactionsUtility.getTransactionType(txChoice);

                System.out.println("Enter the number of recent transactions you want to check out:");
                System.out.println("For instance, entering 10 will show you up to 10 of the latest transactions.");
                int numberTransactions = scanner.nextInt();

                TransactionsUtility.processInfosTransaction(client, accountAddress, numberTransactions, txType);
            }
            case 3 -> {
                Address activator = client
                        .getAccountActivator(accountAddress);
                if(activator == null){
                    System.out.println("The account is not active !");
                } else {
                    System.out.println("Account activated by : " + activator);
                }
            }
            default -> {

            }
        }
    }
}

