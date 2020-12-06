package tools;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Map;
import java.util.StringTokenizer;

public class Huffman {

    public static boolean checkIfFileExists(File f) {
        try {
            InputStream is = new FileInputStream(f);
        } catch (FileNotFoundException e) {
            System.out.println(f.getName() + ": Nie znaleziono pliku.");
            return false;
        }
        return true;
    }

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

    public static ArrayList<BinaryNode> countSymbols(File f) {
        //Tutaj zaimplementować czytanie pliku bajt po bajcie i zapisywanie informacji o liczbie wystąpień do listy

        ArrayList<BinaryNode> nodes = new ArrayList<>();

        try {
            InputStream is = new FileInputStream(f);
            byte[] b = {0};
            while (is.read(b) != -1) {
                BinaryNode n = findNode(nodes, b[0]);
                if (n == null) {
                    BinaryNode newNode = new BinaryNode(b[0]);
                    nodes.add(newNode);
                } else n.incrementOccurrences();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println(f.getName() + ": Niespodziewany blad podczas czytania pliku.");
        }
        return nodes;
    }

    public static void sortNodeList(ArrayList<Node> nodes) {
        //Tutaj zaimplementować dowolny algorytm sortowania listy pod względem ilości wystąpień
        nodes.sort(Node.NodeOccurancesComparator);
    }

    public static void sortBinaryNodeList(ArrayList<BinaryNode> nodes) {
        //Tutaj zaimplementować dowolny algorytm sortowania listy pod względem ilości wystąpień
        nodes.sort(BinaryNode.BinaryNodeOccurancesComparator);
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

    public static ReplacementBinaryNode buildTreeBinary(ArrayList<BinaryNode> nodes) {
        //Tutaj zaimplementować budowanie drzewa Huffmana z obiektów typu Node i ReplacementNode
        int numberOfReplacements = 0;
        ArrayList<BinaryNode> nodesCopy = new ArrayList<>(nodes);
        while (nodesCopy.size() != 1) {
            BinaryNode n1 = nodesCopy.get(0);
            BinaryNode n2 = nodesCopy.get(1);
            ReplacementBinaryNode r = new ReplacementBinaryNode(n1, n2, (byte)(++numberOfReplacements));
            nodesCopy.remove(n1);
            nodesCopy.remove(n2);
            nodesCopy.add(r);
            nodesCopy.sort(BinaryNode.BinaryNodeOccurancesComparator);
        }
        return (ReplacementBinaryNode) nodesCopy.get(0);
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

    public static Graph<String, BinaryTreeEdge> generateTreeGraphBinary(ReplacementBinaryNode root) {

        Graph<String, BinaryTreeEdge> graph = new SimpleGraph<>(BinaryTreeEdge.class);
        checkBranchBinary(root, graph, false);
        return graph;
    }

    public static void checkBranchBinary(BinaryNode root, Graph<String, BinaryTreeEdge> g, boolean dir) {

        if (root instanceof ReplacementBinaryNode) {
            g.addVertex("#" + (int) root.getSymbol());
            if (root.getParent() != null)
                g.addEdge("#" + (int) root.getParent().getSymbol(), "#" + (int) root.getSymbol(), new BinaryTreeEdge(dir));
            checkBranchBinary(((ReplacementBinaryNode) root).getLeft(), g, true);
            checkBranchBinary(((ReplacementBinaryNode) root).getRight(), g, false);
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

    public static void createCodingSequencesBinary(BinaryNode node, String prefix) {
        if (!(node instanceof ReplacementBinaryNode)) node.setCodingSequence(prefix);
        else {
            ReplacementBinaryNode rnode = (ReplacementBinaryNode) node;
            createCodingSequencesBinary(rnode.getLeft(), prefix + "0");
            createCodingSequencesBinary(rnode.getRight(), prefix + "1");
        }
    }

    public static File codeFile(ArrayList<BinaryNode> nodes, File in) {
        File outputFile = new File(in.getName() + ".temp");
        File outputFile2 = new File(in.getName() + ".huff");
        ArrayList<BinaryNode> flippedNodes = new ArrayList<>(nodes);
        flippedNodes.sort(BinaryNode.BinaryNodeOccurancesComparatorDescending);

        try {
            InputStream is = new FileInputStream(in);
            OutputStream os = new FileOutputStream(outputFile);
            byte[] b = {0};
            StringBuffer buffer = new StringBuffer();
            while (is.read(b) != -1) {
                BinaryNode n = findNode(nodes, b[0]);
                buffer.append(n.getCodingSequence());
                while (buffer.length() >= 8) {
                    String seq = buffer.substring(0, 8);
                    buffer.delete(0, 8);
                    byte nb = (byte) Integer.parseInt(seq, 2);
                    os.write(nb);
                }
            }
            int addedBits = 0;
            if (buffer.length() > 0) {
                addedBits = 8 - buffer.length();
                while (buffer.length() < 8) buffer.append("0");
                byte nb = (byte) Integer.parseInt(buffer.toString(), 2);
                os.write(nb);
            }


            OutputStream os2 = new FileOutputStream(outputFile2);
            os2.write((byte)addedBits);
            for(BinaryNode n : nodes) {
                os2.write(n.getSymbol());
                ByteBuffer bb = ByteBuffer.allocate(Long.BYTES);
                bb.putLong(n.getOccurrences());
                os2.write(bb.array());
                os2.write(10);
            }
            InputStream is2 = new FileInputStream(outputFile);
            while(is2.read(b) != -1) {
                os2.write(b);
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println(in.getName() + ": Niespodziewany blad podczas czytania pliku.");
        }

        outputFile.delete();
        return outputFile2;
    }

    public static String codeString(ArrayList<Node> nodes, String s) {
        ArrayList<Node> flippedNodes = new ArrayList<>(nodes);
        flippedNodes.sort(Node.NodeOccurancesComparatorDescending);

        StringBuilder buffer = new StringBuilder();
        StringBuilder out = new StringBuilder();

        for (int i = 0; i < s.length(); i++) {

            Node n = findNode(nodes, s.charAt(i));
            buffer.append(n.getCodingSequence());
            if (buffer.length() >= 16) {
                String seq = buffer.substring(0, 16);
                buffer.delete(0, 16);
                char nb = (char) Integer.parseInt(seq, 2);
                out.append(nb);
            }
        }
        int addedBits = 0;
        if (buffer.length() > 0) {
            addedBits = 16 - buffer.length();
            out.insert(0, (char) (addedBits + 64));
            while (buffer.length() < 16) buffer.append("0");
            String seq = buffer.substring(0, 16);
            char nb = (char) Integer.parseInt(seq, 2);
            out.append(nb);
        }
        else out.insert(0, (char) (addedBits + 64));

        return out.toString();

    }

    public static String codeTextBinary(ArrayList<Node> nodes, String s) {
        ArrayList<Node> flippedNodes = new ArrayList<>(nodes);
        flippedNodes.sort(Node.NodeOccurancesComparatorDescending);

        StringBuilder buffer = new StringBuilder();

        for (int i = 0; i < s.length(); i++) {

            Node n = findNode(nodes, s.charAt(i));
            buffer.append(n.getCodingSequence());
        }
        int addedBits = 0;
        if (buffer.length() % 16 > 0) {
            addedBits = 16 - (buffer.length() % 16);
            buffer.insert(0, (char) (addedBits + 64));
            while ((buffer.length() % 16) != 0) buffer.append("0");
        }
        else buffer.insert(0, (char) (addedBits + 64));

        return buffer.toString();

    }

    public static void saveCodingTable(ArrayList<Node> nodes, File file) {
        try {
            PrintWriter of = new PrintWriter(file.getAbsolutePath() + ".coding");
            for (Node n : nodes) {
                of.println(n.getSymbol() + ":" + n.getCodingSequence() + ":" + n.getOccurrences());
            }
            of.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
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

    public static ReplacementNode rebuildTree(File codingFile) {

        ArrayList<Node> codingNodes = new ArrayList<>();

        try {
            FileReader fr = new FileReader(codingFile);
            BufferedReader br = new BufferedReader(fr);
            String fileLine;
            do {
                fileLine = br.readLine();
                if (fileLine != null) {
                    StringTokenizer st = new StringTokenizer(fileLine, ":");
                    byte b = Byte.parseByte(st.nextToken());
                    String seq = st.nextToken();
                    long oc = Long.parseLong(st.nextToken());
                    Node n = new Node((char) b);
                    n.setOccurrences(oc);
                    n.setCodingSequence(seq);
                    codingNodes.add(n);
                }
            } while (fileLine != null);
        } catch (FileNotFoundException e) {
            System.out.println(codingFile.getName() + ": Nie znaleziono tabeli kodowej");
        } catch (IOException e) {
            e.printStackTrace();
        }

        sortNodeList(codingNodes);
        return buildTree(codingNodes);
    }

    public static ReplacementNode rebuildTree(ArrayList<Node> inputModel) {

        ArrayList<Node> codingNodes = new ArrayList<>(inputModel);
        sortNodeList(codingNodes);
        return buildTree(codingNodes);
    }

    public static void decodeFile(File inputFile, File outputFile, ReplacementNode root) {

        try {
            InputStream is = new FileInputStream(inputFile);
            OutputStream os = new FileOutputStream(outputFile);

            byte[] b = {0};
            StringBuffer buffer = new StringBuffer();
            Node n = root;
            while (is.read(b) != -1) {
                int bi = b[0] & 0xFF;   //Poniewaz byte przyjmuje wartosc od -128 do 127
                for (int i = 0; i < 8; i++) {
                    buffer.insert(0, "" + bi % 2);
                    bi /= 2;
                }

                while (buffer.length() > 0) {
                    if (buffer.substring(0, 1).equals("0")) n = ((ReplacementNode) n).getLeft();
                    else n = ((ReplacementNode) n).getRight();
                    buffer.deleteCharAt(0);
                    if (!(n instanceof ReplacementNode)) {
                        os.write(n.getSymbol());
                        n = root;
                    }
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println(inputFile.getName() + ": Niespodziewany blad podczas czytania pliku.");
        }
    }

    public static String decodeText(StringBuilder buffer, ReplacementNode root) {

        StringBuilder output = new StringBuilder();
        Node n = root;
        int i = 0;
        while (buffer.length() > i) {
            if (buffer.charAt(i) == '0') n = ((ReplacementNode) n).getLeft();
            else n = ((ReplacementNode) n).getRight();
            i++;
            if (!(n instanceof ReplacementNode)) {
                output.append(n.getSymbol());
                n = root;
            }
        }
        return output.toString();
    }

    public static BinaryNode findNode(ArrayList<BinaryNode> nodes, byte b) {
        for (BinaryNode n : nodes) {
            if (n.getSymbol() == b) return n;
        }
        return null;
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
