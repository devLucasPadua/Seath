package com.example.seath.controller;

import com.example.seath.model.Pregao;
import com.example.seath.repository.PregaoRepository;
import com.example.seath.service.PregaoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api/pregoes")
public class PregaoRestController {

    @Autowired
    private PregaoService pregaoService;
    @Autowired
    private PregaoRepository pregaoRepository;

    @PostMapping("/salvar")
    public String salvarPregao(@ModelAttribute Pregao pregao, Model model) {
        if (pregao.getNome() == null || pregao.getNome().trim().isEmpty() || pregao.getProcesso() == null || pregao.getProcesso().trim().isEmpty() || pregao.getDescricao() == null || pregao.getDescricao().trim().isEmpty()) {
            model.addAttribute("error", "Todos os campos do pregão devem ser preenchidos.");
            return "fragments/alert :: alert-component";
        }
        if (pregaoRepository.existsByNomeIgnoreCase(pregao.getNome().trim())) {
            model.addAttribute("error", "Este Número de Pregão já está cadastrado.");
            return "fragments/alert :: alert-component";
        }
        if (pregaoRepository.existsByProcessoIgnoreCase(pregao.getProcesso().trim())) {
            model.addAttribute("error", "Este Número de Processo já está cadastrado.");
            return "fragments/alert :: alert-component";
        }
        if (pregaoRepository.existsByDescricaoIgnoreCase(pregao.getDescricao().trim())) {
            model.addAttribute("error", "Este Nome/Descrição de Pregão já está cadastrado.");
            return "fragments/alert :: alert-component";
        }
        
        Pregao pregaoSalvo = pregaoService.salvar(pregao);
        model.addAttribute("success", "Pregão '" + pregaoSalvo.getNome() + "' salvo com sucesso!");
        return "fragments/alert :: alert-component";
    }
}