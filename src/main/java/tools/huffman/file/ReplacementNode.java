package tools.huffman.file;

public class ReplacementNode extends Node {

    private Node left;
    private Node right;

    public ReplacementNode(Node n1, Node n2, byte b) {
        super(b);
        this.setOccurrences(n1.getOccurrences() + n2.getOccurrences());
        this.setSymbol(b);
        this.left = n1;
        this.right = n2;
        n1.setParent(this);
        n2.setParent(this);
    }

    public Node getLeft() {return left; }
    public void setLeft(Node left) {
        this.left = left;
    }

    public Node getRight() {
        return right;
    }
    public void setRight(Node right) {
        this.right = right;
    }

}