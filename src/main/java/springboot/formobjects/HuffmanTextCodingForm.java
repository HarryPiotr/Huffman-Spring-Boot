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
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Base64;

public class HuffmanTextCodingForm {

    private String inputText;
    private String inputModel;
    private String codingTable;
    private String uncompressedText;
    private String compressedText;
    private int uncompressedTextLength;
    private int codingTableLength;
    private int compressedTextLength;
    private String compressionRatio = new DecimalFormat("#0.0000").format(0.0);
    private ArrayList<Node> nodes;
    private ReplacementNode treeRoot;
    private String treeGraph;

    public String getInputText() {
        return inputText;
    }

    public void setInputText(String inputText) {
        this.inputText = inputText;
    }

    public void generateModel() {

        nodes = TextTools.countSymbols(inputText);
        TextTools.sortNodeList(nodes);

        StringBuilder im = new StringBuilder();

        for(Node n : nodes) {
            if(n.getIsWhiteSpace()) im.append(n.getWhiteSpace());
            else im.append(n.getSymbol());
            im.append(":");
            im.append(n.getOccurrences());
            im.append(System.lineSeparator());
        }

        inputModel = im.toString();

    }

    public void generateUncompressedText() {

        StringBuilder ut = new StringBuilder();

        for(int i = 0; i < inputText.length(); i++) {

            StringBuilder s = new StringBuilder(Integer.toBinaryString(inputText.charAt(i)));
            while(s.length() < 16) {
                s.insert(0, '0');
            }
            ut.append(s);

        }

        uncompressedText = ut.toString();
        uncompressedTextLength = uncompressedText.length() / 8;

    }

    public void generateTreeGraph() {

        treeRoot = TextTools.buildTree(nodes);
        Graph<String, BinaryTreeEdge> g = TextTools.generateTreeGraph(treeRoot);

        JGraphXAdapter<String, BinaryTreeEdge> graphAdapter = new JGraphXAdapter<>(g);
        mxIGraphLayout layout = new mxCompactTreeLayout(graphAdapter, false, false);
        layout.execute(graphAdapter.getDefaultParent());

        BufferedImage image = mxCellRenderer.createBufferedImage(graphAdapter, null, 2, Color.WHITE, true, null);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            ImageIO.write(image, "PNG", os);
        }
        catch(IOException e) {
            e.printStackTrace();
        }
        this.treeGraph = Base64.getEncoder().encodeToString(os.toByteArray());
    }

    public void generateCodingTable() {

        ReplacementNode treeRoot = TextTools.buildTree(nodes);
        TextTools.createCodingSequences(treeRoot, "");
        codingTable = TextTools.saveCodingTable(nodes);

    }

    public int getCodingTableLength() {
        if(codingTable != null) codingTableLength = codingTable.length() * 2;
        return codingTableLength;
    }

    public void generateCompressedText() {

        StringBuilder completeText = new StringBuilder();
        StringBuilder modelCopy = new StringBuilder();
        String rawText = TextTools.encodeText(nodes, inputText);

        for(Node n : nodes) {
            if(n.getIsWhiteSpace()) modelCopy.append(n.getWhiteSpace());
            else modelCopy.append(n.getSymbol());
            modelCopy.append(n.getOccurrences());
            modelCopy.append(";");
        }

        completeText.append(modelCopy);
        completeText.append(rawText);

        compressedText = completeText.toString();
        compressedTextLength = compressedText.length() * 2;

    }

    public String getInputModel() {
        return inputModel;
    }

    public void setInputModel(String inputModel) {
        this.inputModel = inputModel;
    }

    public String getCodingTable() {
        return codingTable;
    }

    public void setCodingTable(String codingTable) {
        this.codingTable = codingTable;
    }

    public String getCompressedText() {
        return compressedText;
    }

    public void setCompressedText(String compressedText) {
        this.compressedText = compressedText;
    }

    public String getUncompressedText() {
        return uncompressedText;
    }

    public void setUncompressedText(String uncompressedText) {
        this.uncompressedText = uncompressedText;
    }

    public int getUncompressedTextLength() {
        return uncompressedTextLength;
    }

    public void setUncompressedTextLength(int uncompressedTextLength) {
        this.uncompressedTextLength = uncompressedTextLength;
    }

    public int getCompressedTextLength() {
        return compressedTextLength;
    }

    public void setCompressedTextLength(int compressedTextLength) {
        this.compressedTextLength = compressedTextLength;
    }

    public String getCompressionRatio() {
        if(uncompressedTextLength != 0) compressionRatio = new DecimalFormat("#0.0000").format((double) getCompressedTextLength() / (double) (inputText.length() * 2));
        return compressionRatio;
    }

    public String getTreeGraph() {
        return treeGraph;
    }

}