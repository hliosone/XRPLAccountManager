package org.example.secure;

import org.example.secure.AccountManager;
import org.xrpl.xrpl4j.codec.addresses.AddressCodec;
import org.xrpl.xrpl4j.crypto.keys.KeyPair;
import org.xrpl.xrpl4j.crypto.keys.Seed;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.XAddress;

import java.util.regex.Pattern;

public class xrplAccount {

        public xrplAccount(final KeyPair keyPair) {
            if(keyPair == null){ System.out.println("Creating account ..."); }
            this.randomKeyPair = (keyPair == null ? Seed.ed25519Seed().deriveKeyPair() : keyPair);
            this.rAddress = randomKeyPair.publicKey().deriveAddress();
            this.xAddress = AddressCodec.getInstance().classicAddressToXAddress(this.rAddress, true);
            if(keyPair == null){
                System.out.println("Created account : " + this.rAddress );
            }
        }

        public Address getrAddress(){
            return this.rAddress;
        }

        public XAddress getxAddress(){
            return this.xAddress;
        }

        KeyPair getRandomKeyPair(){
            return this.randomKeyPair;
        }

        public static Pattern getAddressPattern(){
            return ADDRESS_PATTERN;
        }

        private static final Pattern ADDRESS_PATTERN = Pattern.compile("^[rR][1-9A-HJ-NP-Za-km-z]{24,34}$");
        private final XAddress xAddress;
        private final Address rAddress;
        private final KeyPair randomKeyPair;
}
