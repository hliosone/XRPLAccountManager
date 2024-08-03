package org.example;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;
import okhttp3.HttpUrl;
import org.xrpl.xrpl4j.client.JsonRpcClient;
import org.xrpl.xrpl4j.client.JsonRpcClientErrorException;
import org.xrpl.xrpl4j.client.JsonRpcRequest;
import org.xrpl.xrpl4j.client.XrplClient;
import org.xrpl.xrpl4j.crypto.keys.PrivateKey;
import org.xrpl.xrpl4j.crypto.signing.SignatureService;
import org.xrpl.xrpl4j.crypto.signing.SingleSignedTransaction;
import org.xrpl.xrpl4j.crypto.signing.bc.BcSignatureService;
import org.xrpl.xrpl4j.model.client.accounts.AccountInfoRequestParams;
import org.xrpl.xrpl4j.model.client.accounts.AccountInfoResult;
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;
import org.xrpl.xrpl4j.model.client.common.LedgerSpecifier;
import org.xrpl.xrpl4j.model.client.fees.FeeResult;
import org.xrpl.xrpl4j.model.client.ledger.LedgerRequestParams;
import org.xrpl.xrpl4j.model.client.serverinfo.ServerInfo;
import org.xrpl.xrpl4j.model.client.serverinfo.ServerInfoResult;
import org.xrpl.xrpl4j.model.client.transactions.SubmitResult;
import org.xrpl.xrpl4j.model.client.transactions.TransactionRequestParams;
import org.xrpl.xrpl4j.model.client.transactions.TransactionResult;
import org.xrpl.xrpl4j.model.immutables.FluentCompareTo;
import org.xrpl.xrpl4j.model.transactions.*;

import java.math.BigDecimal;
import java.util.Optional;

public class ClientService {
    private final XrplClient rippledClient;

    public ClientService(String url) {
        this.rippledClient = new XrplClient(HttpUrl.get(url));
    }

    public XrplClient getClient() {
        return rippledClient;
    }

    public ServerInfoResult getServerInfo() throws JsonRpcClientErrorException {
        return rippledClient.serverInformation();
    }

    public XrpCurrencyAmount getBaseIncrementXrp() throws JsonRpcClientErrorException {
        ServerInfo serverInfoData = getServerInfo().info();
        Optional<ServerInfo.ValidatedLedger> validatedLedgerOptional = serverInfoData.validatedLedger();
        if (validatedLedgerOptional.isEmpty()) {
            System.out.println("Error: Validated ledger information is not available.");
            return null;
        }
        return validatedLedgerOptional.get().reserveIncXrp();
    }

    public AccountInfoResult getAccountInfos(Address accountAddress) throws JsonRpcClientErrorException {
        AccountInfoRequestParams requestParams = AccountInfoRequestParams.builder()
                .account(accountAddress)
                .ledgerSpecifier(LedgerSpecifier.VALIDATED)
                .build();
        AccountInfoResult accountInfoResult = rippledClient.accountInfo(requestParams);
        return accountInfoResult;
    }

    public BigDecimal getAccountBalance(Address accountAddress) throws JsonRpcClientErrorException {
        AccountInfoResult infos = getAccountInfos(accountAddress);
        return infos.accountData().balance().toXrp();
    }

    public LedgerIndex getLatestLedgerIndex() throws JsonRpcClientErrorException {
        try {
            return rippledClient.ledger(
                            LedgerRequestParams.builder()
                                    .ledgerSpecifier(LedgerSpecifier.VALIDATED)
                                    .build()
                    )
                    .ledgerIndex()
                    .orElseThrow(() -> new RuntimeException("LedgerIndex not available."));
        } catch (JsonRpcClientErrorException e) {
            System.out.println("Failed to get the latest ledger index (JsonRpcClientErrorException): " + e.getMessage());
            return null;
        } catch (RuntimeException e) {
            System.out.println("Failed to get the latest ledger index (RuntimeException): " + e.getMessage());
            return null;
        }
    }

    public <T extends Transaction> TransactionResult<T> getTransactionResult(Hash256 txHash, Class<T> transactionType) throws InterruptedException, JsonRpcClientErrorException {
        return getTransactionResult(txHash, transactionType, null);
    }

    public <T extends Transaction> TransactionResult<T> getTransactionResult(Hash256 txHash, Class<T> transactionType, UnsignedInteger lastLedgerSequence) throws InterruptedException, JsonRpcClientErrorException {

        TransactionResult<T> transactionResult = null;

        boolean transactionValidated = false;
        boolean transactionExpired = false;
        while (!transactionValidated && !transactionExpired) {
            Thread.sleep(4 * 1000);

            //Check return !!!
            LedgerIndex latestValidatedLedgerIndex = getLatestLedgerIndex();

            transactionResult = rippledClient.transaction(TransactionRequestParams.of(txHash), transactionType);

            if (transactionResult.validated()) {
                System.out.println(transactionType.getSimpleName() + " was validated with result code "
                        + transactionResult.metadata().get().transactionResult());
                transactionValidated = true;
            } else if (lastLedgerSequence != null) {
                boolean lastLedgerSequenceHasPassed = FluentCompareTo.
                        is(latestValidatedLedgerIndex.unsignedIntegerValue())
                        .greaterThan(UnsignedInteger.valueOf(lastLedgerSequence.intValue()));
                if (lastLedgerSequenceHasPassed) {
                    System.out.println("LastLedgerSequence has passed. Last tx response: " +
                            transactionResult);
                    return null;
                } else {
                    System.out.println(transactionType.getSimpleName() + " not yet validated.");
                }
            } else {
                System.out.println("Transaction not found !");
                return null;
            }
        }
        return transactionResult;
    }


