package com.example.seath.service;

import com.example.seath.model.Empresa;
import com.example.seath.repository.EmpresaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class EmpresaService {

    private final EmpresaRepository empresaRepository;

    @Autowired
    public EmpresaService(EmpresaRepository empresaRepository) {
        this.empresaRepository = empresaRepository;
    }

    public List<Empresa> buscarTodas() {
        return empresaRepository.findAll();
    }

    @Transactional
    public Empresa salvar(Empresa empresa) {
        return empresaRepository.save(empresa);
    }
}