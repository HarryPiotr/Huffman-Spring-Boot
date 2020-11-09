package tools;

import org.jgrapht.graph.DefaultEdge;

public class BinaryTreeEdge extends DefaultEdge {

    private String label;

    public BinaryTreeEdge(boolean dir) {
        if(dir) this.label = "0";
        else this.label = "1";
    }

    public String getLabel() {
        return label;
    }

    @Override
    public String toString() {
        return label;
    }
}