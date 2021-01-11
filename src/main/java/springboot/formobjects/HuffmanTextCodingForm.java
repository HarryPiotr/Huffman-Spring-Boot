package springboot.formobjects;

import com.mxgraph.layout.mxCompactTreeLayout;
import com.mxgraph.layout.mxIGraphLayout;
import com.mxgraph.util.mxCellRenderer;
import javafx.util.Pair;
import org.jgrapht.Graph;
import org.jgrapht.ext.JGraphXAdapter;
import tools.BinaryTreeEdge;
import tools.huffman.text.*;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Base64;

public class HuffmanTextCodingForm {

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

    public void generateModel() {

        //Automatyczny dobór długości słowa
        if(wordLength == 0) {
            ArrayList<Pair<Integer, Double>> results = new ArrayList<>();
            for(int i = 1; i <= 4; i++) {
                if(inputText.length() < i) break;
                results.add(new Pair<>(i, TextTools.countSymbols(inputText, i).getValue()));
            }
            wordLength = 1;
            double minEnt = results.get(0).getValue();
            for(Pair<Integer, Double> p : results) {
                if(p.getValue() < minEnt) {
                    minEnt = p.getValue();
                    wordLength = p.getKey();
                }
                entropyReport.add("Dla l=" + p.getKey() + " entriopia H=" + new DecimalFormat("#0.0000").format(p.getValue()) + System.lineSeparator());
            }
            Pair<ArrayList<Node>, Double> resultPair = TextTools.countSymbols(inputText, wordLength);
            nodes = resultPair.getKey();
        }
        else {
            Pair<ArrayList<Node>, Double> resultPair = TextTools.countSymbols(inputText, wordLength);
            nodes = resultPair.getKey();
            entropyReport.add("Dla l=" + wordLength + " entriopia H=" + new DecimalFormat("#0.0000").format(resultPair.getValue()) + System.lineSeparator());
        }

        Pair<ArrayList<Node>, Double> resultPair = TextTools.countSymbols(inputText, wordLength);
        nodes = resultPair.getKey();

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

        ReplacementNode treeRoot = TextTools.buildTree(nodes);
        TextTools.createCodingSequences(treeRoot, "");
        codingTable = TextTools.saveCodingTable(nodes);

    }

    public void generateTreeGraph() throws IOException {

        treeRoot = TextTools.buildTree(nodes);
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
        Pair<String, Integer> compressionResult = TextTools.encodeText(nodes, inputText, wordLength);

        String rawText = compressionResult.getKey();

        //Metadane
        output.append((char)(compressionResult.getValue().longValue() + 64));
        output.append((char)(1 + 64));
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

        output.append(rawText);

        compressedText = output.toString();
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

    public ArrayList<String> getEntropyReport() { return entropyReport; }

    public String getTreeGraph() { return treeGraph; }

}