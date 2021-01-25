package tools.huffman.text;

import com.google.common.base.Splitter;
import org.jgrapht.Graph;
import org.jgrapht.graph.SimpleGraph;
import tools.BinaryTreeEdge;
import java.io.ByteArrayOutputStream;
import java.util.*;

public class TextTools {

    public static Map.Entry<ArrayList<Node>, Double> countSymbols(String s, int l) {

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
        nodes.sort(Node.NodeOccurancesComparator);
        return new AbstractMap.SimpleEntry<>(nodes, calculateEntropy(nodes, s.length(), l));
    }

    public static double calculateEntropy(ArrayList<Node> nodes, int lng, int l) {

        double result = 0.0;
        for(Node n : nodes) {
            double p = ((double) n.getOccurrences()) / (Math.ceil(((double) lng) / (double) l));
            result += p * Math.log(p) / Math.log(2);
        }
        result *= -1;
        return result;
    }

    public static ReplacementNode buildTree(ArrayList<Node> nodes) {

        int numberOfReplacements = 0;
        ArrayList<Node> nodesCopy = new ArrayList<>(nodes);

        if (nodesCopy.size() == 1) {
            return new ReplacementNode(nodesCopy.get(0), nodesCopy.get(0), "1");
        }
        if (nodesCopy.size() == 0) {
            return new ReplacementNode(new Node("\0"), new Node("\0"), "1");
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

        g.addVertex(root.getPrettySymbol());
        if(root.getParent() != null) {
            g.addEdge(root.getParent().getPrettySymbol(), root.getPrettySymbol(), new BinaryTreeEdge(dir));
        }
        if(root instanceof ReplacementNode) {
            checkBranch(((ReplacementNode) root).getLeft(), g, true);
            checkBranch(((ReplacementNode) root).getRight(), g, false);
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

    public static Map.Entry<String, Integer> encodeText(ArrayList<Node> nodes, String s, int l) {

        ArrayList<Node> flippedNodes = new ArrayList<>(nodes);
        Collections.reverse(flippedNodes);
        StringBuilder buffer = new StringBuilder();
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        List<String> tokens = Splitter.fixedLength(l).splitToList(s);

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
        return new AbstractMap.SimpleEntry<>(Base64.getEncoder().encodeToString(outBytes), addedBits);
    }

    public static Map.Entry<byte[], Integer> encodeTextReturnCodewords(ArrayList<Node> nodes, String s, int l) {

        ArrayList<Node> flippedNodes = new ArrayList<>(nodes);
        Collections.reverse(flippedNodes);
        StringBuilder buffer = new StringBuilder();
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        List<String> tokens = Splitter.fixedLength(l).splitToList(s);

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
        return new AbstractMap.SimpleEntry<>(outBytes, addedBits);
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
