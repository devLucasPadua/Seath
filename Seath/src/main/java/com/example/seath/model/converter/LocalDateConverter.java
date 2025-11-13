package com.example.seath.model.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Converter(autoApply = true)
public class LocalDateConverter implements AttributeConverter<LocalDate, String> {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE; // Usa o formato "yyyy-MM-dd"

    /**
     * Converte o objeto LocalDate do Java para uma String a ser salva no banco.
     * Este nome está correto.
     */
    @Override
    public String convertToDatabaseColumn(LocalDate localDate) {
        return Optional.ofNullable(localDate)
                .map(FORMATTER::format)
                .orElse(null);
    }

    /**
     * Converte a String lida do banco de dados de volta para um objeto LocalDate no Java.
     * ### CORREÇÃO APLICADA AQUI ###
     * O nome do método foi corrigido de 'convertToDatabaseColumnValue' para 'convertToEntityAttribute'.
     */
    @Override
    public LocalDate convertToEntityAttribute(String dbData) {
        return Optional.ofNullable(dbData)
                .filter(s -> !s.isBlank())
                .map(s -> LocalDate.parse(s, FORMATTER))
                .orElse(null);
    }
}