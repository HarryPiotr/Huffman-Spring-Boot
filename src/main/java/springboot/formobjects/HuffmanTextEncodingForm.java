package springboot.formobjects;

import com.mxgraph.layout.mxCompactTreeLayout;
import com.mxgraph.layout.mxIGraphLayout;
import com.mxgraph.util.mxCellRenderer;
import org.jgrapht.Graph;
import org.jgrapht.ext.JGraphXAdapter;
import tools.BinaryTreeEdge;
import tools.huffman.text.*;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.text.DecimalFormat;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Map;

public class HuffmanTextEncodingForm {

    private String inputText;
    private String inputModel;
    private String codingTable;
    private String compressedText;
    private String compressionRatio = new DecimalFormat("#0.0000").format(0.0);
    private ArrayList<Node> nodes;
    private ReplacementNode treeRoot;
    private String treeGraph;
    private int wordLength;
    private ArrayList<String> entropyReport = new ArrayList<>();

    public double averageCWLngth;
    public double entropy;

    public void generateModel() {

        if(wordLength == 0) {
            //Automatyczny dobór długości słowa
            double minEnt = -1.0;
            for(int i = 1; i <= 4; i++) {
                if(inputText.length() < i) break;
                Map.Entry<ArrayList<Node>, Double> pair = TextTools.countSymbols(inputText, i);
                if(minEnt == -1.0 || pair.getValue() < minEnt) {
                    minEnt = pair.getValue();
                    wordLength = i;
                    nodes = pair.getKey();
                }
                entropyReport.add("Dla l=" + i + " entriopia H=" + new DecimalFormat("#0.0000").format(pair.getValue()));
            }
        }
        else {
            //Słowo zostało wybrane przez użytkownika
            Map.Entry<ArrayList<Node>, Double> resultPair = TextTools.countSymbols(inputText, wordLength);
            nodes = resultPair.getKey();
            entropyReport.add("Dla l=" + wordLength + " entriopia H=" + new DecimalFormat("#0.0000").format(resultPair.getValue()));
        }

        StringBuilder im = new StringBuilder();
        for(Node n : nodes) {
            im.append(n.getPrettySymbol());
            im.append(":");
            im.append(n.getOccurrences());
            im.append(System.lineSeparator());
        }
        inputModel = im.toString();
    }

    public void generateCodingTable() {

        treeRoot = TextTools.buildTree(nodes);
        TextTools.createCodingSequences(treeRoot, "");
        codingTable = TextTools.saveCodingTable(nodes);

    }

    public void generateTreeGraph() throws IOException {

        Graph<String, BinaryTreeEdge> g = TextTools.generateTreeGraph(treeRoot);

        JGraphXAdapter<String, BinaryTreeEdge> graphAdapter = new JGraphXAdapter<>(g);
        mxIGraphLayout layout = new mxCompactTreeLayout(graphAdapter, false, false);
        layout.execute(graphAdapter.getDefaultParent());
        BufferedImage image = mxCellRenderer.createBufferedImage(graphAdapter, null, 2, Color.WHITE, true, null);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ImageIO.write(image, "PNG", os);

        this.treeGraph = Base64.getEncoder().encodeToString(os.toByteArray());
    }

    public void generateCompressedText() {

        StringBuilder output = new StringBuilder();
        StringBuilder modelCopy = new StringBuilder();
        Map.Entry<String, Integer> compressionResult = TextTools.encodeText(nodes, inputText, wordLength);

        String rawText = compressionResult.getKey();

        //Metadane
        output.append(compressionResult.getValue());
        output.append(wordLength);
        output.append("[");
        output.append(nodes.size());
        output.append("]");

        //Model
        for(Node n : nodes) {
            output.append(n.getPrettySymbol());
            output.append(' ');
            output.append(n.getOccurrences());
            output.append(";");
        }

        //Ciąg kodowy
        output.append(rawText);

        compressedText = output.toString();
    }

    public void calculateAverageCodewordLenght() {
        Map.Entry<ArrayList<Node>, Double> resultPair = TextTools.countSymbols(inputText, wordLength);
        nodes = resultPair.getKey();
        entropy = resultPair.getValue();
        treeRoot = TextTools.buildTree(nodes);
        TextTools.createCodingSequences(treeRoot, "");
        generateCompressedText();
        Map.Entry<byte[], Integer> compressionResult = TextTools.encodeTextReturnCodewords(nodes, inputText, wordLength);
        long totalBytes = compressionResult.getKey().length;
        long totalBits = (totalBytes * 8) - compressionResult.getValue();

        long totalSymbolCount = 0;
        for(Node n: nodes) totalSymbolCount += n.getOccurrences();

        averageCWLngth = (double) totalBits / (double) totalSymbolCount;
    }



    public String getInputText() {
        return inputText;
    }

    public void setInputText(String inputText) { this.inputText = inputText; }

    public String getInputModel() {
        return inputModel;
    }

    public String getCodingTable() {
        return codingTable;
    }

    public String getCompressedText() {
        return compressedText;
    }

    public int getWordLength() { return wordLength; }

    public void setWordLength(int wordLength) { this.wordLength = wordLength; }
    public void setWordLength(String wordLength) { this.wordLength = Integer.valueOf(wordLength); }

    public String getCompressionRatio() {
        compressionRatio = new DecimalFormat("#0.0000").format((double) compressedText.length() / (double) (inputText.length()));
        return compressionRatio;
    }

    public double getCompressionRatioDouble() {
        return (double) compressedText.length() / (double) (inputText.length());
    }

    public ArrayList<String> getEntropyReport() { return entropyReport; }

    public String getTreeGraph() { return treeGraph; }

}