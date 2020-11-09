package tools;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

import java.io.*;
import java.util.ArrayList;
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

    public static ArrayList<Node> countSymbols(File f) {
        //Tutaj zaimplementować czytanie pliku bajt po bajcie i zapisywanie informacji o liczbie wystąpień do listy

        ArrayList<Node> nodes = new ArrayList<>();

        try {
            InputStream is = new FileInputStream(f);
            byte[] b = {0};
            while (is.read(b) != -1) {
                Node n = findNode(nodes, b[0]);
                if (n == null) {
                    Node newNode = new Node((char) b[0]);
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

        if(root instanceof ReplacementNode) {
            g.addVertex("Rep-" + (int)root.getSymbol());
            if(root.getParent() != null) g.addEdge("Rep-" + (int)root.getParent().getSymbol(), "Rep-" + (int)root.getSymbol(), new BinaryTreeEdge(dir));
            checkBranch(((ReplacementNode) root).getLeft(), g, true);
            checkBranch(((ReplacementNode) root).getRight(), g, false);
        }
        else {
            g.addVertex("" + root.getSymbol());
            if(root.getParent() != null) g.addEdge("Rep-" + (int)root.getParent().getSymbol(), "" + root.getSymbol(), new BinaryTreeEdge(dir));
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

    public static void codeFile(ArrayList<Node> nodes, File in) {
        File outputFile = new File(in.getAbsolutePath() + ".huff");
        ArrayList<Node> flippedNodes = new ArrayList<>(nodes);
        flippedNodes.sort(Node.NodeOccurancesComparatorDescending);

        try {
            InputStream is = new FileInputStream(in);
            OutputStream os = new FileOutputStream(outputFile);
            byte[] b = {0};
            StringBuffer buffer = new StringBuffer();
            while (is.read(b) != -1) {
                Node n = findNode(nodes, b[0]);
                buffer.append(n.getCodingSequence());
                if (buffer.length() >= 8) {
                    String seq = buffer.substring(0, 8);
                    buffer.delete(0, 8);
                    byte nb = (byte) Integer.parseInt(seq, 2);
                    os.write(nb);
                }
            }
            if (buffer.length() > 0) {
                while (buffer.length() < 8) buffer.append("0");
                String seq = buffer.substring(0, 8);
                byte nb = (byte) Integer.parseInt(seq, 2);
                os.write(nb);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println(in.getName() + ": Niespodziewany blad podczas czytania pliku.");
        }
    }

    public static String codeFile(ArrayList<Node> nodes, String s) {
        ArrayList<Node> flippedNodes = new ArrayList<>(nodes);
        flippedNodes.sort(Node.NodeOccurancesComparatorDescending);

        StringBuilder out = new StringBuilder();

        for(int i = 0; i < s.length(); i++) {

            Node n = findNode(nodes, s.charAt(i));
            out.append(n.getCodingSequence());

        }

        return out.toString();

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

    public static String saveCodingTable(ArrayList<Node> nodes, String s) {

        StringBuilder ct = new StringBuilder();

        for (Node n : nodes) {
            ct.append(n.getSymbol() + ":" + n.getCodingSequence() + "\n");
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
                    Node n = new Node((char)b);
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

    public static Node findNode(ArrayList<Node> nodes, byte b) {
        for (Node n : nodes) {
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
}
