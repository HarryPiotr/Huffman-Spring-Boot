package tools;

import java.util.Comparator;

public class Node {

    private Node parent;
    private long occurrences = 0;
    private char symbol;
    private String codingSequence;
    private String whiteSpace;
    private boolean isWhiteSpace = false;

    public static Comparator<Node> NodeOccurancesComparator = new Comparator<Node>() {

        public int compare(Node n1, Node n2) {
            int verdict = n1.compareTo(n2);
            if(verdict == 0) {
                verdict = n1.compareToSymbol(n2);
            }
            return verdict;
        }
    };

    public Node(char c) {
        setOccurrences(1);
        if(c == 10 || c == 13 || c == 32 || c == 9) {
            isWhiteSpace = true;
            switch(c) {
                case 10:
                    whiteSpace = "NL";
                    break;
                case 13:
                    whiteSpace = "CR";
                    break;
                case 32:
                    whiteSpace = "SP";
                    break;
                case 9:
                    whiteSpace = "HT";
                    break;
            }
        }
        setSymbol(c);
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

    public char getSymbol() {
        return symbol;
    }

    public void setSymbol(char symbol) {
        this.symbol = symbol;
    }

    public String toString() {
        return "" + symbol + " (" + occurrences + ") [" + this.getCodingSequence() + "]";
    }

    public static Comparator<Node> NodeOccurancesComparatorDescending = new Comparator<Node>() {

        public int compare(Node n1, Node n2) {
            return n1.compareTo(n2);
        }
    };

    public int compareTo(Node n) {
        return this.occurrences < n.occurrences ? -1 : this.occurrences == n.occurrences ? 0 : 1;
    }

    public int compareToSymbol(Node n) {
        return this.symbol < n.symbol ? -1 : this.symbol == n.symbol ? 0 : 1;
    }

    public Node getParent() {
        return parent;
    }

    public void setParent(Node parent) {
        this.parent = parent;
    }

    public String print(String prefix) {
        return prefix + "" + this.getSymbol() + "(" + this.getOccurrences() + ")\n";
    }

    public String getCodingSequence() {
        return codingSequence;
    }

    public void setCodingSequence(String codingSequence) {
        this.codingSequence = codingSequence;
    }

    public void setIsWhiteSpace(Boolean isWhiteSpace) { this.isWhiteSpace = isWhiteSpace; }

    public boolean getIsWhiteSpace() { return isWhiteSpace; }

    public String getWhiteSpace() {
        return whiteSpace;
    }
}