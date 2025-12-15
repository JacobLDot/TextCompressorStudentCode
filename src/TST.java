/**
 * The {@code TST} class encodes a ternary search trie for use with LZW compression.
 *
 * @author Zach Blick
 * @Credit: Robert Sedgewick
 *
 * @Date: Written lovingly in 2024 for Adventures in Algorithms at Menlo School in Atherton, CA.
 */
public class TST {

    public static final int EMPTY = -1;
    private Node root;

    /**
     * Each Node contains its own charater, as well as references to its three children.
     * If this Node's character is the terminus of a coded word, it will also contain
     * the relevant code.
     */
    private class Node {
        Node left, mid, right;
        char c;
        int code;
    }

    /**
     * Recursively inserts the given word-code pair into the TST.
     * @param s The word to insert.
     * @param code The code for the given word.
     */
    public void insert(String s, int code) {
        root = insert(s, root, code, 0);
    }

    public Node insert(String s, Node n, int code, int depth) {
        char current = s.charAt(depth);
        if(n == null) {
            n = new Node();
            n.c = current;
        }
        if (current < n.c)
            n.left = insert(s, n.left, code, depth);
        else if (current > n.c)
            n.right = insert(s, n.right, code, depth);
        else if (depth < s.length() - 1) {
            n.mid = insert(s, n.mid, code, depth + 1);
        }
        else n.code = code;
        return n;
    }

    public String getLongestPrefix(String s) {
        return getLongestPrefix(s, 0);
    }

    /**
     * Returns the longest prefix in the TST that matches the given substring of s starting at index start.
     * @param s The target word, from which the substring is constructed.
     * @param start The starting index of the substring.
     * @return a String of all characters that match the given substring.
     * If no characters match, the empty String is returned.
     */
    public String getLongestPrefix(String s,  int start) {
        return prefix(s, root, start, "");
    }

    private String prefix(String s, Node n, int depth, String prefix) {
        if (n == null) return prefix;

        char current = s.charAt(depth);
        if (current < n.c)
            return prefix(s, n.left, depth, prefix);
        else if (current > n.c)
            return prefix(s, n.right, depth, prefix);
        else if (depth < s.length() - 1)
            return prefix(s, n.mid, depth + 1, prefix + current);
        else return prefix + current;
    }


    /**
     * Looks up the given String in the TST, returning the code associated with the word.
     * @param s The target String
     * @return the correcponding code. If no code is associated, or if the target word is not
     * present, EMPTY is returned.
     */
    public int lookup(String s) {
        return lookup(s, root, 0);
    }

    public int lookup(String s, Node n, int depth) {
        if (n == null) return EMPTY;

        char current = s.charAt(depth);
        if (current < n.c)
            return lookup(s, n.left, depth);
        else if (current > n.c)
            return lookup(s, n.right, depth);
        else if (depth < s.length() - 1)
            return lookup(s, n.mid, depth + 1);
        else return n.code;

    }

    /**
     * Recursively prints the TST (using a preorder traversal).
     */
    public void print() {
        printTST(root, "");
    }

    private void printTST(Node n, String s) {
        if (n == null) {
            return;
        }

        // If this node ends a word, print it out!
        s += n.c;
        if (n.code != EMPTY) System.out.println(s);

        printTST(n.left, s);
        printTST(n.mid, s);
        printTST(n.right, s);
    }
}
