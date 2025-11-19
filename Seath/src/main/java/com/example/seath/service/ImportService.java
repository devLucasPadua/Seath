package com.example.seath.service;

import com.example.seath.model.Empresa;
import com.example.seath.model.Ficha;
import com.example.seath.model.Pregao;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.io.InputStream;
import java.util.Iterator;

@Service
public class ImportService {

    @Autowired private FichaService fichaService;
    @Autowired private PregaoService pregaoService;
    @Autowired private EmpresaService empresaService;

    @Transactional
    public void importarPlanilha(InputStream inputStream) throws Exception {
        Workbook workbook = new XSSFWorkbook(inputStream);
        Sheet sheet = workbook.getSheet("Amostras"); // Busca a aba específica

        if (sheet == null) {
            throw new Exception("Aba 'Amostras' não encontrada na planilha.");
        }

        Iterator<Row> rows = sheet.iterator();
        
        // Pula a primeira linha (cabeçalho)
        if (rows.hasNext()) {
            rows.next();
        }

        while (rows.hasNext()) {
            Row currentRow = rows.next();
            
            // Supondo a ordem das colunas. Ajuste os números se necessário.
            // Ex: getCell(0) para a primeira coluna (A), getCell(1) para a segunda (B), etc.
            String numPregao = getCellValue(currentRow.getCell(0));
            String numProcesso = getCellValue(currentRow.getCell(1));
            String descPregao = getCellValue(currentRow.getCell(2)); // Supondo que a descrição do pregão esteja na coluna C
            String marca = getCellValue(currentRow.getCell(3));
            String descProduto = getCellValue(currentRow.getCell(4));
            String licitante = getCellValue(currentRow.getCell(5));
            String status = getCellValue(currentRow.getCell(6)); // APROVADO ou REPROVADO

            // Validação simples: ignora a linha se campos essenciais estiverem vazios
            if (numPregao.isEmpty() || marca.isEmpty() || descProduto.isEmpty() || licitante.isEmpty()) {
                continue;
            }

            // Lógica para encontrar ou criar as entidades (simplificada por enquanto)
            // Futuramente, isso usará o "findOrCreate"
            Pregao pregao = new Pregao();
            pregao.setNome(numPregao);
            pregao.setProcesso(numProcesso);
            pregao.setDescricao(descPregao);
            // pregaoService.salvar(pregao); // Desativado para evitar duplicatas por enquanto

            Empresa empresa = new Empresa();
            empresa.setNome(licitante);
            // empresaService.salvar(empresa); // Desativado para evitar duplicatas

            // Cria a Ficha de histórico
            Ficha ficha = new Ficha();
            ficha.setPregao(numPregao);
            ficha.setProcesso(numProcesso);
            ficha.setNomePregao(descPregao);
            ficha.setMarca(marca);
            ficha.setInfoProduto(descProduto);
            ficha.setLicitante(licitante);
            // ficha.setStatusAnalise(status); // Futuramente, adicionaremos este campo à Ficha
            
            fichaService.salvar(ficha);
        }

        workbook.close();
    }

    private String getCellValue(Cell cell) {
        if (cell == null) {
            return "";
        }
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                return String.valueOf(cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            default:
                return "";
        }
    }
}