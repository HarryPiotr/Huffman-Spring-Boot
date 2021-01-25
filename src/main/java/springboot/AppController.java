package springboot;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import springboot.formobjects.HuffmanFileEncodingForm;
import springboot.formobjects.HuffmanFileDecodingForm;
import springboot.formobjects.HuffmanTextEncodingForm;
import springboot.formobjects.HuffmanTextDecodingForm;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Controller
public class AppController {

    private String appMode;

    @Autowired
    public AppController(Environment environment) {
        appMode = environment.getProperty("app-mode");
    }

    @GetMapping({"/", "/text_compression"})
    public String textCompressionGet(Model model) {

        model.addAttribute("form", new HuffmanTextEncodingForm());
        return "text_compression";
    }

    @PostMapping("/text_compression")
    public String textCompressionPost(@ModelAttribute HuffmanTextEncodingForm hf, Model model) throws IOException {

        model.addAttribute("form", hf);
        hf.generateModel();
        hf.generateCodingTable();
        hf.generateCompressedText();
        hf.generateTreeGraph();
        return "text_compression_post";
    }

    @GetMapping("/text_decompression")
    public String textDecompressionGet(Model model) {

        model.addAttribute("form", new HuffmanTextDecodingForm());
        return "text_decompression";
    }

    @PostMapping("/text_decompression")
    public String textDecompressionPost(@ModelAttribute HuffmanTextDecodingForm hf, Model model) throws IOException {

        model.addAttribute("form", hf);
        hf.splitInput();
        hf.decodeText();
        hf.generateTreeGraph();
        return "text_decompression_post";
    }

    @GetMapping("/file_compression")
    public String fileCompressionGet(Model model) {
        return "file_compression";
    }

    @PostMapping("/file_compression")
    public void fileCompressionPost(@RequestParam("file") MultipartFile file, Model model, HttpServletResponse response) throws IOException {

        String filename = StringUtils.cleanPath(file.getOriginalFilename());
        HuffmanFileEncodingForm hf = new HuffmanFileEncodingForm();
        hf.setInput(file.getInputStream());
        hf.setOutput(response.getOutputStream());

        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + filename + ".huff");
        response.setHeader(HttpHeaders.CONNECTION, "Keep-Alive");
        response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
        response.setContentLength(hf.calculateExpectedLength());
        hf.compressFile();
        response.flushBuffer();
    }

    @GetMapping("/file_decompression")
    public String fileDecompressionGet(Model model) {
        return "file_decompression";
    }

    @PostMapping("/file_decompression")
    public void fileDecompressionPost(@RequestParam("file") MultipartFile file, Model model, HttpServletResponse response) throws IOException {

        HuffmanFileDecodingForm hf = new HuffmanFileDecodingForm();
        String filename = StringUtils.cleanPath(file.getOriginalFilename());
        hf.setInput(file.getInputStream());
        hf.setOutput(response.getOutputStream());

        hf.rebuildTree();

        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + filename.substring(0, filename.indexOf(".huff")));
        response.setHeader(HttpHeaders.CONNECTION, "Keep-Alive");
        response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
        response.setContentLength(hf.getOutputFileSize());

        hf.decompressFile();
        response.flushBuffer();
    }
}