    public String sendPayment(xrplAccount account, Address destination, XrpCurrencyAmount amount) throws JsonRpcClientErrorException, JsonProcessingException, InterruptedException {

        AccountInfoResult accountInfoResult = getAccountInfos(account.getrAddress());
        UnsignedInteger sequence = accountInfoResult.accountData().sequence();

        // Request current fee information from rippled
        XrpCurrencyAmount openLedgerFee = this.rippledClient.fee().drops().openLedgerFee();

        // Get the latest validated ledger index
        LedgerIndex latestIndex = getLatestLedgerIndex();

        // LastLedgerSequence is the current ledger index + 4
        UnsignedInteger lastLedgerSequence = latestIndex.plus(UnsignedInteger.valueOf(4)).unsignedIntegerValue();

        // Build the payment
        Payment payment = Payment.builder()
                .account(account.getrAddress())
                .amount(amount)
                .destination(destination)
                .sequence(sequence)
                .fee(openLedgerFee)
                .signingPublicKey(account.getRandomKeyPair().publicKey())
                .lastLedgerSequence(lastLedgerSequence)
                .build();

        // Construct a SignatureService to sign the Payment
        SignatureService<PrivateKey> signatureService = new BcSignatureService();
        SingleSignedTransaction<Payment> signedPayment =
                signatureService.sign(account.getRandomKeyPair().privateKey(), payment);

        // Submit the Payment
        this.rippledClient.submit(signedPayment);

        // Wait for validation
        TransactionResult<Payment> transactionResult = getTransactionResult(signedPayment.hash(), Payment.class, lastLedgerSequence);

        String resultCode = "Unknown";
        if (transactionResult != null) {
            if (transactionResult.metadata().isPresent()) {
                TransactionMetadata metadata = transactionResult.metadata().get();
                resultCode = metadata.transactionResult();
                metadata.deliveredAmount().ifPresent(deliveredAmount ->
                        System.out.println("XRP Delivered: " + amount.toXrp()));
            } else {
                System.out.println("No metadata available for the transaction.");
            }
        }

        return resultCode;
    }

    public String deleteAccount(xrplAccount account, Address destination, XrpCurrencyAmount reserveIncrementPrice) throws JsonRpcClientErrorException, JsonProcessingException, InterruptedException {

        AccountInfoResult accountInfoResult = getAccountInfos(account.getrAddress());
        UnsignedInteger sequence = accountInfoResult.accountData().sequence();

        // Request current fee information from rippled
        XrpCurrencyAmount openLedgerFee = this.rippledClient.fee().drops().openLedgerFee();

        // Get the latest validated ledger index
        LedgerIndex latestIndex = getLatestLedgerIndex();
        // LastLedgerSequence is the current ledger index + 4
        UnsignedInteger lastLedgerSequence = latestIndex.plus(UnsignedInteger.valueOf(4)).unsignedIntegerValue();

        AccountDelete deleteAcc = AccountDelete.builder()
                .account(account.getrAddress())
                .sequence(sequence)
                .fee(openLedgerFee.plus(reserveIncrementPrice))
                .destination(destination)
                .signingPublicKey(account.getRandomKeyPair().publicKey())
                .build();

        // Construct a SignatureService to delete the account
        SignatureService<PrivateKey> signatureService = new BcSignatureService();
        SingleSignedTransaction<AccountDelete> deletePayment =
                signatureService.sign(account.getRandomKeyPair().privateKey(), deleteAcc);
        //Can be generic
        System.out.println("Delete Result: " + deletePayment.signedTransaction());

        // Submit the Payment
        this.rippledClient.submit(deletePayment);

        // Wait for validation
        TransactionResult<AccountDelete> transactionResult = getTransactionResult(deletePayment.hash(), AccountDelete.class, lastLedgerSequence);

        String resultCode = "Unknown";
        if (transactionResult != null) {
            if (transactionResult.metadata().isPresent()) {
                TransactionMetadata metadata = transactionResult.metadata().get();
                resultCode = metadata.transactionResult();
                if(resultCode == "tesSUCCESS"){
                    System.out.println("Account " + destination + " was successfully deleted !");
                } else {
                    System.out.println("Account deletion failed !");
                }
            } else {
                System.out.println("No metadata available for the transaction.");
            }
        }
        return resultCode;
    }
}