package springboot.formobjects;

import com.mxgraph.layout.mxCompactTreeLayout;
import com.mxgraph.layout.mxIGraphLayout;
import com.mxgraph.util.mxCellRenderer;
import org.jgrapht.Graph;
import org.jgrapht.ext.JGraphXAdapter;
import tools.BinaryTreeEdge;
import tools.Huffman;
import tools.Node;
import tools.ReplacementNode;

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
    private int addedBits;
    private String codedText;
    private String inputModelView;
    private ArrayList<Node> nodes = new ArrayList<>();
    private ReplacementNode treeRoot;
    private String treeGraph;
    private String codingTable;

    public void splitInput() {

        StringBuilder sb = new StringBuilder(inputText);
        StringBuilder msb = new StringBuilder();
        while(true) {
            try {
                String line = sb.substring(0, sb.indexOf("\r\n"));
                if(line.length() == 0) line = sb.substring(0, sb.indexOf("\r\n", 1));

                if(line.matches("^.[0-9]+")) {
                    Node n = new Node(line.charAt(0));
                    n.setOccurrences(Integer.valueOf(line.substring(1, sb.indexOf("\r\n"))));
                    nodes.add(n);
                    msb.append(line.charAt(0));
                    msb.append(":");
                    msb.append(line.substring(1, sb.indexOf("\r\n")));
                    msb.append("\r\n");
                    sb.delete(0, sb.indexOf("\r\n") + 2);
                }
                else if(line.matches("(NL|CR|SP|HT)[0-9]+")) {
                    String weirdCharacter = line.substring(0, 2);
                    Node n;
                    switch(weirdCharacter) {
                        case "NL":
                            n = new Node((char)10);
                            break;
                        case "CR":
                            n = new Node((char)13);
                            break;
                        case "SP":
                            n = new Node((char)32);
                            break;
                        case "HT":
                            n = new Node((char)9);
                            break;
                        default:
                            n = new Node('x');
                    }
                    n.setIsWhiteSpace(true);
                    n.setOccurrences(Integer.valueOf(line.substring(2, sb.indexOf("\r\n"))));
                    nodes.add(n);
                    msb.append(line.substring(0, 2));
                    msb.append(":");
                    msb.append(line.substring(2, sb.indexOf("\r\n")));
                    msb.append("\n");
                    sb.delete(0, sb.indexOf("\r\n", 1) + 2);
                }
                else {
                    break;
                }
            }
            catch (StringIndexOutOfBoundsException e) {
                break;
            }
        }

        addedBits = (int) (sb.charAt(0) - 64);
        inputModelView = msb.toString();
        codedText = sb.substring(1);

    }

    public void decodeText() {

        char codedTextCharArr[] = codedText.toCharArray();
        StringBuilder buffer = new StringBuilder(codedText);

        for(int i = 1; i < addedBits; i++) buffer.deleteCharAt(buffer.length() - 1);
        this.treeRoot = Huffman.rebuildTree(nodes);
        Huffman.createCodingSequences(treeRoot, "");
        codingTable = Huffman.saveCodingTable(nodes);
        this.outputText = Huffman.decodeText(buffer, this.treeRoot);

    }


    public void generateTreeGraph() {

        Graph<String, BinaryTreeEdge> g = Huffman.generateTreeGraph(treeRoot);

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

    public int getAddedBits() {
        return addedBits;
    }

    public String getCodingTable() {
        return codingTable;
    }
}
