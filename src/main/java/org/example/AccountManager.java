package org.example;

import org.xrpl.xrpl4j.crypto.keys.KeyPair;
import org.xrpl.xrpl4j.model.transactions.Address;

import java.util.ArrayList;

public class AccountManager {
    private ArrayList<xrplAccount> managedAccounts = new ArrayList<>();

    public void addAccount(KeyPair importedKeyPair) {
        xrplAccount newAcc = new xrplAccount(importedKeyPair);
        managedAccounts.add(newAcc);
    }

    public xrplAccount getAccount(int choice){
        return managedAccounts.get(choice);
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
