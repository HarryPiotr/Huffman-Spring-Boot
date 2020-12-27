package springboot.formobjects;

import org.jgrapht.Graph;
import tools.BinaryNode;
import tools.BinaryTreeEdge;
import tools.Huffman;
import tools.ReplacementBinaryNode;

import java.io.File;
import java.util.ArrayList;

public class HuffmanFileDecodingForm {

    private File file;
    private File outputFile;
    private ArrayList<BinaryNode> nodes;
    private ReplacementBinaryNode treeRoot;


    public void decompressFile() {

        nodes = Huffman.rebuildTree(file);
        treeRoot = Huffman.buildTreeBinary(nodes);
        Huffman.createCodingSequencesBinary(treeRoot, "");
        outputFile = Huffman.decodeFile(file, treeRoot);

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
