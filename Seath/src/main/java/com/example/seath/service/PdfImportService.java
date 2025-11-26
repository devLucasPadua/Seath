package com.example.seath.service;

import com.example.seath.model.Vencedor;
import com.example.seath.repository.VencedorRepository;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class PdfImportService {

    @Autowired
    private VencedorRepository vencedorRepository;

    @Transactional
    public void importarPdfVencedores(InputStream inputStream) throws Exception {
        // O try-with-resources garante que o PDF seja fechado após a leitura
        try (PDDocument document = PDDocument.load(inputStream)) {
            
            // Converte o conteúdo do PDF para Texto
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);

            // 1. Regex para extrair Número do Pregão
            // Procura por: "Registro de Preços Eletrônico - 234/2025"
            String numeroPregao = "Não identificado";
            Pattern patternPregao = Pattern.compile("Registro de Preços Eletrônico - (\\d+/\\d+)");
            Matcher matcherPregao = patternPregao.matcher(text);
            
            if (matcherPregao.find()) {
                numeroPregao = matcherPregao.group(1);
            }

            // 2. Regex para extrair Empresas e CNPJs
            // Procura por linhas que começam com o Nome, tem o separador "|", e depois "Documento XX.XXX.XXX/0001-XX"
            Pattern patternEmpresa = Pattern.compile("^(.*?)\\s+\\|\\s+Tipo:.*Documento\\s+(\\d{2}\\.\\d{3}\\.\\d{3}/\\d{4}-\\d{2})", Pattern.MULTILINE);
            Matcher matcherEmpresa = patternEmpresa.matcher(text);

            // Set para evitar duplicidade DENTRO do mesmo arquivo PDF que está sendo processado agora
            Set<String> cnpjsNoArquivoAtual = new HashSet<>();

            while (matcherEmpresa.find()) {
                String nome = matcherEmpresa.group(1).trim();
                String cnpj = matcherEmpresa.group(2).trim();

                // --- INÍCIO DA VERIFICAÇÃO DE DUPLICIDADE ---

                // A. Verifica se a empresa apareceu repetida neste mesmo PDF (ex: ganhou 2 itens)
                if (cnpjsNoArquivoAtual.contains(cnpj)) {
                    continue; // Pula, já lemos essa empresa neste arquivo
                }

                // B. Verifica se a empresa já existe no Banco de Dados de importações passadas
                if (vencedorRepository.existsByCnpj(cnpj)) {
                    continue; // Pula, já está cadastrada no banco
                }

                // --- FIM DA VERIFICAÇÃO ---

                // Adiciona ao controle local
                cnpjsNoArquivoAtual.add(cnpj);

                // Cria e Salva no Banco
                Vencedor vencedor = new Vencedor();
                vencedor.setNumeroPregao(numeroPregao);
                vencedor.setNomeEmpresa(nome);
                vencedor.setCnpj(cnpj);

                vencedorRepository.save(vencedor);
            }
        }
    }
}