package springboot.formobjects;

import org.jgrapht.Graph;
import org.springframework.web.multipart.MultipartFile;
import tools.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class HuffmanFileCodingForm {

    private File file;
    private File outputFile;
    private ArrayList<BinaryNode> nodes;
    private ReplacementBinaryNode treeRoot;
    private Graph<String, BinaryTreeEdge> treeGraph;


    public void compressFile() {

        nodes = Huffman.countSymbols(file);
        Huffman.sortBinaryNodeList(nodes);
        treeRoot = Huffman.buildTreeBinary(nodes);
        treeGraph = Huffman.generateTreeGraphBinary(treeRoot);
        Huffman.createCodingSequencesBinary(treeRoot, "");
        outputFile = Huffman.codeFile(nodes, file);

    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public File getOutputFile() {
        return outputFile;
    }

    public void setOutputFile(File outputFile) {
        this.outputFile = outputFile;
    }
}
