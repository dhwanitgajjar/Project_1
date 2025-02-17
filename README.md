# Computer Security Project 1 – Encryption & Decryption Suite

This project implements three different ciphers as part of a class assignment. The programs are written in Java and consist of the following parts:

1. **Scrypt:**  
   A stream cipher that uses a linear congruential generator (LCG) with a hashed password (using the sdbm hash) to produce a pseudorandom keystream. Encryption and decryption are performed by XORing the plaintext with the keystream.

2. **Vcrypt:**  
   An enhanced stream cipher that incorporates an initialization vector (IV). The IV (8 bytes, written in little-endian order) is generated randomly and XORed with the hashed password to produce the seed for the LCG. The IV is written at the beginning of the ciphertext file so that decryption can recover it.

3. **Feistel:**  
   A block cipher implemented using a 10-round Feistel network on 128-bit (16-byte) blocks with PKCS#7 padding. The password is converted to a 64-bit key using the sdbm hash. Ten (10) round keys are generated using the LCG formula with the constants:
   - **Multiplier:** 1103515245  
   - **Increment:** 12345  
   
   The encryption process splits each block into left (L) and right (R) halves and performs rounds as follows:
   - **For each round (0 to 9):**  
     - New R = L XOR F(R, round_key[i])  
     - New L = original R  
   - After 10 rounds, the halves are swapped to produce the final ciphertext block.
   
   The round function is defined as: temp = (input XOR round_key) * 0xa3b2c1 – 0xc2ff return (((temp >> 23) | (temp << 41)) + temp)


For decryption, the round keys are applied in reverse order and the initial final-swap is undone.

---

## File Structure

- **Scrypt.java:** Implements the stream cipher (Part 1).
- **Vcrypt.java:** Implements the stream cipher with an initialization vector (Part 2).
- **Feistel.java:** Implements the Feistel block cipher with padding (Part 3).
- **Makefile:** (Optional) A Makefile to compile all the programs.
- **README.md:** This file.

---

## Requirements

- Java Development Kit (JDK) 8 or higher
- A command-line environment (Terminal on macOS/Linux or Command Prompt on Windows)

---

## Compilation

You can compile the Java files individually or use the provided Makefile (if available).

**To compile individually:**

```bash
javac Scrypt.java
javac Vcrypt.java
javac Feistel.java

Or, if you have the Makefile: make

Testing

Create a sample plaintext file:
echo "Hello, this is a test file!" > plaintext.txt
Encrypt the file using any of the programs (e.g., Feistel):
java Feistel -e mypassword plaintext.txt encrypted.bin
Decrypt the ciphertext:
java Feistel -d mypassword encrypted.bin decrypted.txt
Compare the original and decrypted files:
cmp plaintext.txt decrypted.txt
If there is no output, the files match.
Use Debugging Options:
To view detailed debugging information:
java Scrypt -D mypassword plaintext.txt encrypted.bin
java Feistel -e -D -k mypassword plaintext.txt encrypted.bin


