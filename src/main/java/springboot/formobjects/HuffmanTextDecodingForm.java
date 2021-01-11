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
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;

public class HuffmanTextDecodingForm {

    private String inputText;
    private String outputText;
    private String codedText;
    private String inputModelView;
    private ArrayList<Node> nodes = new ArrayList<>();
    private ReplacementNode treeRoot;
    private String treeGraph;
    private String codingTable;
    private int addedBits;
    private int modelSize;
    private int wordLength;

    public void splitInput() {

        StringBuilder sb = new StringBuilder(inputText);
        StringBuilder msb = new StringBuilder();

        //Metadane
        this.addedBits = sb.charAt(0) - 64;
        this.wordLength = sb.charAt(1) - 64;
        this.modelSize = Integer.parseInt(sb.substring(3, sb.indexOf("]")));
        sb.delete(0, sb.indexOf("]") + 1);

        for(int i = 0; i < modelSize; i++) {

            String line = sb.substring(0, sb.indexOf(";", sb.indexOf(" ")));
            sb.delete(0, sb.indexOf(";", sb.indexOf(" ")) + 1);

            String symbol = line.substring(0, line.indexOf(" "));
            int occ = Integer.valueOf(line.substring(line.indexOf(" ") + 1));

            msb.append(symbol);
            msb.append(":");
            msb.append(occ);
            msb.append(System.lineSeparator());

            String parsedSymbol = "";
            for(int j = 0; j < symbol.length(); j++) {
                if(symbol.charAt(j) == '[') {
                    String susSequence = "" + symbol.charAt(j + 1) + symbol.charAt(j + 2);
                    switch(susSequence) {
                        case "NL":
                            parsedSymbol += (char)10;
                            break;
                        case "CR":
                            parsedSymbol += (char)13;
                            break;
                        case "SP":
                            parsedSymbol += (char)32;
                            break;
                        case "HT":
                            parsedSymbol += (char)9;
                            break;
                        case "OB":
                            parsedSymbol += (char)91;
                            break;
                        case "CB":
                            parsedSymbol += (char)93;
                            break;
                    }
                    j += 3;
                }
                else parsedSymbol += symbol.charAt(j);
            }
            Node n = new Node(parsedSymbol);
            n.setOccurrences(occ);
            nodes.add(n);
        }

        inputModelView = msb.toString();
        codedText = sb.toString();
    }

    public void decodeText() {

        //Odkodowanie Base64
        byte[] codedBytes = Base64.getDecoder().decode(codedText);

        this.treeRoot = TextTools.rebuildTree(nodes);
        TextTools.createCodingSequences(treeRoot, "");
        codingTable = TextTools.saveCodingTable(nodes);
        this.outputText = TextTools.decodeText(codedBytes, this.treeRoot, addedBits);

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

    public String getInputText() {
        return inputText;
    }

    public String getOutputText() {
        return outputText;
    }

    public void setInputText(String inputText) {
        this.inputText = inputText;
    }

    public String getInputModelView() {
        return inputModelView;
    }

    public String getCodedText() {
        return codedText;
    }

    public String getTreeGraph() {
        return treeGraph;
    }

    public String getCodingTable() {
        return codingTable;
    }
}
