package com.fibra.backend2.services;

import com.fibra.backend2.dto.CalleDTO;
import com.fibra.backend2.repositories.CalleRepository;

import java.util.List;

public class CalleService {

    private final CalleRepository calleRepository;

    public CalleService(CalleRepository calleRepository) {
        this.calleRepository = calleRepository;
    }

    public List<CalleDTO> obtenerCalles() {
        return calleRepository.listarCalles();
    }
}
