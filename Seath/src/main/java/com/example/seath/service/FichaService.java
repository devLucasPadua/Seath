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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@Service
public class FichaService {

    private final FichaRepository fichaRepository;

    @Autowired
    public FichaService(FichaRepository fichaRepository) {
        this.fichaRepository = fichaRepository;
    }

    public List<Ficha> buscarTodas() {
        return fichaRepository.findAll();
    }

    @Transactional
    public Ficha salvar(Ficha ficha) {
        return fichaRepository.saveAndFlush(ficha);
    }
    
    public Ficha buscarPorId(Long id) {
        return fichaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Ficha não encontrada com o ID: " + id));
    }

    /**
     * Gera um arquivo DOCX a partir de um template e dos dados de uma Ficha.
     * @param id O ID da Ficha a ser exportada.
     * @return Um array de bytes contendo o arquivo DOCX gerado.
     */
    public byte[] gerarDocxFicha(Long id) throws IOException, XDocReportException {
        // Garanta que seu template esteja em 'src/main/resources/templates/docx/template_ficha.docx'
        // E que os placeholders nele usem a sintaxe ${PALAVRA}
        try (InputStream in = FichaService.class.getResourceAsStream("/templates/docx/template_ficha.docx")) {
            if (in == null) {
                throw new IOException("Template 'template_ficha.docx' não encontrado no classpath.");
            }

            IXDocReport report = XDocReportRegistry.getRegistry().loadReport(in, TemplateEngineKind.Freemarker);
            IContext context = report.createContext();
            Ficha ficha = buscarPorId(id);

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            Function<LocalDate, String> formatarData = (data) -> data != null ? data.format(formatter) : "";
            
            context.put("PREGAO", Optional.ofNullable(ficha.getPregao()).orElse(""));
            context.put("PROCESSO", Optional.ofNullable(ficha.getProcesso()).orElse(""));
            context.put("ENVIADO_PARA", Optional.ofNullable(ficha.getEnviadoPara()).orElse(""));
            context.put("RESPONSAVEL", Optional.ofNullable(ficha.getResponsavel()).orElse(""));
            context.put("LICITANTE", Optional.ofNullable(ficha.getLicitante()).orElse(""));
            context.put("ITEM", Optional.ofNullable(ficha.getItem()).orElse(""));
            context.put("MARCA", Optional.ofNullable(ficha.getMarca()).orElse(""));
            context.put("INFO_AMOSTRA", Optional.ofNullable(ficha.getInfoProduto()).orElse(""));
            context.put("MATERIAL", Optional.ofNullable(ficha.getMaterial()).orElse(""));
            context.put("MODELO", Optional.ofNullable(ficha.getModelo()).orElse(""));
            context.put("TAMANHO", Optional.ofNullable(ficha.getTamanho()).orElse(""));
            context.put("LOTE", Optional.ofNullable(ficha.getLote()).orElse(""));
            context.put("FAB", formatarData.apply(ficha.getFabricacao()));
            context.put("VAL", formatarData.apply(ficha.getValidade()));
            context.put("REF", Optional.ofNullable(ficha.getReferencia()).orElse(""));
            context.put("QUANTIDADE", Optional.ofNullable(ficha.getQuantidade()).orElse(0));
            context.put("AVALIACAO", Optional.ofNullable(ficha.getFormaAvaliacao()).orElse(""));
            context.put("DATA_ENVIO", formatarData.apply(ficha.getDataEnvio()));

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            report.process(context, out);
            return out.toByteArray();
        }
    }
}
