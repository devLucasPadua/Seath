package com.example.seath.controller;

import com.example.seath.model.Empresa;
import com.example.seath.model.Ficha;
import com.example.seath.model.Pregao;
import com.example.seath.service.EmpresaService;
import com.example.seath.service.FichaService;
import com.example.seath.service.PregaoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ViewController {

    @Autowired private FichaService fichaService;
    @Autowired private PregaoService pregaoService;
    @Autowired private EmpresaService empresaService;

    // Serve o fragmento da tela principal de cadastro
    @GetMapping("/view/cadastrar")
    public String getPaginaCadastro(Model model) {
        if (!model.containsAttribute("fichaForm")) {
            model.addAttribute("fichaForm", new Ficha());
        }
        if (!model.containsAttribute("pregaoForm")) {
            model.addAttribute("pregaoForm", new Pregao());
        }
        if (!model.containsAttribute("empresaForm")) {
            model.addAttribute("empresaForm", new Empresa());
        }
        model.addAttribute("listaPregoes", pregaoService.buscarTodos());
        model.addAttribute("listaEmpresas", empresaService.buscarTodas());
        return "fragments/conteudo_cadastro :: conteudoCadastro";
    }

    // Serve o fragmento da tela com a lista de fichas
    @GetMapping("/view/listar")
    public String getPaginaListagem(Model model) {
        model.addAttribute("listaFichas", fichaService.buscarTodas());
        return "fragments/conteudo_listar :: conteudoListar";
    }
}