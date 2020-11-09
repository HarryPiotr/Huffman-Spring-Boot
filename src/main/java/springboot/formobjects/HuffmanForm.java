package springboot.formobjects;

import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
import com.mxgraph.layout.mxCircleLayout;
import com.mxgraph.layout.mxCompactTreeLayout;
import com.mxgraph.layout.mxIGraphLayout;
import com.mxgraph.util.mxCellRenderer;
import org.jgrapht.Graph;
import org.jgrapht.ext.JGraphXAdapter;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;
import tools.*;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;

public class HuffmanForm {

    private String inputText;
    private String inputModel;
    private String codingTable;
    private String uncompressedText;
    private String compressedText;
    private int uncompressedTextLength;
    private int codingTableLength;
    private int compressedTextLength;
    private int totalCompressedLength;
    private String compressionRatio = new DecimalFormat("#0.0000").format(0.0);
    private ArrayList<Node> nodes;
    private String treeGraphPath;
    private ReplacementNode treeRoot;


    public String getInputText() {
        return inputText;
    }

    public void setInputText(String inputText) {
        this.inputText = inputText;
    }

    public void generateModel() {

        nodes = Huffman.countSymbols(inputText);
        Huffman.sortNodeList(nodes);

        StringBuilder im = new StringBuilder();

        for(Node n : nodes) {
            im.append(n.getSymbol() + ":" + n.getOccurrences() + "\n");
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
        uncompressedTextLength = uncompressedText.length();

    }

    public void generateTreeGraph(String sessionId) {

        treeRoot = Huffman.buildTree(nodes);
        Graph<String, BinaryTreeEdge> g = Huffman.generateTreeGraph(treeRoot);

        JGraphXAdapter<String, BinaryTreeEdge> graphAdapter = new JGraphXAdapter<String, BinaryTreeEdge>(g);
        mxIGraphLayout layout = new mxCompactTreeLayout(graphAdapter, false, false);
        layout.execute(graphAdapter.getDefaultParent());

        BufferedImage image = mxCellRenderer.createBufferedImage(graphAdapter, null, 2, Color.WHITE, true, null);
        String absolutePath = "src/main/resources/static/tree_graphs/" + sessionId + ".png";
        this.treeGraphPath = "tree_graphs/" + sessionId + ".png";
        File imgFile = new File(absolutePath);
        try {
            ImageIO.write(image, "PNG", imgFile);
        }
        catch(IOException e) {
            //TODO: Handle IOException
            this.treeGraphPath = "";
        }

    }

    public void generateCodingTable() {

        ReplacementNode treeRoot = Huffman.buildTree(nodes);
        Huffman.createCodingSequences(treeRoot, "");
        codingTable = Huffman.saveCodingTable(nodes, compressedText);

    }

    public int getCodingTableLength() {
        if(codingTable != null) codingTableLength = codingTable.length() * 16;
        return codingTableLength;
    }

    public void generateCompressedText() {

        compressedText = Huffman.codeFile(nodes, inputText);
        compressedTextLength = compressedText.length();

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

    public int getTotalCompressedLength() {
        totalCompressedLength = compressedTextLength + codingTableLength;
        return totalCompressedLength;
    }

    public void setTotalCompressedLength(int totalCompressedLength) {
        this.totalCompressedLength = totalCompressedLength;
    }

    public String getCompressionRatio() {
        if(uncompressedTextLength != 0) compressionRatio = new DecimalFormat("#0.0000").format((double) getTotalCompressedLength() / (double) uncompressedTextLength);
        return compressionRatio;
    }

    public String getTreeGraphPath() {
        return treeGraphPath;
    }

    public void setTreeGraphPath(String treeGraphPath) {
        this.treeGraphPath = treeGraphPath;
    }
}