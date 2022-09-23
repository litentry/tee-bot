package com.litentry.litbot.TEEBot.polkadot.SS58;

/**
 * @see <a href="https://github.com/paritytech/substrate/wiki/External-Address-Format-(SS58)">https://github.com/paritytech/substrate/wiki/External-Address-Format-(SS58)</a>
 */
public abstract class SS58Type {

    private byte value;

    private SS58Type(byte value) {
        this.value = value;
    }

    private SS58Type(int value) {
        //values starting from 64 are reserved by the spec at this moment
        if (value < 0 || value >= 64) {
            throw new IllegalArgumentException("Unsupported value: " + value);
        }
        this.value = (byte) value;
    }

    public byte getValue() {
        return value;
    }

    public static class Network extends SS58Type {

        public static Network POLKADOT = new Network(0b00000000); //Prefix: 0
        public static Network KUSAMA = new Network(0b00000010); //Prefix: 2
        public static Network PLASM = new Network(0b00000101); //Prefix: 5
        public static Network BIFROST = new Network(0b00000110); //Prefix: 6
        public static Network EDGEWARE = new Network(0b00000111); //Prefix: 7
        public static Network ACALA = new Network(0b00010000); //Prefix: 16
        public static Network KULUPU = new Network(0b00001010); //Acala Mainnet, Prefix: 10
        public static Network STAFI = new Network(0b00010100); //Prefix: 20
        public static Network LITENTRY = new Network(0b000011111); //Prefix: 31
        public static Network SUBSTRATE = new Network(0b00101010); //Westend (Prefix: 42)

        private static Network[] ALL = { POLKADOT, KUSAMA, PLASM, BIFROST, EDGEWARE, ACALA, KULUPU, STAFI, LITENTRY, SUBSTRATE };

        private Network(int value) {
            super(value);
        }

        public static Network from(byte value) {
            for (Network n : ALL) {
                if (n.getValue() == value) {
                    return n;
                }
            }
            throw new IllegalArgumentException("Unsupported network: " + value);
        }
    }

    public static class Key extends SS58Type {

        private Key(int value) {
            super(value);
        }

        public static Key SR25519 = new Key(0b00110000);
        public static Key ED25519 = new Key(0b00110001);
        public static Key SECP256K1 = new Key(0b00110010);
    }

    public static class Custom extends SS58Type {

        public Custom(byte value) {
            super(value);
        }

        public Custom(int value) {
            super(value);
        }
    }
}
