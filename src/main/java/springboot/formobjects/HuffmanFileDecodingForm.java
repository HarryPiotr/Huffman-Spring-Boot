package springboot.formobjects;

import tools.huffman.file.*;

import javax.servlet.ServletOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class HuffmanFileDecodingForm {

    private InputStream input;
    private ServletOutputStream output;
    private ArrayList<BinaryNode> nodes;
    private ReplacementBinaryNode treeRoot;
    private int addedBits;
    private int modelLines;
    private int occurenceBytes;


    public void decompressFile() throws IOException {

        int res[] = FileTools.readMetaData(input);
        addedBits = res[0];
        modelLines = res[1];
        occurenceBytes = res[2];

        nodes = FileTools.rebuildTree(input, modelLines, occurenceBytes);
        treeRoot = FileTools.buildTreeBinary(nodes);
        FileTools.decodeFile(input, output, treeRoot, addedBits);

    }

    public InputStream getInput() {
        return input;
    }

    public void setInput(InputStream input) {
        this.input = input;
    }

    public ServletOutputStream getOutput() {
        return output;
    }

    public void setOutput(ServletOutputStream output) {
        this.output = output;
    }
}
