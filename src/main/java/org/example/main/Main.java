package org.example.main;

import org.example.program_management.*;
import org.example.secure.*;
import org.example.utility.*;
import org.xrpl.xrpl4j.model.transactions.Address;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.primitives.UnsignedInteger;
import org.xrpl.xrpl4j.client.JsonRpcClientErrorException;
import org.xrpl.xrpl4j.model.client.accounts.*;
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;
import org.xrpl.xrpl4j.model.ledger.*;
import org.xrpl.xrpl4j.model.transactions.*;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws JsonRpcClientErrorException, InterruptedException, JsonProcessingException {
        // Construct a network client
        ClientService testnetClient = new ClientService("https://s.altnet.rippletest.net:51234/");
        FaucetService rippleFaucet = new FaucetService("https://faucet.altnet.rippletest.net");

        AccountManager accountList = new AccountManager();
        Scanner scanner = new Scanner(System.in);
        //scanner.nextLine();
        MenuOptions userChoice = null;
        int choice = 0;
        do {
            MenuOptions.printMenuOptions();
            userChoice = LedgerUtility.inputUserMenu();

            switch (userChoice) {
                case CREATE_ACCOUNT -> accountList.addAccount(null);
                case IMPORT_ACCOUNT -> accountList.importAccount();
                case FUND_ACCOUNT -> {
                    try {
                        rippleFaucet.fundWallet(accountList.getAccounts());
                    } catch (InterruptedException e) { continue; }
                }
                case SEND_PAYMENT -> {
                    testnetClient.constructPayment(accountList);
                }
                case DELETE_ACCOUNT -> {

                    System.out.println("Choose an account to delete !");
                    xrplAccount selectedAccount = LedgerUtility.selectAccount(accountList.getAccounts()
                            , accountList.getNumberOfAccounts());

                    AccountInfoResult selectedAccountInfos = testnetClient.getAccountInfos(selectedAccount.getrAddress());
                    AccountObjectsResult accountObjectsResult;
                    try {
                        accountObjectsResult = testnetClient.getAccountObjects(selectedAccount.getrAddress());
                    } catch (JsonRpcClientErrorException e) { continue; }

                    LedgerIndex latestLedgerIndex = testnetClient.getLatestLedgerIndex();

                    //if(selectedAccountInfos == null || accountObjectsResult == null){continue;}
                    if(!(LedgerUtility.isAccountDeletable(selectedAccountInfos, accountObjectsResult,
                            latestLedgerIndex))){
                        continue;
                    };

                    System.out.println("Please enter the rAddress of the deleted account funds receiver: ");
                    Address destinationAccount = LedgerUtility.inputAddress();
                    if(destinationAccount == null){break;}
                    if(TransactionsUtility.userValidation()){
                        testnetClient.deleteAccount(selectedAccount, destinationAccount,
                                testnetClient.getBaseIncrementXrp());
                    }
                }

                case VIEW_MY_ACCOUNTS -> {
                    System.out.println("View My Accounts choice !");
                    if(accountList.getNumberOfAccounts() > 0){
                        AccountInfosOptions.printAccountInfosMenu();
                        choice = scanner.nextInt();

                        xrplAccount infosSelectedAccount = LedgerUtility.selectAccount(accountList.getAccounts()
                                , accountList.getNumberOfAccounts());
                        AccountInfosOptions.processAccountInfosOptions(testnetClient,
                                infosSelectedAccount.getrAddress(), choice);
                    } else {
                        System.out.println("You don't have an account, please create one !");
                    }
                }
                case QUIT_PROGRAM -> System.out.println("Quitting program ...");
                default -> System.out.println("Unexpected value, please enter a number between 1 and 7 !");
            }
            System.out.println();
        }while (userChoice != MenuOptions.QUIT_PROGRAM);
        System.exit(0);
    }
}