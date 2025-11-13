package com.example.seath.controller;

import com.example.seath.model.Ficha;
import com.example.seath.service.FichaService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import java.io.OutputStream;
import java.util.ArrayList;

@Controller
public class FichaController {

    private final FichaService fichaService;

    @Autowired
    public FichaController(FichaService fichaService) {
        this.fichaService = fichaService;
    }

    @GetMapping("/")
    public String paginaInicial(Model model) {
        //model.addAttribute("listaFichas", fichaService.buscarTodas());
        model.addAttribute("fichaForm", new Ficha());
        
        model.addAttribute("listaFichas", new ArrayList<>());
        return "index";
    }
    
    @GetMapping("/test")
    public String paginaDeTeste(Model model) {
        model.addAttribute("message", "Se esta mensagem aparecer em verde, o Thymeleaf está funcionando!");
        return "test"; // <-- Pede para renderizar o arquivo 'test.html'
    }


    @PostMapping("/salvar")
    public String salvarFicha(@ModelAttribute("fichaForm") Ficha ficha) {
        fichaService.salvar(ficha);
        return "redirect:/";
    }

    @GetMapping("/exportar/{id}")
    public void exportarFicha(@PathVariable("id") Long id, HttpServletResponse response) {
        try {
            Ficha ficha = fichaService.buscarPorId(id);
            byte[] docxBytes = fichaService.gerarDocxFicha(id);
            String processo = ficha.getProcesso() != null ? ficha.getProcesso().replaceAll("[^a-zA-Z0-9.-]", "_") : "proc";
            String item = ficha.getItem() != null ? ficha.getItem().replaceAll("[^a-zA-Z0-9.-]", "_") : "item";
            String nomeArquivo = "Ficha_Proc_" + processo + "_Item_" + item + ".docx";
            response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + nomeArquivo + "\"");
            response.setContentLength(docxBytes.length);
            try (OutputStream outStream = response.getOutputStream()) {
                outStream.write(docxBytes);
                outStream.flush();
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}
