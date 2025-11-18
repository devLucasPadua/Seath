package com.example.seath.controller;

import com.example.seath.model.Empresa;
import com.example.seath.repository.EmpresaRepository;
import com.example.seath.service.EmpresaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api/empresas")
public class EmpresaRestController {

    private final EmpresaService empresaService;
    private final EmpresaRepository empresaRepository;

    @Autowired
    public EmpresaRestController(EmpresaService empresaService, EmpresaRepository empresaRepository) {
        this.empresaService = empresaService;
        this.empresaRepository = empresaRepository;
    }

    @PostMapping("/salvar")
    public String salvarEmpresa(@ModelAttribute Empresa empresa, Model model) {
        if (empresa.isEmpty()) {
            model.addAttribute("error", "Pelo menos um campo (Nome ou CNPJ) deve ser preenchido.");
            return "fragments/alert :: alert-component";
        }
        if (empresa.getNome() != null && !empresa.getNome().isBlank() && empresaRepository.existsByNomeIgnoreCase(empresa.getNome())) {
            model.addAttribute("error", "Uma empresa com este nome já está cadastrada.");
            return "fragments/alert :: alert-component";
        }
        if (empresa.getCnpj() != null && !empresa.getCnpj().isBlank() && empresaRepository.existsByCnpjIgnoreCase(empresa.getCnpj())) {
            model.addAttribute("error", "Uma empresa com este CNPJ já está cadastrada.");
            return "fragments/alert :: alert-component";
        }

        Empresa empresaSalva = empresaService.salvar(empresa);
        model.addAttribute("success", "Empresa '" + empresaSalva.getNome() + "' salva com sucesso!");
        return "fragments/alert :: alert-component";
    }
}