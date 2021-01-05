package springboot.formobjects;

import tools.huffman.file.*;

import javax.servlet.ServletOutputStream;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class HuffmanFileCodingForm {

    private InputStream input;
    private ServletOutputStream output;
    private ArrayList<BinaryNode> nodes;
    private ReplacementBinaryNode treeRoot;
    private int outputFileSize;
    private int addedBits;
    private int occurenceBytes;

    public int calculateExpectedLength() throws IOException {

        nodes = FileTools.countSymbols(input);
        FileTools.sortBinaryNodeList(nodes);
        treeRoot = FileTools.buildTreeBinary(nodes);
        FileTools.createCodingSequencesBinary(treeRoot, "");
        int[] res = FileTools.calculateCompressedFileSize(nodes);
        outputFileSize = res[0];
        addedBits = res[1];
        occurenceBytes = res[2];

        System.out.println("Spodziewamy się " + outputFileSize + " bajtów");
        return outputFileSize;
    }

    public void compressFile() throws IOException {

        FileTools.codeFile(input, output, nodes, addedBits, occurenceBytes);

    }

    public InputStream getInput() {
        return input;
    }

    public void setInput(InputStream inputStream) {
        this.input = new BufferedInputStream(inputStream);
    }

    public ServletOutputStream getOutput() {
        return output;
    }

    public void setOutput(ServletOutputStream servletOutputStream) {
        this.output = servletOutputStream;
    }

    public int getOutputFileSize() {
        return outputFileSize;
    }

    public void setOutputFileSize(int outputFileSize) {
        this.outputFileSize = outputFileSize;
    }
}
