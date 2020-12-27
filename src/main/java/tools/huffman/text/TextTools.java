package tools.huffman.text;

import org.jgrapht.Graph;
import org.jgrapht.graph.SimpleGraph;
import tools.BinaryTreeEdge;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Base64;

public class TextTools {

    public static ArrayList<Node> countSymbols(String s) {
        //Tutaj zaimplementować czytanie tekstu char po charze i zapisywanie informacji o liczbie wystąpień do listy

        ArrayList<Node> nodes = new ArrayList<>();

        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            Node n = findNode(nodes, c);
            if (n == null) {
                Node newNode = new Node(c);
                nodes.add(newNode);
            } else n.incrementOccurrences();
        }

        return nodes;
    }

    public static void sortNodeList(ArrayList<Node> nodes) {
        //Tutaj zaimplementować dowolny algorytm sortowania listy pod względem ilości wystąpień
        nodes.sort(Node.NodeOccurancesComparator);
    }

    public static ReplacementNode buildTree(ArrayList<Node> nodes) {
        //Tutaj zaimplementować budowanie drzewa Huffmana z obiektów typu Node i ReplacementNode
        int numberOfReplacements = 0;
        ArrayList<Node> nodesCopy = new ArrayList<>(nodes);
        while (nodesCopy.size() != 1) {
            Node n1 = nodesCopy.get(0);
            Node n2 = nodesCopy.get(1);
            ReplacementNode r = new ReplacementNode(n1, n2, (char) (++numberOfReplacements));
            nodesCopy.remove(n1);
            nodesCopy.remove(n2);
            nodesCopy.add(r);
            nodesCopy.sort(Node.NodeOccurancesComparator);
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
            g.addVertex("#" + (int) root.getSymbol());
            if (root.getParent() != null)
                g.addEdge("#" + (int) root.getParent().getSymbol(), "#" + (int) root.getSymbol(), new BinaryTreeEdge(dir));
            checkBranch(((ReplacementNode) root).getLeft(), g, true);
            checkBranch(((ReplacementNode) root).getRight(), g, false);
        } else {
            String vertexName = (!root.getIsWhiteSpace() ? "" + root.getSymbol() : root.getWhiteSpace());
            g.addVertex(vertexName);
            if (root.getParent() != null)
                g.addEdge("#" + (int) root.getParent().getSymbol(), vertexName, new BinaryTreeEdge(dir));
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

    public static String encodeText(ArrayList<Node> nodes, String s) {
        ArrayList<Node> flippedNodes = new ArrayList<>(nodes);
        flippedNodes.sort(Node.NodeOccurancesComparatorDescending);

        StringBuilder buffer = new StringBuilder();
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        for (int i = 0; i < s.length(); i++) {

            Node n = findNode(nodes, s.charAt(i));
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
            out.write((byte) (addedBits + 64));
            System.out.println("Dopisano " + addedBits + " bitów");
        }
        else out.write((byte) (addedBits + 64));

        byte[] outBytes = out.toByteArray();
        return Base64.getEncoder().encodeToString(outBytes);

    }

    public static String saveCodingTable(ArrayList<Node> nodes) {

        StringBuilder ct = new StringBuilder();

        for (Node n : nodes) {
            if(n.getIsWhiteSpace()) ct.append(n.getWhiteSpace());
            else ct.append(n.getSymbol());
            ct.append(":");
            ct.append(n.getCodingSequence());
            ct.append(System.lineSeparator());
        }

        return ct.toString();
    }

    public static ReplacementNode rebuildTree(ArrayList<Node> inputModel) {

        ArrayList<Node> codingNodes = new ArrayList<>(inputModel);
        sortNodeList(codingNodes);
        return buildTree(codingNodes);
    }

    public static String decodeText(byte[] input, ReplacementNode root) {

        StringBuilder output = new StringBuilder();
        Node n = root;
        int bitLimit = 8;

        int addedBits = (int) (input[input.length - 1] - 64);

        System.out.println("Dopisane bity: " + addedBits);

        for (int i = 0; i < input.length - 1; i++) {
            int comparator = 0x80;

            if(i == input.length - 2) bitLimit = 8 - addedBits;

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

    public static Node findNode(ArrayList<Node> nodes, char c) {
        for (Node n : nodes) {
            if (n.getSymbol() == c) return n;
        }
        return null;
    }

    public static Node findNode(ArrayList<Node> nodes, char c, boolean wsp) {
        for (Node n : nodes) {
            if (n.getSymbol() == c && n.getIsWhiteSpace()) return n;
        }
        return null;
    }
}
