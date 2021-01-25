package tools.huffman.file;

import com.google.common.primitives.Longs;
import java.io.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class FileTools {

    public static int[] calculateCompressedFileSize(ArrayList<Node> nodes) {

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

        for (Node n : nodes) if (n.getOccurrences() > numTypesMax[neededType]) neededType++;

        for (Node bn : nodes) {
            //bajty potrzebne na zapisanie modelu źródła:

            //bajt odpowiadający symbolowi
            counter += 1;
            //liczba powtórzeń symbolu
            counter += numTypesBytes[neededType];

            //bajty potrzebne na zakodowanie pliku:
            codedBitsCounter += bn.getOccurrences() * bn.getCodingSequence().length();
        }
        counter += Math.ceil(codedBitsCounter / 8.00);
        int leftoverBits = (int)(codedBitsCounter % 8);
        addedBits = ((leftoverBits == 0) ? 0 : (8 - leftoverBits));
        return new int[]{counter, addedBits, numTypesBytes[neededType]};
    }

    public static ArrayList<Node> countSymbols(InputStream is) throws IOException {

        ArrayList<Node> nodes = new ArrayList<>();
        is.mark(Integer.MAX_VALUE);

        byte[] b = {0};
        while (is.read(b) != -1) {
            Node n = findNode(nodes, b[0]);
            if (n == null) {
                Node newNode = new Node(b[0]);
                nodes.add(newNode);
            } else n.incrementOccurrences();
        }
        is.reset();
        return nodes;
    }

    public static ReplacementNode buildTree(ArrayList<Node> nodes) {
        //Tutaj zaimplementować budowanie drzewa Huffmana z obiektów typu Node i ReplacementNode
        int numberOfReplacements = 0;
        ArrayList<Node> nodesCopy = new ArrayList<>(nodes);
        if (nodesCopy.size() == 1) {
            ReplacementNode r = new ReplacementNode(nodesCopy.get(0), nodesCopy.get(0), (byte) 1);
            return r;
        }
        if (nodesCopy.size() == 0) {
            ReplacementNode r = new ReplacementNode(new Node((byte) 0), new Node((byte) 0), (byte) 1);
            return r;
        }
        while (nodesCopy.size() != 1) {
            Node n1 = nodesCopy.get(0);
            Node n2 = nodesCopy.get(1);
            ReplacementNode r = new ReplacementNode(n1, n2, (byte) (++numberOfReplacements));
            nodesCopy.remove(n1);
            nodesCopy.remove(n2);
            nodesCopy.add(r);
            nodesCopy.sort(Node.NodeOccurancesComparator);
        }
        return (ReplacementNode) nodesCopy.get(0);
    }

    public static void createCodingSequences(Node node, String prefix) {
        if (!(node instanceof ReplacementNode)) node.setCodingSequence(prefix);
        else {
            ReplacementNode rnode = (ReplacementNode) node;
            createCodingSequences(rnode.getLeft(), prefix + "0");
            createCodingSequences(rnode.getRight(), prefix + "1");
        }
    }

    public static void codeFile(InputStream is, OutputStream os, ArrayList<Node> nodes, int addedBits, int occurenceBytes) throws IOException {

        Collections.reverse(nodes);

        byte[] b = {0};
        StringBuilder buffer = new StringBuilder();

        os.write((byte) addedBits);
        os.write(ByteBuffer.allocate(2).putChar((char) nodes.size()).array());
        os.write((byte) occurenceBytes);

        for (Node n : nodes) {
            os.write(n.getSymbol() & 0xFF);
            ByteBuffer bb = ByteBuffer.allocate(occurenceBytes).put(Longs.toByteArray(n.getOccurrences()), 8 - occurenceBytes, occurenceBytes);
            os.write(bb.array());
        }

        while (is.read(b) != -1) {
            Node n = findNode(nodes, b[0]);
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

    public static ArrayList<Node> retrieveModel(InputStream is, int lineNum, int occBytes) throws IOException {

        ArrayList<Node> nodes = new ArrayList<>();
        byte[] b = {0};

        byte[] line = new byte[1 + occBytes];
        for(int i = 0; i < lineNum; i++) {
            is.read(line);
            byte[] ocb = Arrays.copyOfRange(line, 1, occBytes + 1);
            long occ = 0;
            for(int j = 0; j < ocb.length; j++) occ += (long) (ocb[ocb.length - 1 - j] & 0xFF) << (j * 8);

            Node newNode = new Node((byte) (line[0] & 0xFF));
            newNode.setOccurrences(occ);
            nodes.add(newNode);
        }
        nodes.sort(Node.NodeOccurancesComparator);
        return nodes;
    }

    public static void decodeFile(InputStream is, OutputStream os, ReplacementNode root, int addedBits) throws IOException {

        byte[] b = {0};
        StringBuilder buffer = new StringBuilder();
        Node n = root;

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
                if(buffer.charAt(i) == '0') n = ((ReplacementNode) n).getLeft();
                else n = ((ReplacementNode) n).getRight();
                if(!(n instanceof ReplacementNode)) {
                    os.write(n.getSymbol());
                    n = root;
                }
            }
            buffer.setLength(0);
        }
    }

    public static Node findNode(ArrayList<Node> nodes, byte b) {
        for (Node n : nodes) if (n.getSymbol() == b) return n;
        return null;
    }
}