package org.example;

import io.github.novacrypto.base58.Base58;
import org.xrpl.xrpl4j.model.transactions.Address;

import java.util.ArrayList;
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
}
