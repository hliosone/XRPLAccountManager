package org.example.utility;

import okhttp3.HttpUrl;
import org.example.program_management.LedgerErrorMessage;
import org.example.secure.xrplAccount;
import org.xrpl.xrpl4j.client.faucet.FaucetClient;
import org.xrpl.xrpl4j.client.faucet.FundAccountRequest;
import org.xrpl.xrpl4j.model.transactions.Address;

import java.util.ArrayList;

public class FaucetService {

    public FaucetService(String faucetUrl){
        setFaucetAddress(faucetUrl);
    }

    public void setFaucetAddress(String faucetUrl) {
        this.faucetAddress = faucetUrl;
        this.currentFaucet = FaucetClient.construct(HttpUrl.get(this.faucetAddress));
        System.out.println("Constructed a FaucetClient connected to " + this.faucetAddress);
    }

    public void fundWallet(final ArrayList<xrplAccount> accountList) throws InterruptedException {
        if(!accountList.isEmpty()){
            System.out.println("Choose the account to fund !");
            xrplAccount selectedAccount = LedgerUtility.selectAccount(accountList,
                    accountList.size());

            System.out.println("Funding the account ...");
            try {
                currentFaucet.fundAccount(FundAccountRequest.of(selectedAccount.getrAddress()));
                // Wait for the Faucet Payment to get validated
                Thread.sleep(4000);
                System.out.println("Funded the account " + selectedAccount.getrAddress() + " using " + this.faucetAddress);
            }catch (InterruptedException e) {
                System.err.println("Thread was interrupted: " + e.getMessage());
                throw e;
            } catch (Exception e){
                System.out.println("An error occured while using the faucet : " + e.getMessage());
            }
        } else { LedgerErrorMessage.printError(LedgerErrorMessage.NO_MANAGED_ACCOUNTS); }


    }

    public void extWallet(Address rAddress) throws InterruptedException {
        System.out.println("Funding the account ...");
        try {
            currentFaucet.fundAccount(FundAccountRequest.of(rAddress));
            // Wait for the Faucet Payment to get validated
            Thread.sleep(4000);
            System.out.println("Funded the account " + rAddress + " using " + this.faucetAddress);
        }catch (InterruptedException e) {
            System.err.println("Thread was interrupted: " + e.getMessage());
            throw e;
        }
    }

    public FaucetClient getCurrentFaucet() {
        return currentFaucet;
    }

    public String getFaucetAddress() {
        return faucetAddress;
    }

    private FaucetClient currentFaucet;
    private String faucetAddress;



}
