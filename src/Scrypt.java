import java.io.*;
import java.nio.charset.StandardCharsets;

public class Scrypt {
    public static void main(String[] args) {
        if (args.length != 3) {
            System.err.println("Usage: scrypt password plaintext ciphertext");
            System.exit(1);
        }

        String password = args[0];
        String inputFile = args[1];
        String outputFile = args[2];

        try {
            byte[] passwordBytes = password.getBytes(StandardCharsets.UTF_8);
            long seed = sdbmHash(passwordBytes);
            int current = (byte) seed & 0xFF;

            try (InputStream in = new FileInputStream(inputFile);
                 OutputStream out = new FileOutputStream(outputFile)) {

                int b;
                while ((b = in.read()) != -1) {
                    current = (109 * current + 57) % 256;
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