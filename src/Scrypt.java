import java.io.*;
import java.nio.charset.StandardCharsets;

public class scrypt {
    public static void main(String[] args) {
        if (args.length < 3 || args.length > 4) {
            System.err.println("Usage: scrypt [-D] password plaintext ciphertext");
            System.exit(1);
        }

        boolean debug = false;
        int argIndex = 0;

        // Check for -D flag
        if (args[argIndex].equals("-D")) {
            debug = true;
            argIndex++;
        }

        String password = args[argIndex];
        String inputFile = args[argIndex + 1];
        String outputFile = args[argIndex + 2];

        try {
            byte[] passwordBytes = password.getBytes(StandardCharsets.UTF_8);
            long seed = sdbmHash(passwordBytes);
            int current = (int) seed & 0xFF;

            if (debug) {
                System.out.println("Debug Mode ON");
                System.out.println("Using seed: " + seed);
            }

            try (InputStream in = new BufferedInputStream(new FileInputStream(inputFile));
                    OutputStream out = new BufferedOutputStream(new FileOutputStream(outputFile))) {

                int b;
                while ((b = in.read()) != -1) {
                    current = (1103515245 * current + 12345) & 0xFF; // ✅ Fixed LCG formula
                    if (debug) {
                        System.out.printf("XOR Byte: 0x%02X -> 0x%02X%n", b, b ^ current);
                    }
                    out.write(b ^ current);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
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
