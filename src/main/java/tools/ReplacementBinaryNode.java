package tools;

public class ReplacementBinaryNode extends BinaryNode {

    private BinaryNode left;
    private BinaryNode right;

    public ReplacementBinaryNode(BinaryNode n1, BinaryNode n2, byte b) {
        super(b);
        this.setOccurrences(n1.getOccurrences() + n2.getOccurrences());
        this.setSymbol(b);
        this.left = n1;
        this.right = n2;
        n1.setParent(this);
        n2.setParent(this);
    }

    public BinaryNode getLeft() {return left; }
    public void setLeft(BinaryNode left) {
        this.left = left;
    }

    public BinaryNode getRight() {
        return right;
    }
    public void setRight(BinaryNode right) {
        this.right = right;
    }

}