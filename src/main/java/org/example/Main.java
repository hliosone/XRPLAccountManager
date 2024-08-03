package org.example;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;
import okhttp3.HttpUrl;
import org.xrpl.xrpl4j.client.JsonRpcClientErrorException;
import org.xrpl.xrpl4j.client.XrplClient;
import org.xrpl.xrpl4j.model.client.accounts.*;
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;
import org.xrpl.xrpl4j.model.client.common.LedgerSpecifier;
import org.xrpl.xrpl4j.model.client.serverinfo.ServerInfo;
import org.xrpl.xrpl4j.model.ledger.*;
import org.xrpl.xrpl4j.model.transactions.*;
import io.github.novacrypto.base58.Base58;

import org.xrpl.xrpl4j.model.client.accounts.AccountInfoRequestParams;
import org.xrpl.xrpl4j.model.client.accounts.AccountInfoResult;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws JsonRpcClientErrorException, InterruptedException, JsonProcessingException {

        // Construct a network client
        XrplClient xrplClient = new XrplClient(HttpUrl.get("https://s.altnet.rippletest.net:51234/"));
        ClientService testnetClient = new ClientService("https://s.altnet.rippletest.net:51234/");
        FaucetService rippleFaucet = new FaucetService("https://faucet.altnet.rippletest.net");

        AccountManager accountList = new AccountManager();
        Scanner scanner = new Scanner(System.in);
        MenuOptions userChoice = MenuOptions.VIEW_MY_ACCOUNTS;
        int choice = 0;
        do {
            MenuOptions.printMenuOptions();
            choice = scanner.nextInt();
            userChoice = MenuOptions.getMenuOptions(choice);

            switch (userChoice) {
                case CREATE_ACCOUNT -> {
                    System.out.println("Create Account choice !");
                    accountList.addAccount();
                }
                case FUND_ACCOUNT -> {
                    System.out.println("Fund account choice !");
                    System.out.println("Choose the account to fund !");
                    if(accountList.getNumberOfAccounts() > 0){
                        xrplAccount selectedAccount = selectAccount(accountList.getAccounts(),
                                accountList.getNumberOfAccounts());
                        System.out.println("Funding the account ...");
                        rippleFaucet.fundWallet(selectedAccount.getrAddress());
                    } else {
                        System.out.println("You don't have an account, please create one first !");
                    }
                }

                case SEND_PAYMENT -> {
                    System.out.println("Send Payment choice !");
                    System.out.println("Choose a sender account !");
                    xrplAccount selectedAccount = selectAccount(accountList.getAccounts(),
                            accountList.getNumberOfAccounts());

                    AccountInfoResult selectedAccountInfos = null;
                    scanner.nextLine();
                    try {
                        selectedAccountInfos = testnetClient.getAccountInfos(selectedAccount.getrAddress());
                    } catch (JsonRpcClientErrorException e) {
                        System.out.println("Error while fetching account data: " + e.getMessage());
                        break;
                    }

                    if (!selectedAccountInfos.validated()) {
                        System.out.println("The selected account is not activated! Please fund it to cover the " +
                                "reserve and transaction fees.");
                        continue;
                    }

                    System.out.println("Please enter the rAddress of the destination account: ");
                    Address destinationAccount = inputAddress();

                    if(destinationAccount == null){ break;}

                    if(Objects.equals(destinationAccount, selectedAccount.getrAddress())){
                        System.out.println("You cannot send a payment to yourself");
                        break;
                    }

                    ServerInfo serverInfoData = testnetClient.getServerInfo().info();
                    Optional<ServerInfo.ValidatedLedger> validatedLedgerOptional = serverInfoData.validatedLedger();
                    if (validatedLedgerOptional.isEmpty()) {
                        System.out.println("Error: Validated ledger information is not available.");
                        continue;
                    }

                    BigDecimal accountBalance = testnetClient
                            .getAccountXrpBalance(selectedAccount.getrAddress()
                                    , FunctionParameters.AVAILABLE_BALANCE);

                    System.out.println("Please enter the amount to send (Available balance : " + accountBalance +
                            " XRP):");

                    BigDecimal amountToSend = BigDecimal.ZERO;
                    String input = scanner.next();
                    scanner.nextLine();
                    try {
                        amountToSend = new BigDecimal(input);
                        System.out.println("Amount to send : " + amountToSend);
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid amount format.");
                        break;
                    }

                    BigDecimal amountToSendInDrops = amountToSend.multiply(BigDecimal.valueOf(1_000_000));
                    XrpCurrencyAmount amountInDrops = XrpCurrencyAmount.ofDrops(amountToSendInDrops.longValue());

                    // Checking validity of amount to send
                    BigDecimal openLedgerFee = testnetClient.getClient().fee().drops().openLedgerFee().toXrp();
                    if (amountToSend.compareTo(BigDecimal.ZERO) <= 0) {
                        System.out.println("Amount must be greater than zero.");
                    } else if (openLedgerFee.add(amountToSend).compareTo(accountBalance) > 0) {
                        System.out.println("Insufficient funds " + accountBalance +
                                ". The total amount (including fees) exceeds the available balance.");
                    } else {
                        try {
                            testnetClient.sendPayment(selectedAccount, destinationAccount, amountInDrops);
                        } catch (JsonRpcClientErrorException e) {
                            System.out.println("Error while sending payment: " + e.getMessage());
                        }
                    }
                }

                case DELETE_ACCOUNT -> {

                    System.out.println("Choose an account to delete !");
                    xrplAccount selectedAccount = selectAccount(accountList.getAccounts(), accountList.getNumberOfAccounts());

                    AccountInfoResult selectedAccountInfos = null;
                    try {
                        selectedAccountInfos = testnetClient.getAccountInfos(selectedAccount.getrAddress());
                    } catch (JsonRpcClientErrorException e) {
                        System.out.println("Error while fetching account data: " + e.getMessage());
                        break;
                    }

                    if (!selectedAccountInfos.validated()) {
                        System.out.println("The selected account is not activated!");
                        break;
                    } else if (selectedAccountInfos.accountData().ownerCount().longValue() > 1000) {
                        System.out.println("The selected account owns more than 1000 directory entries and cannot be deleted");
                        break;
                    } else if (selectedAccountInfos.accountData().ownerCount().longValue() > 0){
                        // Get account objects
                        AccountObjectsRequestParams requestParams = AccountObjectsRequestParams.builder()
                                .account(selectedAccount.getrAddress())
                                .ledgerSpecifier(LedgerSpecifier.VALIDATED)
                                .build();
                        AccountObjectsResult accountObjectsResult = testnetClient.getClient().accountObjects(requestParams);

                        for (LedgerObject obj : accountObjectsResult.accountObjects()) {
                            if (obj instanceof EscrowObject || obj instanceof PayChannelObject ||
                                    obj instanceof RippleStateObject || obj instanceof CheckObject) {
                                System.out.println("Account cannot be deleted because it has an " + obj.getClass().getSimpleName()
                                        + " object");
                                break;
                            }
                        }
                    }

                    UnsignedInteger accountSequence = selectedAccountInfos.accountData().sequence();
                    LedgerIndex latestLedgerIndex = testnetClient.getLatestLedgerIndex();

                    // Verify the difference between latest ledger index and acccount sequence number
                    if (latestLedgerIndex.unsignedIntegerValue().minus(accountSequence)
                            .compareTo(UnsignedInteger.valueOf(256)) < 0 ) {
                        System.out.println("The account cannot be deleted because it has not passed 256 ledgers since its sequence number.");
                        break;
                    }

                    System.out.println("Please enter the rAddress of the deleted account funds receiver: ");
                    Address destinationAccount = inputAddress();
                    if(destinationAccount == null){break;}


                    testnetClient.deleteAccount(selectedAccount, destinationAccount, testnetClient.getBaseIncrementXrp());
                }

                case VIEW_MY_ACCOUNTS -> {
                    System.out.println("View My Accounts choice !");
                    if(accountList.getNumberOfAccounts() > 0){
                        AccountInfosOptions.printAccountInfosMenu();
                        choice = scanner.nextInt();

                        xrplAccount infosSelectedAccount = selectAccount(accountList.getAccounts()
                                , accountList.getNumberOfAccounts());

                        switch (choice){

                            case 1 -> {
                                System.out.println("Choose the balance type:");
                                FunctionParameters.printBalanceTypes();
                                int balanceChoice = scanner.nextInt();
                                FunctionParameters balanceType = FunctionParameters.getBalanceOptions(balanceChoice);
                                System.out.println( FunctionParameters.getBalanceText(balanceChoice) + " "
                                        + testnetClient.getAccountXrpBalance(infosSelectedAccount.getrAddress()
                                        ,balanceType));
                                break;
                            }
                            case 3 -> {
//                                Address activator = testnetClient
//                                            .getAccountActivator(infosSelectedAccount.getrAddress());
//                                if(activator.value().isEmpty()){
//                                    System.out.println("Problem occurred, couldn't find activator !");
//                                } else {
//                                    System.out.println("Account activated by : " + activator);
//                                }
                            }
                            default -> throw new IllegalStateException("Unexpected value: " + choice);
                        }
                    } else {
                        System.out.println("You don't have an account, please create one !");
                    }
                }
                case QUIT_PROGRAM -> {
                    System.out.println("Quitting program ...");
                }
                default -> throw new IllegalStateException("Unexpected value: " + userChoice);
            }
        }while (userChoice != MenuOptions.QUIT_PROGRAM);

        System.exit(0);
    }

        public static boolean isValidXrpAddress(String address) {
            if (!xrplAccount.getAddressPattern().matcher(address).matches()) {
                return false;
            }
            try {
                byte[] decoded = Base58.base58Decode(address);
                return true;
            } catch (Exception e) {
                return false;
            }
        }

    public static xrplAccount selectAccount(ArrayList<xrplAccount> accounts, int numberOfAccounts) {
        Scanner selectScanner = new Scanner(System.in);
        int choice = 0;
        do {
            System.out.println("Please select the account in the list (1 to "
                    + numberOfAccounts + "):");
            for (int i = 1; i <= numberOfAccounts; ++i) {
                System.out.println(i + ": " + accounts.get(i - 1).getrAddress());
            }
            choice = selectScanner.nextInt();
            selectScanner.nextLine();

            if (choice < 1 || choice > numberOfAccounts) {
                System.out.println("Invalid choice, please try again.");
            }
        } while (choice < 1 || choice > numberOfAccounts);

        return accounts.get(choice - 1);
    }

    public static Address inputAddress() {
        String inputAddr = null;
        Scanner addessScanner = new Scanner(System.in);
        try {
            inputAddr = addessScanner.nextLine();
            if(!isValidXrpAddress(inputAddr)){
                System.out.println("Invalid address format.");
                return null;
            }
        } catch (Exception e) {
            System.out.println("Error reading input: " + e.getMessage());
            return null;
        }

        return Address.of(inputAddr);
    }

}