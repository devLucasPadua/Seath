package com.example.seath.service;

import com.example.seath.model.Ficha;
import com.example.seath.repository.FichaRepository;
import fr.opensagres.xdocreport.core.XDocReportException;
import fr.opensagres.xdocreport.document.IXDocReport;
import fr.opensagres.xdocreport.document.registry.XDocReportRegistry;
import fr.opensagres.xdocreport.template.IContext;
import fr.opensagres.xdocreport.template.TemplateEngineKind;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.format.DateTimeFormatter; // <-- ADICIONAR ESTE IMPORT
import java.util.List;
import java.util.Optional;

@Service
public class FichaService {

    private final FichaRepository fichaRepository;

    @Autowired
    public FichaService(FichaRepository fichaRepository) {
        this.fichaRepository = fichaRepository;
    }

    // ... métodos buscarTodas(), salvar(), buscarPorId() ...
    @Transactional(readOnly = true)
    public List<Ficha> buscarTodas() {
        return fichaRepository.findAll();
    }

    @Transactional
    public Ficha salvar(Ficha ficha) {
        return fichaRepository.save(ficha);
    }
    
    @Transactional(readOnly = true)
    public Ficha buscarPorId(Long id) {
        return fichaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Ficha não encontrada com o ID: " + id));
    }


    public byte[] gerarDocxFicha(Long id) throws IOException, XDocReportException {
        try (InputStream in = FichaService.class.getResourceAsStream("/templates/docx/template_ficha.docx")) {
            if (in == null) {
                throw new IOException("Template 'template_ficha.docx' não encontrado no classpath.");
            }

            IXDocReport report = XDocReportRegistry.getRegistry().loadReport(in, TemplateEngineKind.Freemarker);
            IContext context = report.createContext();
            Ficha ficha = buscarPorId(id);

            // ### INÍCIO DA ALTERAÇÃO ###
            // 1. Criar o formatador de data
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

            // 2. Formatar as datas antes de enviá-las ao contexto
            String fabFormatada = ficha.getFabricacao() != null ? ficha.getFabricacao().format(formatter) : "";
            String valFormatada = ficha.getValidade() != null ? ficha.getValidade().format(formatter) : "";
            String dataEnvioFormatada = ficha.getDataEnvio() != null ? ficha.getDataEnvio().format(formatter) : "";
            
            // Mapeia os dados da Ficha para os placeholders do template
            context.put("PREGAO", Optional.ofNullable(ficha.getPregao()).orElse(""));
            context.put("PROCESSO", Optional.ofNullable(ficha.getProcesso()).orElse(""));
            // ... outros context.put ...
            context.put("LOTE", Optional.ofNullable(ficha.getLote()).orElse(""));
            
            context.put("FAB", fabFormatada); // <-- USAR DATA FORMATADA
            context.put("VAL", valFormatada); // <-- USAR DATA FORMATADA
            
            context.put("REF", Optional.ofNullable(ficha.getReferencia()).orElse(""));
            // ... outros context.put ...
            context.put("AVALIACAO", Optional.ofNullable(ficha.getFormaAvaliacao()).orElse(""));
            
            context.put("DATA_ENVIO", dataEnvioFormatada); // <-- USAR DATA FORMATADA
            // ### FIM DA ALTERAÇÃO ###

            // Preenchendo o resto para garantir que o código esteja completo
            context.put("ENVIADO_PARA", Optional.ofNullable(ficha.getEnviadoPara()).orElse(""));
            context.put("RESPONSAVEL", Optional.ofNullable(ficha.getResponsavel()).orElse(""));
            context.put("LICITANTE", Optional.ofNullable(ficha.getLicitante()).orElse(""));
            context.put("ITEM", Optional.ofNullable(ficha.getItem()).orElse(""));
            context.put("MARCA", Optional.ofNullable(ficha.getMarca()).orElse(""));
            context.put("INFO_AMOSTRA", Optional.ofNullable(ficha.getInfoProduto()).orElse(""));
            context.put("MATERIAL", Optional.ofNullable(ficha.getMaterial()).orElse(""));
            context.put("MODELO", Optional.ofNullable(ficha.getModelo()).orElse(""));
            context.put("TAMANHO", Optional.ofNullable(ficha.getTamanho()).orElse(""));
            context.put("QUANTIDADE", Optional.ofNullable(ficha.getQuantidade()).orElse(0));


            ByteArrayOutputStream out = new ByteArrayOutputStream();
            report.process(context, out);

            return out.toByteArray();
        }
    }
}