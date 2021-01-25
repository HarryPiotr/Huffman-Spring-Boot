package springboot.formobjects;

import tools.huffman.file.*;

import javax.servlet.ServletOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

public class HuffmanFileDecodingForm {

    private InputStream input;
    private OutputStream output;
    private ArrayList<Node> nodes;
    private ReplacementNode treeRoot;
    private int addedBits;
    private int modelLines;
    private int occurenceBytes;
    private int outputFileSize;

    public void rebuildTree() throws IOException {
        int res[] = FileTools.readMetaData(input);
        addedBits = res[0];
        modelLines = res[1];
        occurenceBytes = res[2];

        nodes = FileTools.retrieveModel(input, modelLines, occurenceBytes);
        outputFileSize = calculateExpectedLength();
        treeRoot = FileTools.buildTree(nodes);
    }

    public void decompressFile() throws IOException {
        FileTools.decodeFile(input, output, treeRoot, addedBits);
    }

    private int calculateExpectedLength() {
        int sum = 0;
        for(Node n : nodes) sum += n.getOccurrences();
        return sum;
    }

    public InputStream getInput() {
        return input;
    }

    public void setInput(InputStream input) {
        this.input = input;
    }

    public OutputStream getOutput() {
        return output;
    }

    public void setOutput(OutputStream output) {
        this.output = output;
    }

    public int getOutputFileSize() {
        return outputFileSize;
    }
}
