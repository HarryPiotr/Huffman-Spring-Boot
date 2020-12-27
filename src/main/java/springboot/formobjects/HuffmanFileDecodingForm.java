package springboot.formobjects;

import tools.huffman.file.*;
import java.io.File;
import java.util.ArrayList;

public class HuffmanFileDecodingForm {

    private File file;
    private File outputFile;
    private ArrayList<BinaryNode> nodes;
    private ReplacementBinaryNode treeRoot;


    public void decompressFile() {

        nodes = FileTools.rebuildTree(file);
        treeRoot = FileTools.buildTreeBinary(nodes);
        FileTools.createCodingSequencesBinary(treeRoot, "");
        outputFile = FileTools.decodeFile(file, treeRoot);

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
