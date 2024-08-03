package org.example;

import org.xrpl.xrpl4j.codec.addresses.AddressCodec;
import org.xrpl.xrpl4j.crypto.keys.KeyPair;
import org.xrpl.xrpl4j.crypto.keys.Seed;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.XAddress;

import java.util.regex.Pattern;

public class xrplAccount {

        public xrplAccount() {
            System.out.println("Creating new account ...");
            this.randomKeyPair = Seed.ed25519Seed().deriveKeyPair();
            this.rAddress = randomKeyPair.publicKey().deriveAddress();
            this.xAddress = AddressCodec.getInstance().classicAddressToXAddress(this.rAddress, true);
            System.out.println("New account rAddress is : " + this.rAddress);
        }

        public Address getrAddress(){
            return this.rAddress;
        }

        public XAddress getxAddress(){
            return this.xAddress;
        }

        public KeyPair getRandomKeyPair(){
            return this.randomKeyPair;
        }

        public static Pattern getAddressPattern(){
            return ADDRESS_PATTERN;
        }

        private static final Pattern ADDRESS_PATTERN = Pattern.compile("^[rR][1-9A-HJ-NP-Za-km-z]{24,34}$");
        private XAddress xAddress;
        private Address rAddress;
        private KeyPair randomKeyPair;
}
