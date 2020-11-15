package springboot;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import springboot.formobjects.HuffmanTextCodingForm;
import springboot.formobjects.HuffmanTextDecodingForm;

import java.text.SimpleDateFormat;
import java.util.Calendar;


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
}
