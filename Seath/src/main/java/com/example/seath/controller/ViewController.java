package com.example.seath.controller;

import com.example.seath.model.Empresa;
import com.example.seath.model.Ficha;
import com.example.seath.repository.EmpresaRepository;
import com.example.seath.repository.PregaoRepository;
import com.example.seath.service.EmpresaService;
import com.example.seath.service.FichaService;
import com.example.seath.service.PregaoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ViewController {

    @Autowired private FichaService fichaService;
    @Autowired private PregaoService pregaoService;
    @Autowired private EmpresaService empresaService;
    @Autowired private EmpresaRepository empresaRepository;
    @Autowired private PregaoRepository pregaoRepository;

    @GetMapping("/view/cadastrar")
    public String getFormularioCadastro(Model model) {
        model.addAttribute("listaPregoes", pregaoService.buscarTodos());
        model.addAttribute("listaEmpresas", empresaService.buscarTodas());
        model.addAttribute("fichaForm", new Ficha());
        return "fragments/form_cadastro :: formCadastro";
    }

    @GetMapping("/view/listar")
    public String getTabelaFichas(Model model) {
        model.addAttribute("listaFichas", fichaService.buscarTodas());
        return "fragments/tabela_fichas :: tabelaFichas";
    }
    
    @GetMapping("/view/empresas")
    public String getPaginaEmpresas(Model model) {
        model.addAttribute("listaEmpresas", empresaService.buscarTodas());
        model.addAttribute("empresaForm", new Empresa());
        return "fragments/gerenciar_empresas :: gerenciarEmpresas";
    }

    @GetMapping("/view/empresas/lista")
    public String getListaEmpresasFiltrada(@RequestParam(required = false) String termoBusca, Model model) {
        if (termoBusca != null && !termoBusca.isBlank()) {
            model.addAttribute("listaEmpresas", empresaRepository.findByNomeContainingIgnoreCaseOrCnpjContainingIgnoreCase(termoBusca, termoBusca));
        } else {
            model.addAttribute("listaEmpresas", empresaService.buscarTodas());
        }
        return "fragments/lista_empresas :: listaEmpresas";
    }

    @GetMapping("/view/pregoes/lista")
    public String getListaPregoesFiltrada(@RequestParam(required = false) String termoBusca, Model model) {
        if (termoBusca != null && !termoBusca.isBlank()) {
            model.addAttribute("listaPregoes", pregaoRepository.findByNomeContainingIgnoreCaseOrProcessoContainingIgnoreCaseOrDescricaoContainingIgnoreCase(termoBusca, termoBusca, termoBusca));
        } else {
            model.addAttribute("listaPregoes", pregaoService.buscarTodos());
        }
        return "fragments/lista_pregoes :: listaPregoes";
    }
}