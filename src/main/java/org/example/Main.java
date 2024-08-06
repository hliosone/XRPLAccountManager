package org.example;

import java.security.*;

import org.bouncycastle.util.encoders.Hex;
import org.xrpl.xrpl4j.codec.addresses.KeyType;
import org.xrpl.xrpl4j.crypto.core.keys.*;
import org.xrpl.xrpl4j.crypto.core.*;
import org.xrpl.xrpl4j.crypto.keys.ImmutableKeyPair;
import org.xrpl.xrpl4j.crypto.keys.KeyPair;
import org.xrpl.xrpl4j.model.transactions.Address;

import org.xrpl.xrpl4j.crypto.keys.KeyPair;
import org.xrpl.xrpl4j.crypto.keys.PrivateKey;
import org.xrpl.xrpl4j.crypto.keys.PublicKey;


import java.util.*;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;
import okhttp3.HttpUrl;
import org.xrpl.xrpl4j.client.JsonRpcClientErrorException;
import org.xrpl.xrpl4j.client.XrplClient;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;
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
import java.util.Scanner;

import static ch.qos.logback.core.encoder.ByteArrayUtil.hexStringToByteArray;

public class Main {
    public static void main(String[] args) throws JsonRpcClientErrorException, InterruptedException, JsonProcessingException {
        // Construct a network client
        ClientService testnetClient = new ClientService("https://s.altnet.rippletest.net:51234/");
        FaucetService rippleFaucet = new FaucetService("https://faucet.altnet.rippletest.net");

        AccountManager accountList = new AccountManager();
        Scanner scanner = new Scanner(System.in);
        MenuOptions userChoice;
        int choice;
        
        do {
            MenuOptions.printMenuOptions();
            choice = scanner.nextInt();
            userChoice = MenuOptions.getMenuOptions(choice);

            switch (userChoice) {
                case CREATE_ACCOUNT -> {
                    accountList.addAccount(null);
                }
                case IMPORT_ACCOUNT -> {
                    scanner.nextLine();
                    System.out.print("Enter Seed Key: ");
                    String privateKeyInput = scanner.nextLine();

                    try {
                        System.out.println("Importing account...");
                        org.xrpl.xrpl4j.crypto.keys.Seed seed = org.xrpl.xrpl4j.crypto.keys.Seed.fromBase58EncodedSecret
                                ((org.xrpl.xrpl4j.crypto.keys.Base58EncodedSecret.of(privateKeyInput)));
                        KeyPair keyPair = seed.deriveKeyPair();
                        accountList.addAccount(keyPair);
                        System.out.println("Account " + keyPair.publicKey().deriveAddress()
                                + " have been successfully imported !");
                    } catch (org.xrpl.xrpl4j.codec.addresses.exceptions.EncodingFormatException e) {
                        System.err.println("Key format error : " + e.getMessage());
                    }
                }
                case FUND_ACCOUNT -> {
                    if(accountList.getNumberOfAccounts() > 0){
                        System.out.println("Choose the account to fund !");
                        xrplAccount selectedAccount = LedgerUtility.selectAccount(accountList.getAccounts(),
                                accountList.getNumberOfAccounts());
                        rippleFaucet.fundWallet(selectedAccount.getrAddress());
                    } else { LedgerErrorMessage.printError(LedgerErrorMessage.NO_MANAGED_ACCOUNTS); }
                }

                case SEND_PAYMENT -> {
                    System.out.println("Choose the sender account !");
                    xrplAccount selectedAccount = LedgerUtility.selectAccount(accountList.getAccounts(),
                            accountList.getNumberOfAccounts());

                    AccountInfoResult selectedAccountInfos = testnetClient
                            .getAccountInfos(selectedAccount.getrAddress());
                    scanner.nextLine();
                    if(selectedAccountInfos == null){continue;}

                    if (!selectedAccountInfos.validated()) {
                        LedgerErrorMessage.printError(LedgerErrorMessage.ACCOUNT_NOT_FOUND);
                        continue;
                    }

                    System.out.println("Please enter the rAddress of the destination account: ");
                    Address destinationAccount = LedgerUtility.inputAddress();

                    if(destinationAccount == null){ continue;}

                    if(Objects.equals(destinationAccount, selectedAccount.getrAddress())){
                        System.out.println("You cannot send a payment to yourself");
                        continue;
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

                    BigDecimal amountToSend;
                    String input = scanner.next();
                    scanner.nextLine();
                    try {
                        amountToSend = new BigDecimal(input);
                        System.out.println("Amount to send : " + amountToSend);
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid amount format.");
                        continue;
                    }

                    BigDecimal amountToSendInDrops = amountToSend.multiply(BigDecimal.valueOf(1_000_000));
                    XrpCurrencyAmount amountInDrops = XrpCurrencyAmount.ofDrops(amountToSendInDrops.longValue());

                    // Checking validity of amount to send
                    BigDecimal openLedgerFee = testnetClient.getClient().fee().drops().openLedgerFee().toXrp();
                    if(TransactionsUtility.isValidPaymentAmount(amountToSend, openLedgerFee, accountBalance)){
                        if(TransactionsUtility.userValidation()){
                            try {
                                testnetClient.sendPayment(selectedAccount, destinationAccount, amountInDrops);
                            } catch (JsonRpcClientErrorException e) {
                                System.out.println("Error while sending payment: " + e.getMessage());
                                continue;
                            }
                        }
                    }
                }

                case DELETE_ACCOUNT -> {

                    System.out.println("Choose an account to delete !");
                    xrplAccount selectedAccount = LedgerUtility.selectAccount(accountList.getAccounts()
                            , accountList.getNumberOfAccounts());

                    AccountInfoResult selectedAccountInfos = testnetClient.getAccountInfos(selectedAccount.getrAddress());
                    if(selectedAccountInfos == null){continue;}

                    if (!selectedAccountInfos.validated()) {
                        LedgerErrorMessage.printError(LedgerErrorMessage.ACCOUNT_NOT_FOUND);
                        break;
                    } else if (selectedAccountInfos.accountData().ownerCount().longValue() > 1000) {
                        System.out.println("The selected account owns more than 1000 directory entries and cannot be deleted");
                        break;
                    } else if (selectedAccountInfos.accountData().ownerCount().longValue() > 0) {

                        AccountObjectsResult accountObjectsResult;
                        try {
                            accountObjectsResult = testnetClient.getAccountObjects(selectedAccount.getrAddress());
                        } catch (JsonRpcClientErrorException e) { continue; }

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

                    // Verify the difference between latest ledger index and account sequence number
                    if (latestLedgerIndex.unsignedIntegerValue().minus(accountSequence)
                            .compareTo(UnsignedInteger.valueOf(256)) < 0 ) {
                        System.out.println("The account cannot be deleted because it has not passed 256 " +
                                "ledgers since its sequence number.");
                        break;
                    }

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
                case QUIT_PROGRAM -> {
                    System.out.println("Quitting program ...");
                }
                default -> throw new IllegalStateException("Unexpected value: " + userChoice);
            }
            System.out.println();
        }while (userChoice != MenuOptions.QUIT_PROGRAM);
        System.exit(0);
    }
}