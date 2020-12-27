package springboot;

import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.core.io.FileSystemResource;
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
import java.io.File;
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
    public ResponseEntity<InputStreamResource> fileCompressionPost(@RequestParam("file") MultipartFile file, Model model) throws IOException {

        HuffmanFileCodingForm hf = new HuffmanFileCodingForm();
        String filename = StringUtils.cleanPath(file.getOriginalFilename());
        Path path = Paths.get(filename);
        Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
        hf.setFile(path.toFile());
        hf.compressFile();

        InputStreamResource resource = new InputStreamResource(new FileInputStream(hf.getOutputFile()));
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + hf.getOutputFile().getName())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(hf.getOutputFile().length())
                .body(resource);
    }

    @GetMapping("/file_decompression")
    public String fileDecompressionGet(Model model) {
        return "file_decompression";
    }

    @PostMapping("file_decompression")
    public ResponseEntity<InputStreamResource> fileDecompressionPost(@RequestParam("file") MultipartFile file, Model model) throws IOException {

        HuffmanFileDecodingForm hf = new HuffmanFileDecodingForm();
        String filename = StringUtils.cleanPath(file.getOriginalFilename());
        Path path = Paths.get(filename);
        Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
        hf.setFile(path.toFile());
        hf.decompressFile();

        InputStreamResource resource = new InputStreamResource(new FileInputStream(hf.getOutputFile()));
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + hf.getOutputFile().getName())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(hf.getOutputFile().length())
                .body(resource);
    }

}
