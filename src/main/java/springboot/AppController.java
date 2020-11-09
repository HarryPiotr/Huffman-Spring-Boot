package springboot;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.RequestContextHolder;
import springboot.formobjects.HuffmanForm;

import java.text.SimpleDateFormat;
import java.util.Calendar;


@Controller
public class AppController {

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("huffmanForm", new HuffmanForm());
        return "index";
    }

    @PostMapping("/")
    public String testaction(@ModelAttribute HuffmanForm hf, Model model) {

        model.addAttribute("huffmanForm", hf);
        hf.generateModel();
        hf.generateCodingTable();
        hf.generateUncompressedText();
        hf.generateCompressedText();
        hf.generateTreeGraph(new SimpleDateFormat("ddMMyyyy-HHmmss").format(Calendar.getInstance().getTime()));
        return "index_post";
    }
}
