package org.example.utility;

import com.google.common.primitives.UnsignedInteger;
import io.github.novacrypto.base58.Base58;
import org.example.program_management.LedgerErrorMessage;
import org.example.program_management.MenuOptions;
import org.example.secure.xrplAccount;
import org.xrpl.xrpl4j.model.client.accounts.AccountInfoResult;
import org.xrpl.xrpl4j.model.client.accounts.AccountObjectsResult;
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;
import org.xrpl.xrpl4j.model.ledger.*;
import org.xrpl.xrpl4j.model.transactions.Address;

import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.Scanner;

public class LedgerUtility {

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

    //MAYBE cancel and just pass the rippledClient ?
    public static boolean isAccountDeletable(AccountInfoResult selectedAccountInfos,
                                             AccountObjectsResult selectedAccountObjects,
                                             LedgerIndex latestLedgerIndex){
        if(selectedAccountInfos == null){return false;}

        UnsignedInteger accountSequence = selectedAccountInfos.accountData().sequence();

        if (!selectedAccountInfos.validated()) {
            LedgerErrorMessage.printError(LedgerErrorMessage.ACCOUNT_NOT_FOUND);
            return false;
        } else if (selectedAccountInfos.accountData().ownerCount().longValue() > 1000) {
            System.out.println("The selected account owns more than 1000 directory entries and cannot be deleted");
            return false;
        } else if (selectedAccountInfos.accountData().ownerCount().longValue() > 0) {

            for (LedgerObject obj : selectedAccountObjects.accountObjects()) {
                if (obj instanceof EscrowObject || obj instanceof PayChannelObject ||
                        obj instanceof RippleStateObject || obj instanceof CheckObject) {
                    System.out.println("Account cannot be deleted because it has an " + obj.getClass().getSimpleName()
                            + " object");
                    return false;
                }
            }
        } else if (latestLedgerIndex.unsignedIntegerValue().minus(accountSequence)
                .compareTo(UnsignedInteger.valueOf(256)) < 0 )
            // Verify the difference between latest ledger index and account sequence number
            if (latestLedgerIndex.unsignedIntegerValue().minus(accountSequence)
                    .compareTo(UnsignedInteger.valueOf(256)) < 0 ) {

                // CREATE ERROR MESSAGE THERE TO THROW, SAME ON TOP FOR INSTANCE ?
                System.out.println("The account cannot be deleted because it has not passed 256 " +
                        "ledgers since its sequence number.");
                return false;
            }
        return true;
    }

    public static MenuOptions inputUserMenu() {
        Scanner scanner = new Scanner(System.in);
        int choice = 0;
        boolean validChoice = false;
        while (!validChoice) {
            try {
                choice = scanner.nextInt();
                if (choice < 1 || choice > 7) {
                    throw new InputMismatchException();
                } else { validChoice = true; }
            } catch (InputMismatchException e) {
                System.out.println("Invalid input! Please select a choice between 1 and 7!");
            } finally {
                scanner.nextLine();
            }
        }

        return MenuOptions.getMenuOptions(choice);
    }

    public static Address inputAddress() {
        String inputAddr;
        Scanner addessScanner = new Scanner(System.in);
        try {
            System.out.println("Please enter the rAddress of the destination account: ");
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

    public static xrplAccount selectAccount(ArrayList<xrplAccount> accounts, int numberOfAccounts) {
        Scanner selectScanner = new Scanner(System.in);
        //selectScanner.nextLine();
        int choice;
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
}
