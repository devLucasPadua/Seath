package com.example.seath.controller;

import com.example.seath.model.Pregao;
import com.example.seath.repository.PregaoRepository;
import com.example.seath.service.PregaoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class PregaoController {

    private final PregaoService pregaoService;
    private final PregaoRepository pregaoRepository;

    @Autowired
    public PregaoController(PregaoService pregaoService, PregaoRepository pregaoRepository) {
        this.pregaoService = pregaoService;
        this.pregaoRepository = pregaoRepository;
    }

    @PostMapping("/pregoes/salvar")
    public String salvarPregao(@RequestParam("nomePregao") String nomePregao,
                               @RequestParam("processoPregao") String processoPregao,
                               @RequestParam("descricaoPregao") String descricaoPregao,
                               RedirectAttributes redirectAttributes) {

        // 1. Validar campos vazios
        if (nomePregao == null || nomePregao.trim().isEmpty() ||
            processoPregao == null || processoPregao.trim().isEmpty() ||
            descricaoPregao == null || descricaoPregao.trim().isEmpty()) {
            
            redirectAttributes.addFlashAttribute("error", "Todos os campos para cadastrar o pregão devem ser preenchidos.");
            return "redirect:/";
        }

        // 2. Verificar duplicidade
        if (pregaoRepository.existsByNomeIgnoreCase(nomePregao.trim())) {
            redirectAttributes.addFlashAttribute("error", "Este Número de Pregão já está cadastrado.");
            return "redirect:/";
        }
        if (pregaoRepository.existsByProcessoIgnoreCase(processoPregao.trim())) {
            redirectAttributes.addFlashAttribute("error", "Este Número de Processo já está cadastrado.");
            return "redirect:/";
        }
        if (pregaoRepository.existsByDescricaoIgnoreCase(descricaoPregao.trim())) {
            redirectAttributes.addFlashAttribute("error", "Este Nome/Descrição de Pregão já está cadastrado.");
            return "redirect:/";
        }
        
        // 3. Se tudo estiver OK, salvar
        Pregao novoPregao = new Pregao();
        novoPregao.setNome(nomePregao.trim());
        novoPregao.setProcesso(processoPregao.trim());
        novoPregao.setDescricao(descricaoPregao.trim());
        pregaoService.salvar(novoPregao);

        redirectAttributes.addFlashAttribute("success", "Pregão cadastrado com sucesso!");
        return "redirect:/";
    }
}