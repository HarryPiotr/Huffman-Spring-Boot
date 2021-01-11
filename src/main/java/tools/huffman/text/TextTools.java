package tools.huffman.text;

import com.google.common.base.Splitter;
import javafx.util.Pair;
import org.jgrapht.Graph;
import org.jgrapht.graph.SimpleGraph;
import tools.BinaryTreeEdge;
import tools.huffman.file.BinaryNode;
import tools.huffman.file.ReplacementBinaryNode;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class TextTools {

    public static Pair<ArrayList<Node>, Double> countSymbols(String s, int l) {

        ArrayList<Node> nodes = new ArrayList<>();

        List<String> tokens = Splitter.fixedLength(l).splitToList(s);

        for(String st : tokens) {
            Node n = findNode(nodes, st);
            if(n == null) {
                n = new Node(st);
                nodes.add(n);
            }
            else n.incrementOccurrences();
        }

        sortNodeList(nodes);
        return new Pair<>(nodes, calculateEntropy(nodes, s.length(), l));
    }

    public static double calculateEntropy(ArrayList<Node> nodes, int lng, int l) {

        double p_sum = 0.0;
        double result = 0.0;
        for(Node n : nodes) {
            double p = ((double) n.getOccurrences()) / (Math.ceil((double) lng) / (double) l);
            p_sum += p;
            result += p * Math.log(p) / Math.log(2);
        }
        result *= -1;
        return result;
    }

    public static void sortNodeList(ArrayList<Node> nodes) {
        //Tutaj zaimplementować dowolny algorytm sortowania listy pod względem ilości wystąpień
        nodes.sort(Node.NodeOccurancesComparator);
    }

    public static ReplacementNode buildTree(ArrayList<Node> nodes) {
        //Tutaj zaimplementować budowanie drzewa Huffmana z obiektów typu Node i ReplacementNode
        int numberOfReplacements = 0;
        ArrayList<Node> nodesCopy = new ArrayList<>(nodes);
        if (nodesCopy.size() == 1) {
            ReplacementNode r = new ReplacementNode(nodesCopy.get(0), nodesCopy.get(0), "1");
            return r;
        }
        if (nodesCopy.size() == 0) {
            ReplacementNode r = new ReplacementNode(new Node("\0"), new Node("\0"), ("1"));
            return r;
        }
        while (nodesCopy.size() != 1) {
            nodesCopy.sort(Node.NodeOccurancesComparator);
            Node n1 = nodesCopy.get(0);
            Node n2 = nodesCopy.get(1);
            ReplacementNode r = new ReplacementNode(n1, n2, "" + ++numberOfReplacements);
            nodesCopy.remove(n1);
            nodesCopy.remove(n2);
            nodesCopy.add(r);
        }
        return (ReplacementNode) nodesCopy.get(0);
    }

    public static Graph<String, BinaryTreeEdge> generateTreeGraph(ReplacementNode root) {

        Graph<String, BinaryTreeEdge> graph = new SimpleGraph<>(BinaryTreeEdge.class);
        checkBranch(root, graph, false);
        return graph;
    }

    public static void checkBranch(Node root, Graph<String, BinaryTreeEdge> g, boolean dir) {

        if (root instanceof ReplacementNode) {
            g.addVertex("#" + root.getSymbol());
            if (root.getParent() != null)
                g.addEdge("#" + root.getParent().getSymbol(), "#" + root.getSymbol(), new BinaryTreeEdge(dir));
            checkBranch(((ReplacementNode) root).getLeft(), g, true);
            checkBranch(((ReplacementNode) root).getRight(), g, false);
        } else {
            String vertexName = root.getPrettySymbol();
            g.addVertex(vertexName);
            if (root.getParent() != null)
                g.addEdge("#" + root.getParent().getSymbol(), vertexName, new BinaryTreeEdge(dir));
        }
    }

    public static void createCodingSequences(Node node, String prefix) {
        if (!(node instanceof ReplacementNode)) node.setCodingSequence(prefix);
        else {
            ReplacementNode rnode = (ReplacementNode) node;
            createCodingSequences(rnode.getLeft(), prefix + "0");
            createCodingSequences(rnode.getRight(), prefix + "1");
        }
    }

    public static Pair<String, Integer> encodeText(ArrayList<Node> nodes, String s, int l) {
        ArrayList<Node> flippedNodes = new ArrayList<>(nodes);
        flippedNodes.sort(Node.NodeOccurancesComparatorDescending);

        List<String> tokens = Splitter.fixedLength(l).splitToList(s);

        StringBuilder buffer = new StringBuilder();
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        for (String st : tokens) {

            Node n = findNode(nodes, st);
            buffer.append(n.getCodingSequence());
            while (buffer.length() >= 8) {
                String seq = buffer.substring(0, 8);
                buffer.delete(0, 8);
                byte nb = (byte) Integer.parseInt(seq, 2);
                out.write(nb);
            }
        }
        int addedBits = 0;
        if (buffer.length() > 0) {
            addedBits = 8 - buffer.length();
            while (buffer.length() < 8) buffer.append("0");
            String seq = buffer.substring(0, 8);
            byte nb = (byte) Integer.parseInt(seq, 2);
            out.write(nb);
        }

        byte[] outBytes = out.toByteArray();
        return new Pair<>(Base64.getEncoder().encodeToString(outBytes), addedBits);

    }

    public static String saveCodingTable(ArrayList<Node> nodes) {

        StringBuilder ct = new StringBuilder();

        for (Node n : nodes) {
            ct.append(n.getPrettySymbol());
            ct.append(":");
            ct.append(n.getCodingSequence());
            ct.append(System.lineSeparator());
        }

        return ct.toString();
    }

    public static ReplacementNode rebuildTree(ArrayList<Node> inputModel) {

        ArrayList<Node> codingNodes = new ArrayList<>(inputModel);
        return buildTree(codingNodes);
    }

    public static String decodeText(byte[] input, ReplacementNode root, int addedBits) {

        StringBuilder output = new StringBuilder();
        Node n = root;
        int bitLimit = 8;

        for (int i = 0; i < input.length; i++) {

            if(i == input.length - 1) bitLimit = 8 - addedBits;

            int comparator = 0x80;

            for(int j = 0; j < bitLimit; j++) {
                if((input[i] & comparator) == 0) n = ((ReplacementNode) n).getLeft();
                else n = ((ReplacementNode) n).getRight();
                if (!(n instanceof ReplacementNode)) {
                    output.append(n.getSymbol());
                    n = root;
                }
                comparator >>= 1;
            }
        }
        return output.toString();
    }

    public static Node findNode(ArrayList<Node> nodes, String s) {
        for (Node n : nodes) if (n.getSymbol().equals(s)) return n;
        return null;
    }
}
