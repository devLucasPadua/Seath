package com.example.seath.service;

import com.example.seath.model.Ficha;
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

    @Autowired 
    private FichaService fichaService;

    // Método atualizado para receber os novos parâmetros
    @Transactional
    public void importarPlanilha(InputStream inputStream, String nomePregao, String numeroPregao) throws Exception {
        Workbook workbook = new XSSFWorkbook(inputStream);
        Sheet sheet = workbook.getSheet("Amostras");

        if (sheet == null) {
            workbook.close();
            throw new Exception("Aba 'Amostras' não encontrada na planilha.");
        }

        Iterator<Row> rows = sheet.iterator();
        
        if (rows.hasNext()) {
            rows.next(); // Pula cabeçalho
        }

        while (rows.hasNext()) {
            Row currentRow = rows.next();
            
            String item = getCellValue(currentRow.getCell(0));
            String infoProduto = getCellValue(currentRow.getCell(1));
            String licitante = getCellValue(currentRow.getCell(2));
            String marca = getCellValue(currentRow.getCell(3));
            String enviadoPara = getCellValue(currentRow.getCell(5));
            String responsavel = getCellValue(currentRow.getCell(6));

            if (infoProduto.isEmpty() || marca.isEmpty() || licitante.isEmpty()) {
                continue;
            }

            Ficha ficha = new Ficha();
            
            // ### DADOS DO NOME DO ARQUIVO APLICADOS AQUI ###
            ficha.setNomePregao(nomePregao);
            ficha.setPregao(numeroPregao);
            // O campo "processo" não foi extraído do nome do arquivo, então ficará nulo por enquanto.

            // Dados da planilha
            ficha.setItem(item);
            ficha.setInfoProduto(infoProduto);
            ficha.setLicitante(licitante);
            ficha.setMarca(marca);
            ficha.setEnviadoPara(enviadoPara);
            ficha.setResponsavel(responsavel);
            
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
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (cell.getNumericCellValue() == (long) cell.getNumericCellValue()) {
                    return String.format("%d", (long) cell.getNumericCellValue());
                } else {
                    return String.valueOf(cell.getNumericCellValue());
                }
            default:
                return "";
        }
    }
}