Project 1 – Encryption & Decryption Suite

This project implements three different ciphers as part of a class assignment. The programs are written in Java and consist of the following parts:

Scrypt: A stream cipher that uses a linear congruential generator (LCG) with a hashed password (using the sdbm hash) to produce a pseudorandom keystream. Encryption and decryption are performed by XORing the plaintext with the keystream.
Vcrypt: An enhanced stream cipher that incorporates an initialization vector (IV). The IV (8 bytes, written in little-endian order) is generated randomly and XORed with the hashed password to produce the seed for the LCG. The IV is written at the beginning of the ciphertext file so that decryption can recover it.
Feistel: A block cipher implemented using a 10-round Feistel network on 128-bit (16-byte) blocks with PKCS#7 padding. The password is converted to a 64-bit key using the sdbm hash. Ten (10) round keys are generated using the LCG formula with the constants:
Multiplier: 1103515245
Increment: 12345
The encryption process splits each block into left (L) and right (R) halves and performs rounds as follows:

For each round (0 to 9):
New R = L XOR F(R, round_key[i])
New L = original R
After 10 rounds, the halves are swapped to produce the final ciphertext block.
The round function is defined as:

temp = (input XOR round_key) * 0xa3b2c1 – 0xc2ff
return (((temp >> 23) | (temp << 41)) + temp)
For decryption, the round keys are applied in reverse order and the initial final-swap is undone.
File Structure

Scrypt.java: Implements the stream cipher (Part 1).
Vcrypt.java: Implements the stream cipher with an initialization vector (Part 2).
Feistel.java: Implements the Feistel block cipher with padding (Part 3).
Makefile: (Optional) A Makefile to compile all the programs.
README.md: This file.
Requirements

Java Development Kit (JDK) 8 or higher
A command-line environment (Terminal on macOS/Linux or Command Prompt on Windows)
Compilation

You can compile the Java files individually or use the provided Makefile (if available). To compile individually:

javac Scrypt.java
javac Vcrypt.java
javac Feistel.java
Or, if you have the Makefile:

make
Usage

Scrypt (Stream Cipher without IV)
Encrypt or decrypt by running the same command:

java Scrypt [-D] password plaintext_file ciphertext_file
The optional -D flag enables debug mode, printing keystream and XOR details.
Vcrypt (Stream Cipher with IV)
Encryption:

java Vcrypt -e password plaintext_file ciphertext_file
Decryption:

java Vcrypt -d password ciphertext_file plaintext_file
Vcrypt generates an 8-byte initialization vector (IV) and writes it at the beginning of the ciphertext file. The same IV is recovered during decryption.
Feistel (Block Cipher)
Encryption:

java Feistel -e [-D] [-k] password plaintext_file ciphertext_file
Decryption:

java Feistel -d [-D] [-k] password ciphertext_file plaintext_file
The -D flag enables debugging output showing the intermediate round values.
The -k flag displays the generated round keys.
For encryption, the program automatically appends an 'x' to the provided password before hashing (per assignment spec).
Testing

Create a sample plaintext file:
echo "Hello, this is a test file!" > plaintext.txt
Encrypt the file using any of the programs (e.g., Feistel):
java Feistel -e mypassword plaintext.txt encrypted.bin
Decrypt the ciphertext:
java Feistel -d mypassword encrypted.bin decrypted.txt
Compare the original and decrypted files:
cmp plaintext.txt decrypted.txt
If there is no output, the files are identical.
Use Debugging Options:
To view detailed debugging information:
java Scrypt -D mypassword plaintext.txt encrypted.bin
java Feistel -e -D -k mypassword plaintext.txt encrypted.bin
Error Handling

The programs print usage instructions if the required arguments are missing.
They handle file reading/writing exceptions and display appropriate error messages.
Ensure that the input file exists and is readable, and that the output file can be created.
Notes

Testing on Various Inputs:
Test the programs with empty files, files shorter than one block, files exactly one block in length, and larger files.
Comparing Files:
Use the cmp command on Linux/macOS or a file comparison tool on Windows to ensure that decrypted files match the original plaintext exactly.
