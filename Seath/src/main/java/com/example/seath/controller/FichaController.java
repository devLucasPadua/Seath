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

@Controller
public class FichaController {

    private final FichaService fichaService;

    @Autowired
    public FichaController(FichaService fichaService) {
        this.fichaService = fichaService;
    }

    @GetMapping("/")
    public String paginaInicial(Model model) {
        model.addAttribute("listaFichas", fichaService.buscarTodas());
        model.addAttribute("fichaForm", new Ficha());
        return "index";
    }

    @PostMapping("/salvar")
    public String salvarFicha(@ModelAttribute("fichaForm") Ficha ficha) {
        fichaService.salvar(ficha);
        return "redirect:/";
    }

    /**
     * Lida com a requisição GET para exportar uma ficha como DOCX.
     *
     * @param id O ID da ficha a ser exportada, vindo da URL.
     * @param response O objeto de resposta HTTP para enviar o arquivo.
     */

    // ### NOVO MÉTODO 1: MOSTRAR A TELA DE EDIÇÃO ###
    /**
     * Lida com a requisição GET para /editar/{id}. Busca a ficha pelo ID e a
     * envia para a página de edição.
     */
    @GetMapping("/editar/{id}")
    public String mostrarFormularioDeEdicao(@PathVariable("id") Long id, Model model) {
        try {
            Ficha ficha = fichaService.buscarPorId(id);
            model.addAttribute("ficha", ficha);
            return "editar_ficha"; // Nome do arquivo HTML de edição
        } catch (Exception e) {
            // Se a ficha não for encontrada, redireciona para a página principal
            return "redirect:/";
        }
    }

    // ### NOVO MÉTODO 2: PROCESSAR A ATUALIZAÇÃO ###
    /**
     * Lida com a requisição POST de /atualizar. Recebe o objeto Ficha
     * modificado e o salva.
     */
    @PostMapping("/atualizar")
    public String atualizarFicha(@ModelAttribute("ficha") Ficha ficha) {
        // O método 'salvar' do service serve tanto para criar (se o ID for nulo)
        // quanto para atualizar (se o ID já existir).
        fichaService.salvar(ficha);
        return "redirect:/"; // Redireciona para a lista principal
    }

    @GetMapping("/exportar/{id}")
    public void exportarFicha(@PathVariable("id") Long id, HttpServletResponse response) {
        try {
            Ficha ficha = fichaService.buscarPorId(id);
            byte[] docxBytes = fichaService.gerarDocxFicha(id);

            // Cria um nome de arquivo seguro, substituindo caracteres inválidos
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
