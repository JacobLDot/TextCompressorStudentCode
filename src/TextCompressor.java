/******************************************************************************
 *  Compilation:  javac TextCompressor.java
 *  Execution:    java TextCompressor - < input.txt   (compress)
 *  Execution:    java TextCompressor + < input.txt   (expand)
 *  Dependencies: BinaryIn.java BinaryOut.java
 *  Data files:   abra.txt
 *                jabberwocky.txt
 *                shakespeare.txt
 *                virus.txt
 *
 *  % java DumpBinary 0 < abra.txt
 *  136 bits
 *
 *  % java TextCompressor - < abra.txt | java DumpBinary 0
 *  104 bits    (when using 8-bit codes)
 *
 *  % java DumpBinary 0 < alice.txt
 *  1104064 bits
 *  % java TextCompressor - < alice.txt | java DumpBinary 0
 *  480760 bits
 *  = 43.54% compression ratio!
 ******************************************************************************/

/**
 *  The {@code TextCompressor} class provides static methods for compressing
 *  and expanding natural language through textfile input.
 *
 *  @author Zach Blick, Jacob Lowe
 */
public class TextCompressor {
    private static final int EOF = 128;
    private static final int NUM_BITS = 12;
    private static final int MAX_CODES = 4096;

    private static void compress() {
        String sequence = BinaryStdIn.readString();
        TST tst = new TST();

        // Add each character from 0-127 into the TST
        for (int i = 0; i < EOF; i++) {
            tst.insert("" + (char) i, i);
        }

        // Extra codes start after EOF
        int nextCode = EOF + 1;
        int i = 0;

        // Finds the longest prefix of the suffix in the TST
        // Finds the code of that prefix, and compresses it
        while (i < sequence.length()) {
            String suffix = sequence.substring(i);
            String prefix = tst.getLongestPrefix(suffix);

            if (prefix.isEmpty()) {
                prefix = suffix.substring(0, 1);
            }

            int code = tst.lookup(prefix);

            // Something is wrong here
            if (code == TST.EMPTY) {
                break;
            }

            BinaryStdOut.write(code, NUM_BITS);

            int preLen = prefix.length();
            int seqLen = sequence.length();

            // Finds next character in the sequence
            // Adds the short sequence to the TST if not already present
            if (i + preLen < seqLen && nextCode < MAX_CODES) {
                char nextChar = sequence.charAt(i + preLen);
                String addNode = prefix + nextChar;

                if (tst.lookup(addNode) == TST.EMPTY) {
                    tst.insert(addNode, nextCode++);
                }
            }

            i += preLen;
        }

        BinaryStdOut.write(EOF, NUM_BITS);
        BinaryStdOut.close();
    }

    private static void expand() {
        String[] dictionary = new String[MAX_CODES];

        // Fill the dictionary with the initial 128 characters (0-127)
        for (int i = 0; i < EOF; i++) {
            dictionary[i] = "" + (char) i;
        }

        // Extra codes start after EOF
        int nextCode = EOF + 1;
        int i = 0;

        // If the text has reached the end of the file, end the decoding process
        int code = BinaryStdIn.readInt(NUM_BITS);
        if (code == EOF) {
            BinaryStdOut.close();
            return;
        }

        // Look up string for the first code
        String ascVal = dictionary[code];

        // Read the codes and expand the compressed text
        while (true) {
            BinaryStdOut.write(ascVal);
            int nextSeq = BinaryStdIn.readInt(NUM_BITS);
            if (nextSeq == EOF) {
                break;
            }

            // Next ascii value is in the dictionary
            String nextAscVal = dictionary[nextSeq];

            // Edge case if the lookahead code isn't known yet
            // The code isn't in the dictionary yet, but it has to start with the ascVal
            // It also adds the first char from the lookahead
            // When expanding, if we see a code that doesn't exist yet, we know it must be the next code.
            if (nextCode == nextSeq) {
                nextAscVal = ascVal + ascVal.charAt(0);
            }

            // Add the previous ascii value and the beginning of the next to the string
            if (nextCode < MAX_CODES) {
                dictionary[nextCode++] = ascVal + nextAscVal.charAt(0);
            }

            ascVal = nextAscVal;
        }

        BinaryStdOut.close();
    }

    public static void main(String[] args) {
        if      (args[0].equals("-")) compress();
        else if (args[0].equals("+")) expand();
        else throw new IllegalArgumentException("Illegal command line argument");
    }
}
