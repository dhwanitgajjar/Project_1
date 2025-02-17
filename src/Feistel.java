import java.io.*;
import java.nio.ByteBuffer;
import java.nio.file.*;
import java.util.Arrays;

public class Feistel {
    private static final int BLOCK_SIZE = 16;
    private static final int ROUNDS = 10;
    private static final long MULTIPLIER = 0xA3B2C1L;

    public static void main(String[] args) throws IOException {
        if (args.length < 4 || (!args[0].equals("-e") && !args[0].equals("-d"))) {
            System.err.println("Usage: feistel -e password plaintext ciphertext");
            System.err.println("       feistel -d password ciphertext plaintext");
            System.exit(1);
        }

        String mode = args[0];
        String password = args[1];
        String inputFile = args[2];
        String outputFile = args[3];

        byte[] inputBytes = Files.readAllBytes(Paths.get(inputFile));
        byte[] processed;

        long key = sdbmHash(password.getBytes());
        long[] roundKeys = generateRoundKeys(key);

        if (mode.equals("-e")) {
            byte[] padded = pad(inputBytes);
            processed = processBlocks(padded, roundKeys, true);
        } else {
            byte[] decrypted = processBlocks(inputBytes, reverseKeys(roundKeys), false);
            processed = unpad(decrypted);
        }

        Files.write(Paths.get(outputFile), processed);
    }

    private static long[] generateRoundKeys(long key) {
        long[] keys = new long[ROUNDS];
        keys[0] = key;
        for (int i = 1; i < ROUNDS; i++)
            keys[i] = (keys[i-1] * 1103515245L + 12345L) & 0xFFFFFFFFFFFFFFFFL;
        return keys;
    }

    private static long[] reverseKeys(long[] keys) {
        long[] reversed = new long[ROUNDS];
        for (int i = 0; i < ROUNDS; i++)
            reversed[i] = keys[ROUNDS - 1 - i];
        return reversed;
    }

    private static byte[] processBlocks(byte[] data, long[] keys, boolean encrypt) {
        byte[] result = new byte[data.length];
        for (int i = 0; i < data.length; i += BLOCK_SIZE) {
            byte[] block = Arrays.copyOfRange(data, i, i + BLOCK_SIZE);
            ByteBuffer buffer = ByteBuffer.wrap(block);
            long left = buffer.getLong();
            long right = buffer.getLong();

            for (long roundKey : keys) {
                long temp = right;
                right = left ^ roundFunction(right, roundKey);
                left = temp;
            }

            ByteBuffer out = ByteBuffer.allocate(BLOCK_SIZE);
            out.putLong(right);
            out.putLong(left);
            System.arraycopy(out.array(), 0, result, i, BLOCK_SIZE);
        }
        return result;
    }

    private static long roundFunction(long input, long key) {
        long temp = (input ^ key) * MULTIPLIER;
        return (temp >>> 23) | (temp << 41);
    }

    private static byte[] pad(byte[] data) {
        int padLen = BLOCK_SIZE - (data.length % BLOCK_SIZE);
        padLen = padLen == 0 ? BLOCK_SIZE : padLen;
        byte[] padded = Arrays.copyOf(data, data.length + padLen);
        Arrays.fill(padded, data.length, padded.length, (byte) padLen);
        return padded;
    }

    private static byte[] unpad(byte[] data) {
        int padLen = data[data.length - 1] & 0xFF;
        return Arrays.copyOf(data, data.length - padLen);
    }

    private static long sdbmHash(byte[] data) {
        long hash = 0;
        for (byte b : data) {
            int c = b & 0xFF;
            hash = c + (hash << 6) + (hash << 16) - hash;
        }
        return hash;
    }
}