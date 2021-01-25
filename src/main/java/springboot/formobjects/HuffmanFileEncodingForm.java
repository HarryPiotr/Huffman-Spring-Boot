package springboot.formobjects;

import tools.huffman.file.*;

import javax.servlet.ServletOutputStream;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

public class HuffmanFileEncodingForm {

    private InputStream input;
    private OutputStream output;
    private ArrayList<Node> nodes;
    private ReplacementNode treeRoot;
    private int outputFileSize;
    private int addedBits;
    private int occurenceBytes;

    public double entropy;
    public double avgCodeWordLength;
    public long fileSize;
    public double compressionRatio;

    public int calculateExpectedLength() throws IOException {

        nodes = FileTools.countSymbols(input);
        nodes.sort(Node.NodeOccurancesComparator);
        treeRoot = FileTools.buildTree(nodes);
        FileTools.createCodingSequences(treeRoot, "");
        int[] res = FileTools.calculateCompressedFileSize(nodes);
        outputFileSize = res[0];
        addedBits = res[1];
        occurenceBytes = res[2];

        return outputFileSize;
    }

    public void compressFile() throws IOException {

        FileTools.codeFile(input, output, nodes, addedBits, occurenceBytes);

    }

    public void calculateTestedData() {

        double result = 0.0;
        for(Node n : nodes) {
            double p = ((double) n.getOccurrences()) / ((double) fileSize);
            result += p * Math.log(p) / Math.log(2);
        }
        result *= -1;
        entropy = result;

        long totalBits = 0;
        for(Node n : nodes) totalBits += n.getCodingSequence().length() * n.getOccurrences();
        totalBits -= addedBits;
        avgCodeWordLength = (double) totalBits / (double) fileSize;
        compressionRatio =  (double) outputFileSize / (double) fileSize;
    }

    public InputStream getInput() {
        return input;
    }

    public void setInput(InputStream inputStream) {
        this.input = new BufferedInputStream(inputStream);
    }

    public OutputStream getOutput() {
        return output;
    }

    public void setOutput(OutputStream outputStream) {
        this.output = outputStream;
    }

    public int getOutputFileSize() {
        return outputFileSize;
    }

    public void setOutputFileSize(int outputFileSize) {
        this.outputFileSize = outputFileSize;
    }
}
