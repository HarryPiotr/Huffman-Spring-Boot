package tools.huffman.file;

import com.google.common.primitives.Longs;
import org.jgrapht.Graph;
import org.jgrapht.graph.SimpleGraph;
import tools.BinaryTreeEdge;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;

public class FileTools {

    public static int[] calculateCompressedFileSize(ArrayList<BinaryNode> nodes) {

        int counter = 0;
        int addedBits;
        long codedBitsCounter = 0;

        //liczba dopisanych bitów:
        counter += 1;
        //liczba linijek modelu źródła:
        counter += 2;
        //liczba bajtów do zapisania liczby powtórzeń symbolu:
        counter += 1;

        //Szukanie liczby bajtów potrzebnych do zapisania liczby powtórzeń symbolu:
        int neededType = 0;
        long[] numTypesMax = {Byte.MAX_VALUE, Short.MAX_VALUE, Integer.MAX_VALUE, Long.MAX_VALUE};
        int[] numTypesBytes = {1, 2, 4, 8};

        for (BinaryNode n : nodes) if (n.getOccurrences() > numTypesMax[neededType]) neededType++;

        for (BinaryNode bn : nodes) {

            //bajty potrzebne na zapisanie modelu źródła:
            //bajt odpowiadający symbolowi
            counter += 1;
            //liczba powtórzeń symbolu
            counter += numTypesBytes[neededType];
            //newline (\10)
            counter += 1;

            //bajty potrzebne na zakodowanie pliku:
            codedBitsCounter += bn.getOccurrences() * bn.getCodingSequence().length();

        }

        counter += Math.ceil(codedBitsCounter / 8.00);
        addedBits = 8 - (int) (codedBitsCounter % 8);

        return new int[]{counter, addedBits, numTypesBytes[neededType]};

    }

    public static ArrayList<BinaryNode> countSymbols(InputStream is) throws IOException {

        ArrayList<BinaryNode> nodes = new ArrayList<>();

        is.mark(Integer.MAX_VALUE);

        byte[] b = {0};
        while (is.read(b) != -1) {
            BinaryNode n = findNode(nodes, b[0]);
            if (n == null) {
                BinaryNode newNode = new BinaryNode(b[0]);
                nodes.add(newNode);
            } else n.incrementOccurrences();
        }

        is.reset();
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
        if (nodesCopy.size() == 1) {
            ReplacementBinaryNode r = new ReplacementBinaryNode(nodesCopy.get(0), nodesCopy.get(0), (byte) 1);
            return r;
        }
        if (nodesCopy.size() == 0) {
            ReplacementBinaryNode r = new ReplacementBinaryNode(new BinaryNode((byte) 0), new BinaryNode((byte) 0), (byte) 1);
            return r;
        }
        while (nodesCopy.size() != 1) {
            BinaryNode n1 = nodesCopy.get(0);
            BinaryNode n2 = nodesCopy.get(1);
            ReplacementBinaryNode r = new ReplacementBinaryNode(n1, n2, (byte) (++numberOfReplacements));
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

    public static void codeFile(InputStream is, OutputStream os, ArrayList<BinaryNode> nodes, int addedBits, int occurenceBytes) throws IOException {

        Collections.reverse(nodes);

        byte[] b = {0};
        StringBuilder buffer = new StringBuilder();

        os.write((byte) addedBits);
        os.write(ByteBuffer.allocate(2).putChar((char) nodes.size()).array());
        os.write((byte) occurenceBytes);

        for (BinaryNode n : nodes) {
            os.write(n.getSymbol() & 0xFF);
            ByteBuffer bb = ByteBuffer.allocate(occurenceBytes).put(Longs.toByteArray(n.getOccurrences()), 8 - occurenceBytes, occurenceBytes);
            os.write(bb.array());
            os.write(10);
        }

        while (is.read(b) != -1) {
            BinaryNode n = findNode(nodes, b[0]);
            buffer.append(n.getCodingSequence());
            while (buffer.length() >= 8) {
                byte nb = (byte) Integer.parseInt(buffer.substring(0, 8), 2);
                os.write(nb);
                buffer.delete(0, 8);
            }
        }
        if (buffer.length() > 0) {
            while (buffer.length() < 8) buffer.append('0');
            byte nb = (byte) Integer.parseInt(buffer.toString(), 2);
            os.write(nb);
        }
    }

    public static int[] readMetaData(InputStream is) throws IOException {
        byte[] b = {0};
        byte[] tb = {0, 0};
        int addedBits;
        int lineNum;
        int occBytes;

        is.read(b);
        addedBits = b[0] & 0xFF;
        is.read(tb);
        lineNum = ByteBuffer.wrap(tb).getShort();
        is.read(b);
        occBytes = b[0] & 0xFF;

        return new int[]{addedBits, lineNum, occBytes};
    }

    public static ArrayList<BinaryNode> rebuildTree(InputStream is, int lineNum, int occBytes) throws IOException {

        ArrayList<BinaryNode> nodes = new ArrayList<>();
        byte[] b = {0};

        byte[] line = new byte[1 + occBytes + 1];
        for (int i = 0; i < lineNum; i++) {
            is.read(line);
            byte[] ocb = Arrays.copyOfRange(line, 1, occBytes + 1);
            long occ = 0;
            for (int j = 0; j < ocb.length; j++) occ += (long) (ocb[ocb.length - 1 - j] & 0xFF) << (j * 8);

            BinaryNode newNode = new BinaryNode((byte) (line[0] & 0xFF));
            newNode.setOccurrences(occ);
            nodes.add(newNode);
        }

        sortBinaryNodeList(nodes);
        return nodes;
    }

    public static void decodeFile(InputStream is, OutputStream os, ReplacementBinaryNode root, int addedBits) throws IOException {

        byte[] b = {0};
        StringBuilder buffer = new StringBuilder();
        BinaryNode n = root;

        while (is.read(b) != -1) {
            int bi = b[0] & 0xFF;
            for (int i = 0; i < 8; i++) {
                buffer.append(bi & 0x01);
                bi >>= 1;
            }
            buffer.reverse();

            //sprawdzamy czy to ostatni bajt w pliku - jesli tak, to usuwamy dopisane bity
            if (is.available() == 0) buffer.delete(8 - addedBits, 8);

            for(int i = 0; i < buffer.length(); i++) {
                if(buffer.charAt(i) == '0') n = ((ReplacementBinaryNode) n).getLeft();
                else n = ((ReplacementBinaryNode) n).getRight();
                if(!(n instanceof ReplacementBinaryNode)) {
                    os.write(n.getSymbol());
                    n = root;
                }
            }
            buffer.setLength(0);
        }
    }

    public static BinaryNode findNode(ArrayList<BinaryNode> nodes, byte b) {
        for (BinaryNode n : nodes) if (n.getSymbol() == b) return n;
        return null;
    }
}