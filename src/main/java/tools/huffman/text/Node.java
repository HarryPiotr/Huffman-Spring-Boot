package tools.huffman.text;

import java.util.Comparator;

public class Node {

    private Node parent;
    private long occurrences = 0;
    private String symbol;
    private String prettySymbol;
    private String codingSequence;

    public static Comparator<Node> NodeOccurancesComparator = new Comparator<Node>() {

        public int compare(Node n1, Node n2) {
            int verdict = n1.compareTo(n2);
            if (verdict == 0) {
                verdict = n1.compareToSymbol(n2);
            }
            return verdict;
        }
    };

    public Node(String s) {
        setOccurrences(1);
        symbol = s;
        prettySymbol = "";
        for (char c : s.toCharArray()) {
            if (c == 10) prettySymbol += "[NL]";
            else if (c == 13) prettySymbol += "[CR]";
            else if (c == 32) prettySymbol += "[SP]";
            else if (c == 9) prettySymbol += "[HT]";
            else if (c == 91) prettySymbol += "[OB]";
            else if (c == 93) prettySymbol += "[CB]";
            else prettySymbol += c;
        }
    }

    public int compareTo(Node n) {
        return this.occurrences < n.occurrences ? -1 : this.occurrences == n.occurrences ? 0 : 1;
    }

    public int compareToSymbol(Node n) {
        if(this.symbol.length() != n.symbol.length()) {
            if(this.symbol.length() < n.symbol.length()) return -1;
            else return 1;
        }
        else {
            for(int i = 0; i < symbol.length(); i++) {
                if(this.symbol.charAt(i) < n.symbol.charAt(i)) return -1;
                else if (this.symbol.charAt(i) > n.symbol.charAt(i)) return 1;
                else continue;
            }
        }
        return 0;
    }

    public long getOccurrences() {
        return occurrences;
    }

    public void setOccurrences(long occurences) {
        this.occurrences = occurences;
    }

    public void incrementOccurrences() {
        occurrences++;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String toString() {
        return "" + symbol + " (" + occurrences + ") [" + this.getCodingSequence() + "]";
    }

    public Node getParent() {
        return parent;
    }

    public void setParent(Node parent) {
        this.parent = parent;
    }

    public String getCodingSequence() {
        return codingSequence;
    }

    public void setCodingSequence(String codingSequence) {
        this.codingSequence = codingSequence;
    }

    public String getPrettySymbol() {
        return prettySymbol;
    }

    public void setPrettySymbol(String prettySymbol) {
        this.prettySymbol = prettySymbol;
    }
}