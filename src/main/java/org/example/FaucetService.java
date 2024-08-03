package org.example;

import okhttp3.HttpUrl;
import org.xrpl.xrpl4j.client.faucet.FaucetClient;
import org.xrpl.xrpl4j.client.faucet.FundAccountRequest;
import org.xrpl.xrpl4j.model.transactions.Address;

public class FaucetService {

    public FaucetService(String faucetUrl){
        setFaucetAddress(faucetUrl);
    }

    public void setFaucetAddress(String faucetUrl) {
        this.faucetAddress = faucetUrl;
        this.currentFaucet = FaucetClient.construct(HttpUrl.get(this.faucetAddress));
        System.out.println("Constructed a FaucetClient connected to " + this.faucetAddress);
    }

    public void fundWallet(Address rAddress) throws InterruptedException {
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
