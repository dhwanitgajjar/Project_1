import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
    
public class feistel {

    private static final int BLOCK_LENGTH = 16;
    private static final int NUM_ROUNDS = 10;
    private static final long KEY_MULTIPLIER = 1103515245L;
    private static final long KEY_INCREMENT = 12345L;

    public static void main(String[] arguments) {
        if (arguments.length != 4) {
            System.out.println("Usage:");
            System.out.println("  Encrypt: java feistel -e <password> <inputFile> <outputFile>");
            System.out.println("  Decrypt: java feistel -d <password> <inputFile> <outputFile>");
            return;
        }

        String mode = arguments[0];
        String pwd = arguments[1];
        String inPath = arguments[2];
        String outPath = arguments[3];

        try {
            if ("-e".equals(mode)) {
                performEncryption(pwd, inPath, outPath);
            } else if ("-d".equals(mode)) {
                performDecryption(pwd, inPath, outPath);
            } else {
                System.out.println("Error: Invalid mode. Use '-e' to encrypt or '-d' to decrypt.");
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private static void performEncryption(String password, String inputFile, String outputFile) throws IOException {
        long baseKey = computeHash(password);
        long[] subKeys = createRoundKeys(baseKey);

        byte[] plainData = loadFile(inputFile);
        byte[] dataWithPadding = addPadding(plainData);

        ByteArrayOutputStream cipherStream = new ByteArrayOutputStream();

        for (int pos = 0; pos < dataWithPadding.length; pos += BLOCK_LENGTH) {
            byte[] currentBlock = Arrays.copyOfRange(dataWithPadding, pos, pos + BLOCK_LENGTH);
            byte[] encBlock = encryptFeistelBlock(currentBlock, subKeys);
            cipherStream.write(encBlock);
        }
        saveFile(outputFile, cipherStream.toByteArray());
    }

    private static void performDecryption(String password, String inputFile, String outputFile) throws IOException {
        long baseKey = computeHash(password);
        long[] subKeys = createRoundKeys(baseKey);

        byte[] cipherData = loadFile(inputFile);
        ByteArrayOutputStream plainStream = new ByteArrayOutputStream();

        for (int pos = 0; pos < cipherData.length; pos += BLOCK_LENGTH) {
            byte[] currentBlock = Arrays.copyOfRange(cipherData, pos, pos + BLOCK_LENGTH);
            byte[] decBlock = decryptFeistelBlock(currentBlock, subKeys);
            plainStream.write(decBlock);
        }

        byte[] paddedPlain = plainStream.toByteArray();
        byte[] originalPlain = removePadding(paddedPlain);
        saveFile(outputFile, originalPlain);
    }

    private static byte[] encryptFeistelBlock(byte[] block, long[] keys) {
        long leftHalf = toLong(block, 0);
        long rightHalf = toLong(block, 8);

        for (int round = 0; round < NUM_ROUNDS; round++) {
            long temp = rightHalf;
            rightHalf = leftHalf ^ feistelRound(rightHalf, keys[round]);
            leftHalf = temp;
        }
        return combineLongs(leftHalf, rightHalf);
    }

    private static byte[] decryptFeistelBlock(byte[] block, long[] keys) {
        long leftHalf = toLong(block, 0);
        long rightHalf = toLong(block, 8);

        for (int round = NUM_ROUNDS - 1; round >= 0; round--) {
            long temp = leftHalf;
            leftHalf = rightHalf ^ feistelRound(leftHalf, keys[round]);
            rightHalf = temp;
        }
        return combineLongs(leftHalf, rightHalf);
    }

    private static long feistelRound(long value, long roundKey) {
        long mixed = (value ^ roundKey) * 0xa3b2c1L;
        return (mixed >>> 23) | (mixed << 41);
    }

    private static long[] createRoundKeys(long seed) {
        long[] keys = new long[NUM_ROUNDS];
        keys[0] = seed;
        for (int i = 1; i < NUM_ROUNDS; i++) {
            keys[i] = (keys[i - 1] * KEY_MULTIPLIER + KEY_INCREMENT) & 0xffffffffffffffffL;
        }
        return keys;
    }

    private static long computeHash(String input) {
        long hash = 0;
        for (char ch : input.toCharArray()) {
            hash = ch + (hash << 6) + (hash << 16) - hash;
        }
        return hash;
    }

    private static byte[] loadFile(String path) throws IOException {
        File file = new File(path);
        byte[] content = new byte[(int) file.length()];
        try (FileInputStream inStream = new FileInputStream(file)) {
            if (inStream.read(content) != content.length) {
                throw new IOException("Error reading file completely: " + path);
            }
        }
        return content;
    }

    private static void saveFile(String path, byte[] data) throws IOException {
        try (FileOutputStream outStream = new FileOutputStream(path)) {
            outStream.write(data);
        }
    }

    private static byte[] addPadding(byte[] input) {
        int padLength = BLOCK_LENGTH - (input.length % BLOCK_LENGTH);
        byte[] padded = new byte[input.length + padLength];
        System.arraycopy(input, 0, padded, 0, input.length);
        Arrays.fill(padded, input.length, padded.length, (byte) padLength);
        return padded;
    }

    private static byte[] removePadding(byte[] input) {
        int padLength = input[input.length - 1] & 0xFF;
        if (padLength < 1 || padLength > BLOCK_LENGTH || padLength > input.length) {
            throw new IllegalArgumentException("Invalid padding encountered: " + padLength);
        }
        for (int i = input.length - padLength; i < input.length; i++) {
            if (input[i] != (byte) padLength) {
                throw new IllegalArgumentException("Padding corruption detected.");
            }
        }
        return Arrays.copyOfRange(input, 0, input.length - padLength);
    }

    private static long toLong(byte[] arr, int startIndex) {
        return ByteBuffer.wrap(arr, startIndex, 8).order(ByteOrder.BIG_ENDIAN).getLong();
    }

    private static byte[] combineLongs(long first, long second) {
        ByteBuffer buffer = ByteBuffer.allocate(BLOCK_LENGTH).order(ByteOrder.BIG_ENDIAN);
        buffer.putLong(first);
        buffer.putLong(second);
        return buffer.array();
    }
}
