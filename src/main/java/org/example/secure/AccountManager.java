package org.example.secure;

import org.xrpl.xrpl4j.crypto.keys.KeyPair;
import org.xrpl.xrpl4j.model.transactions.Address;

import java.util.ArrayList;
import java.util.Scanner;

public class AccountManager {
    private ArrayList<xrplAccount> managedAccounts = new ArrayList<>();

    public void addAccount(KeyPair importedKeyPair) {
        xrplAccount newAcc = new xrplAccount(importedKeyPair);
        managedAccounts.add(newAcc);
    }

    public void importAccount(){
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter Seed Key: ");
        String privateKeyInput = scanner.nextLine();

        try {
            System.out.println("Importing account...");
            org.xrpl.xrpl4j.crypto.keys.Seed seed = org.xrpl.xrpl4j.crypto.keys.Seed.fromBase58EncodedSecret
                    ((org.xrpl.xrpl4j.crypto.keys.Base58EncodedSecret.of(privateKeyInput)));
            KeyPair keyPair = seed.deriveKeyPair();
            this.addAccount(keyPair);
            System.out.println("Account " + keyPair.publicKey().deriveAddress()
                    + " have been successfully imported !");
        } catch (org.xrpl.xrpl4j.codec.addresses.exceptions.EncodingFormatException e) {
            System.err.println("Key format error : " + e.getMessage());
        }
    }

    public Address getAccountAddress(int choice){
        return managedAccounts.get(choice).getrAddress();
    }

    public ArrayList<xrplAccount> getAccounts() {
        return managedAccounts;
    }

    public int getNumberOfAccounts(){
        return managedAccounts.size();
    }
}
