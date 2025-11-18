package com.example.seath.controller;

import com.example.seath.model.Ficha;
import com.example.seath.service.FichaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class FichaController {

    private final FichaService fichaService;

    @Autowired
    public FichaController(FichaService fichaService) {
        this.fichaService = fichaService;
    }

    @PostMapping("/salvar")
    public String salvarFicha(@ModelAttribute("fichaForm") Ficha ficha, RedirectAttributes redirectAttributes) {
        if (ficha.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Não é possível salvar uma ficha vazia. Preencha pelo menos um campo.");
            return "redirect:/";
        }
        fichaService.salvar(ficha);
        redirectAttributes.addFlashAttribute("success", "Ficha salva com sucesso!");
        return "redirect:/";
    }

    @PostMapping("/atualizar")
    public String atualizarFicha(@ModelAttribute("ficha") Ficha ficha, RedirectAttributes redirectAttributes) {
        fichaService.salvar(ficha);
        redirectAttributes.addFlashAttribute("success", "Ficha atualizada com sucesso!");
        return "redirect:/";
    }
}