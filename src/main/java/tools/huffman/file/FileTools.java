package tools.huffman.file;

import org.jgrapht.Graph;
import org.jgrapht.graph.SimpleGraph;
import tools.BinaryTreeEdge;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;

public class FileTools {

    public static boolean checkIfFileExists(File f) {
        try {
            InputStream is = new FileInputStream(f);
        } catch (FileNotFoundException e) {
            System.out.println(f.getName() + ": Nie znaleziono pliku.");
            return false;
        }
        return true;
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

    public static void sortBinaryNodeList(ArrayList<BinaryNode> nodes) {
        //Tutaj zaimplementować dowolny algorytm sortowania listy pod względem ilości wystąpień
        nodes.sort(BinaryNode.BinaryNodeOccurancesComparator);
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

    public static void createCodingSequencesBinary(BinaryNode node, String prefix) {
        if (!(node instanceof ReplacementBinaryNode)) node.setCodingSequence(prefix);
        else {
            ReplacementBinaryNode rnode = (ReplacementBinaryNode) node;
            createCodingSequencesBinary(rnode.getLeft(), prefix + "0");
            createCodingSequencesBinary(rnode.getRight(), prefix + "1");
        }
    }

    //TODO: operating on strings -> operating on bytes
    public static File codeFile(ArrayList<BinaryNode> nodes, File in) {
        File outputFile = new File(in.getName() + ".temp");
        File outputFile2 = new File(in.getName() + ".huff");
        ArrayList<BinaryNode> flippedNodes = new ArrayList<>(nodes);
        flippedNodes.sort(BinaryNode.BinaryNodeOccurancesComparatorDescending);

        int i = 0, j = 8;

        try {
            InputStream is = new FileInputStream(in);
            OutputStream os = new FileOutputStream(outputFile);
            byte[] b = {0};
            StringBuilder buffer = new StringBuilder();
            System.out.println("Kodowanie pliku");
            while (is.read(b) != -1) {
                BinaryNode n = findNode(nodes, b[0]);
                buffer.append(n.getCodingSequence());
                while (buffer.length() >= j) {
                    String seq = buffer.substring(i, j);
                    i += 8; j += 8;
                    byte nb = (byte) Integer.parseInt(seq, 2);
                    os.write(nb);
                }
            }
            int addedBits = 0;
            System.out.println("Dopisywanie bitow na koncu");
            if (buffer.length() > i) {
                addedBits = 8 - (buffer.length() - i);
                while (buffer.length() < j) buffer.append("0");
                byte nb = (byte) Integer.parseInt(buffer.substring(i, j), 2);
                os.write(nb);
            }


            OutputStream os2 = new FileOutputStream(outputFile2);
            System.out.println("Tworzenie nowego pliku");
            System.out.println("Zapisywanie liczby dopisanych bitow");
            os2.write((byte)addedBits);
            System.out.println("Zapisywanie liczby linijek tabeli modelu");
            byte[] ns = ByteBuffer.allocate(2).putChar((char)nodes.size()).array();
            os2.write(ns);

            short neededType = 0;
            long[] numTypesMax = {Byte.MAX_VALUE, Short.MAX_VALUE, Integer.MAX_VALUE, Long.MAX_VALUE};
            int[] numTypesBytes = {1, 2, 4, 8};

            System.out.println("Szukanie liczby potrzebnych bajtow");

            for(BinaryNode n: nodes) {
                if(n.getOccurrences() > numTypesMax[neededType]) neededType++;
            }

            ns = ByteBuffer.allocate(1).put((byte)numTypesBytes[neededType]).array();

            System.out.println("Zapisywanie liczby potrzebnych bajtow");

            os2.write(ns);

            System.out.println("Zapisywanie tabeli modelu");
            switch(numTypesBytes[neededType]) {
                case 1:
                    for(BinaryNode n : nodes) {
                        os2.write(n.getSymbol() & 0xFF);
                        ByteBuffer bb = ByteBuffer.allocate(Byte.BYTES);
                        bb.put((byte)n.getOccurrences());
                        os2.write(bb.array());
                        os2.write(10);
                    }
                    break;
                case 2:
                    for(BinaryNode n : nodes) {
                        os2.write(n.getSymbol() & 0xFF);
                        ByteBuffer bb = ByteBuffer.allocate(Short.BYTES);
                        bb.putShort((short)n.getOccurrences());
                        os2.write(bb.array());
                        os2.write(10);
                    }
                    break;
                case 4:
                    for(BinaryNode n : nodes) {
                        os2.write(n.getSymbol() & 0xFF);
                        ByteBuffer bb = ByteBuffer.allocate(Integer.BYTES);
                        bb.putInt((int)n.getOccurrences());
                        os2.write(bb.array());
                        os2.write(10);
                    }
                    break;
                case 8:
                    for(BinaryNode n : nodes) {
                        os2.write(n.getSymbol() & 0xFF);
                        ByteBuffer bb = ByteBuffer.allocate(Long.BYTES);
                        bb.putLong(n.getOccurrences());
                        os2.write(bb.array());
                        os2.write(10);
                    }
                    break;
            }

            System.out.println("Przepisywanie zakodowanego pliku");

            InputStream is2 = new FileInputStream(outputFile);
            while(is2.read(b) != -1) {
                os2.write(b);
            }
            os2.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        outputFile.delete();
        return outputFile2;
    }

    public static ArrayList<BinaryNode> rebuildTree(File file) {

        ArrayList<BinaryNode> codingNodes = new ArrayList<>();
        byte[] b = {0};
        int occBytes;
        ByteBuffer bb;

        try {
            InputStream is = new FileInputStream(file);
            is.read(b);
            //Odczytujemy liczbę linijek słownikowych (INT)
            byte[] tb = {0, 0};
            is.read(tb);
            bb = ByteBuffer.wrap(tb);
            int lineNum = bb.getChar();
            is.read(b);
            occBytes = b[0] & 0xFF;

            System.out.println("Tablicę zakodowano " + occBytes + " bajtami liczbowymi");

            byte[] line = new byte[1 + occBytes + 1];

            System.out.println("Linijki " + line.length + "-bitowe");

            switch(occBytes) {
                case 1:
                    for(int i = 0; i < lineNum ; i++) {
                        is.read(line);
                        BinaryNode newNode = new BinaryNode((byte)(line[0] & 0xFF));
                        bb = ByteBuffer.wrap(Arrays.copyOfRange(line, 1, occBytes + 1));
                        byte occ = bb.get();
                        newNode.setOccurrences(occ);
                        codingNodes.add(newNode);
                    }
                    break;
                case 2:
                    for(int i = 0; i < lineNum ; i++) {
                        is.read(line);
                        BinaryNode newNode = new BinaryNode(line[0]);
                        bb = ByteBuffer.wrap(Arrays.copyOfRange(line, 1, occBytes + 1));
                        short occ = bb.getShort();
                        newNode.setOccurrences(occ);
                        codingNodes.add(newNode);
                    }
                    break;
                case 4:
                    for(int i = 0; i < lineNum ; i++) {
                        is.read(line);
                        BinaryNode newNode = new BinaryNode(line[0]);
                        bb = ByteBuffer.wrap(Arrays.copyOfRange(line, 1, occBytes + 1));
                        int occ = bb.getInt();
                        newNode.setOccurrences(occ);
                        codingNodes.add(newNode);
                    }
                    break;
                case 8:
                    for(int i = 0; i < lineNum ; i++) {
                        is.read(line);
                        BinaryNode newNode = new BinaryNode(line[0]);
                        bb = ByteBuffer.wrap(Arrays.copyOfRange(line, 1, occBytes + 1));
                        long occ = bb.getLong();
                        newNode.setOccurrences(occ);
                        codingNodes.add(newNode);
                    }
                    break;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        sortBinaryNodeList(codingNodes);
        return codingNodes;
    }

    public static File decodeFile(File inputFile, ReplacementBinaryNode root) {

        File outputFile = new File(inputFile.getName().substring(0, inputFile.getName().length() - 5));
        byte[] b = {0};
        int addedBits;
        int occBytes;
        InputStream is;
        OutputStream os;

        try {
            is = new FileInputStream(inputFile);
            os = new FileOutputStream(outputFile);
            is.read(b);
            addedBits = b[0] & 0xFF;
            //Odczytujemy liczbę linijek słownikowych (INT)
            byte[] tb = {0, 0};
            is.read(tb);
            ByteBuffer bb = ByteBuffer.wrap(tb);
            is.read(b);
            occBytes = b[0] & 0xFF;
            int lineNum = bb.getChar();
            byte[] line = new byte[1 + occBytes + 1];
            //pomijamy linijki słownikowe
            for(int i = 0; i < lineNum; i++) is.read(line);

            StringBuffer buffer = new StringBuffer();
            BinaryNode n = root;

            while (is.read(b) != -1) {
                int bi = b[0] & 0xFF;   //Poniewaz byte przyjmuje wartosc od -128 do 127
                for (int i = 0; i < 8; i++) {
                    buffer.insert(0, "" + bi % 2);
                    bi /= 2;
                }

                //sprawdzamy czy to ostatni bajt w pliku
                if(is.available() == 0) {
                    //To znaczy że dotarliśmy do końca pliku
                    buffer.delete(8 - addedBits, 8);
                }

                while (buffer.length() > 0) {
                    if (buffer.substring(0, 1).equals("0")) n = ((ReplacementBinaryNode) n).getLeft();
                    else n = ((ReplacementBinaryNode) n).getRight();
                    buffer.deleteCharAt(0);
                    if (!(n instanceof ReplacementBinaryNode)) {
                        os.write(n.getSymbol());
                        n = root;
                    }
                }
            }
            os.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return outputFile;

    }

    public static BinaryNode findNode(ArrayList<BinaryNode> nodes, byte b) {
        for (BinaryNode n : nodes) {
            if (n.getSymbol() == b) return n;
        }
        return null;
    }
}
