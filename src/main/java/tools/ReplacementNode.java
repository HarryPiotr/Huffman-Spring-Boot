package tools;

public class ReplacementNode extends Node {

    private Node left;
    private Node right;

    public ReplacementNode(Node n1, Node n2, char c) {
        super(c);
        this.setOccurrences(n1.getOccurrences() + n2.getOccurrences());
        this.setSymbol(c);
        this.left = n1;
        this.right = n2;
        n1.setParent(this);
        n2.setParent(this);
    }

    public Node getLeft() {
        return left;
    }

    public void setLeft(Node left) {
        this.left = left;
    }

    public Node getRight() {
        return right;
    }

    public void setRight(Node right) {
        this.right = right;
    }

    public String print(String prefix, String childrenPrefix) {
        String x = prefix + "" + this.getSymbol() + "(" + this.getOccurrences() + ")\n";
        if(right instanceof ReplacementNode) {
            x += ((ReplacementNode)right).print(childrenPrefix + "├── ", childrenPrefix + "│   ");
        }
        else x+= right.print(childrenPrefix + "├──");
        if(left instanceof ReplacementNode) {
            x += ((ReplacementNode)left).print(childrenPrefix + "└── ", childrenPrefix + "    ");
        }
        else x += left.print(childrenPrefix + "└──");

        return x;
    }
}
