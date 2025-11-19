package com.example.seath.controller;

import com.example.seath.model.Empresa;
import com.example.seath.model.Ficha;
import com.example.seath.model.Pregao;
import com.example.seath.repository.EmpresaRepository;
import com.example.seath.repository.PregaoRepository;
import com.example.seath.service.EmpresaService;
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
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class FichaController {

    @Autowired private FichaService fichaService;
    @Autowired private PregaoService pregaoService;
    @Autowired private EmpresaService empresaService;
    @Autowired private PregaoRepository pregaoRepository;
    @Autowired private EmpresaRepository empresaRepository;

    @GetMapping("/")
    public String paginaInicial(Model model) {
        if (!model.containsAttribute("pregaoForm")) { model.addAttribute("pregaoForm", new Pregao()); }
        if (!model.containsAttribute("empresaForm")) { model.addAttribute("empresaForm", new Empresa()); }
        model.addAttribute("fichaForm", new Ficha());
        model.addAttribute("listaPregoes", pregaoService.buscarTodos());
        model.addAttribute("listaEmpresas", empresaService.buscarTodas());
        return "index";
    }

    // --- NOVOS ENDPOINTS DE BUSCA PARA O HTMX ---

    @GetMapping("/pregoes/buscar")
    public String buscarPregoes(@RequestParam("termoBusca") String termo, Model model) {
        if (termo.isEmpty()) {
            model.addAttribute("listaPregoes", pregaoRepository.findTop3ByOrderByIdDesc());
        } else {
            model.addAttribute("listaPregoes", pregaoRepository.findByNomeContainingIgnoreCaseOrProcessoContainingIgnoreCaseOrDescricaoContainingIgnoreCase(termo, termo, termo));
        }
        // Retorna apenas o fragmento da lista
        return "index :: #pregao-list-container";
    }

    @GetMapping("/empresas/buscar")
    public String buscarEmpresas(@RequestParam("termoBusca") String termo, Model model) {
        if (termo.isEmpty()) {
            model.addAttribute("listaEmpresas", empresaRepository.findTop3ByOrderByIdDesc());
        } else {
            model.addAttribute("listaEmpresas", empresaRepository.findByNomeContainingIgnoreCaseOrCnpjContainingIgnoreCase(termo, termo));
        }
        // Retorna apenas o fragmento da lista
        return "index :: #empresa-list-container";
    }    
    
    // ### NOVO MÉTODO PARA A PÁGINA DE LISTAGEM ###
    @GetMapping("/fichas")
    public String listarFichas(Model model) {
        model.addAttribute("listaFichas", fichaService.buscarTodas());
        return "listar_fichas"; // Aponta para o arquivo listar_fichas.html
    }

    // --- AÇÕES DA FICHA ---
    @PostMapping("/fichas/salvar")
    public String salvarFicha(@ModelAttribute("fichaForm") Ficha ficha, RedirectAttributes redirectAttributes) {
        if (ficha.isEmpty()) { redirectAttributes.addFlashAttribute("error", "Não é possível salvar uma ficha vazia."); return "redirect:/"; }
        fichaService.salvar(ficha);
        redirectAttributes.addFlashAttribute("success", "Ficha salva com sucesso!");
        return "redirect:/";
    }

    @GetMapping("/fichas/editar/{id}")
    public String mostrarFormularioDeEdicao(@PathVariable("id") Long id, Model model) {
        try {
            Ficha ficha = fichaService.buscarPorId(id);
            model.addAttribute("ficha", ficha);
            return "editar_ficha";
        } catch (Exception e) {
            return "redirect:/";
        }
    }

    @PostMapping("/fichas/atualizar")
    public String atualizarFicha(@ModelAttribute("ficha") Ficha ficha, RedirectAttributes redirectAttributes) {
        fichaService.salvar(ficha);
        redirectAttributes.addFlashAttribute("success", "Ficha atualizada com sucesso!");
        return "redirect:/fichas"; // Redireciona para a lista após editar
    }

    @GetMapping("/fichas/exportar/{id}")
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

    // --- AÇÕES DO PREGÃO E EMPRESA ---
    @PostMapping("/pregoes/salvar")
    public String salvarPregao(@ModelAttribute("pregaoForm") Pregao pregao, RedirectAttributes redirectAttributes) {
        // ... (código de salvar pregão)
        return "redirect:/";
    }

    @PostMapping("/empresas/salvar")
    public String salvarEmpresa(@ModelAttribute("empresaForm") Empresa empresa, RedirectAttributes redirectAttributes) {
        // ... (código de salvar empresa)
        return "redirect:/";
    }
}