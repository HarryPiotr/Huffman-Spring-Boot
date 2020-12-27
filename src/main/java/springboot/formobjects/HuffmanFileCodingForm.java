package springboot.formobjects;

import tools.huffman.file.*;
import java.io.File;
import java.util.ArrayList;

public class HuffmanFileCodingForm {

    private File file;
    private File outputFile;
    private ArrayList<BinaryNode> nodes;
    private ReplacementBinaryNode treeRoot;


    public void compressFile() {

        nodes = FileTools.countSymbols(file);
        FileTools.sortBinaryNodeList(nodes);
        treeRoot = FileTools.buildTreeBinary(nodes);
        FileTools.createCodingSequencesBinary(treeRoot, "");
        outputFile = FileTools.codeFile(nodes, file);

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

}
