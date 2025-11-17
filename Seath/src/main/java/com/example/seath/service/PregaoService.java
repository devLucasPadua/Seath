package com.example.seath.service;

import com.example.seath.model.Pregao;
import com.example.seath.repository.PregaoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class PregaoService {

    private final PregaoRepository pregaoRepository;

    @Autowired
    public PregaoService(PregaoRepository pregaoRepository) {
        this.pregaoRepository = pregaoRepository;
    }

    public List<Pregao> buscarTodos() {
        return pregaoRepository.findAll();
    }

    @Transactional
    public Pregao salvar(Pregao pregao) {
        return pregaoRepository.save(pregao);
    }
}