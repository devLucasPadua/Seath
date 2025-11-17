package com.example.seath.controller;

import com.example.seath.model.Ficha;
import com.example.seath.service.FichaService;
import com.example.seath.service.PregaoService;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.io.OutputStream;

@Controller
public class FichaController {

    private final FichaService fichaService;
    private final PregaoService pregaoService;

    @Autowired
    public FichaController(FichaService fichaService, PregaoService pregaoService) {
        this.fichaService = fichaService;
        this.pregaoService = pregaoService;
    }

    @GetMapping("/")
    public String paginaInicial(Model model) {
        model.addAttribute("listaFichas", fichaService.buscarTodas());
        model.addAttribute("listaPregoes", pregaoService.buscarTodos());
        model.addAttribute("fichaForm", new Ficha());
        return "index";
    }

    @PostMapping("/salvar")
    public String salvarFicha(@ModelAttribute("fichaForm") Ficha ficha, RedirectAttributes redirectAttributes) {
        // Validação para impedir o salvamento de uma ficha vazia
        if (ficha.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Não é possível salvar uma ficha vazia. Preencha pelo menos um campo.");
            return "redirect:/";
        }
        
        fichaService.salvar(ficha);
        redirectAttributes.addFlashAttribute("success", "Ficha salva com sucesso!");
        return "redirect:/";
    }
    
    @GetMapping("/editar/{id}")
    public String mostrarFormularioDeEdicao(@PathVariable("id") Long id, Model model) {
        try {
            Ficha ficha = fichaService.buscarPorId(id);
            model.addAttribute("ficha", ficha);
            model.addAttribute("listaPregoes", pregaoService.buscarTodos());
            return "editar_ficha";
        } catch (Exception e) {
            return "redirect:/";
        }
    }

    @PostMapping("/atualizar")
    public String atualizarFicha(@ModelAttribute("ficha") Ficha ficha, RedirectAttributes redirectAttributes) {
        fichaService.salvar(ficha);
        redirectAttributes.addFlashAttribute("success", "Ficha atualizada com sucesso!");
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