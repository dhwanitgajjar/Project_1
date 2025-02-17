import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;

public class vcrypt {
    public static void main(String[] args) {
        if (args.length < 4 || (!args[0].equals("-e") && !args[0].equals("-d"))) {
            System.err.println("Usage: vcrypt -e password plaintext ciphertext");
            System.err.println("       vcrypt -d password ciphertext plaintext");
            System.exit(1);
        }
        String mode = args[0];
        String password = args[1];
        String inputFile = args[2];
        String outputFile = args[3];

        try {
            byte[] passwordBytes = password.getBytes(StandardCharsets.UTF_8);
            long passwordHash = sdbmHash(passwordBytes);
            if (mode.equals("-e")) { // Encrypt Mode
                SecureRandom random = new SecureRandom();
                byte[] ivBytes = new byte[8]; // Exactly 8 bytes
                random.nextBytes(ivBytes);
                long iv = ByteBuffer.wrap(ivBytes).order(ByteOrder.LITTLE_ENDIAN).getLong();
                long seed = passwordHash ^ iv;
                try (OutputStream out = new BufferedOutputStream(new FileOutputStream(outputFile))) {
                    out.write(ivBytes); // Write IV only once at the beginning
                    processData(inputFile, out, seed);
                }
            } else { // Decrypt Mode
                try (InputStream in = new BufferedInputStream(new FileInputStream(inputFile));
                        OutputStream out = new BufferedOutputStream(new FileOutputStream(outputFile))) {
                    byte[] ivBytes = new byte[8]; // Read exactly 8 bytes for IV
                    if (in.read(ivBytes) != 8)
                        throw new IOException("Invalid IV size");
                    long iv = ByteBuffer.wrap(ivBytes).order(ByteOrder.LITTLE_ENDIAN).getLong();
                    long seed = passwordHash ^ iv;
                    processStream(in, out, (int) seed);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static void processData(String inputFile, OutputStream out, long seed) throws IOException {
        int current = (byte) seed & 0xFF;
        try (InputStream in = new FileInputStream(inputFile)) {
            processStream(in, out, current);
        }
    }

    private static void processStream(InputStream in, OutputStream out, int seed) throws IOException {
        int current = seed;
        int b;
        while ((b = in.read()) != -1) {
            current = (109 * current + 57) % 256;
            out.write(b ^ current);
        }
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
