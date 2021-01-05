package springboot;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import springboot.formobjects.HuffmanFileCodingForm;
import springboot.formobjects.HuffmanFileDecodingForm;
import springboot.formobjects.HuffmanTextCodingForm;
import springboot.formobjects.HuffmanTextDecodingForm;

import javax.servlet.http.HttpServletResponse;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Controller
public class AppController {

    private String appMode;

    @Autowired
    public AppController(Environment environment){
        appMode = environment.getProperty("app-mode");
    }

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("huffmanTextCodingForm", new HuffmanTextCodingForm());
        return "index";
    }

    @GetMapping("/index")
    public String index2(Model model) {
        model.addAttribute("huffmanTextCodingForm", new HuffmanTextCodingForm());
        return "index";
    }


    @PostMapping("/index")
    public String indexPost(@ModelAttribute HuffmanTextCodingForm hf, Model model) {

        model.addAttribute("huffmanTextCodingForm", hf);
        hf.generateModel();
        hf.generateCodingTable();
        hf.generateUncompressedText();
        hf.generateCompressedText();
        hf.generateTreeGraph();
        return "index_post";
    }

    @GetMapping("/text_decompression")
    public String textDecompression(Model model) {
        model.addAttribute("huffmanTextDecodingForm", new HuffmanTextDecodingForm());
        return "text_decompression";
    }

    @PostMapping("/text_decompression")
    public String textDecompressionPost(@ModelAttribute HuffmanTextDecodingForm hf, Model model) {

        model.addAttribute("huffmanTextDecodingForm", hf);
        hf.splitInput();
        hf.decodeText();
        hf.generateTreeGraph();
        return "text_decompression_post";
    }

    @GetMapping("/file_compression")
    public String fileCompressionGet(Model model) {
        return "file_compression";
    }

    @PostMapping("file_compression")
    public void fileCompressionPost(@RequestParam("file") MultipartFile file, Model model, HttpServletResponse response) throws IOException {

        HuffmanFileCodingForm hf = new HuffmanFileCodingForm();
        String filename = StringUtils.cleanPath(file.getOriginalFilename());

        hf.setInput(file.getInputStream());

        int returnSize = hf.calculateExpectedLength();

        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + filename + ".huff");
        response.setHeader(HttpHeaders.CONNECTION, "Keep-Alive");
        response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
        response.setContentLength(returnSize);
        hf.setOutput(response.getOutputStream());
        hf.compressFile();
        response.flushBuffer();

    }

    @GetMapping("/file_decompression")
    public String fileDecompressionGet(Model model) {
        return "file_decompression";
    }

    @PostMapping("file_decompression")
    public void fileDecompressionPost(@RequestParam("file") MultipartFile file, Model model, HttpServletResponse response) throws IOException {

        HuffmanFileDecodingForm hf = new HuffmanFileDecodingForm();
        String filename = StringUtils.cleanPath(file.getOriginalFilename());

        hf.setInput(file.getInputStream());

        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + filename.substring(0, filename.indexOf(".huff")));
        response.setHeader(HttpHeaders.CONNECTION, "Keep-Alive");
        response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
        hf.setOutput(response.getOutputStream());
        hf.decompressFile();
        response.flushBuffer();
    }

}
