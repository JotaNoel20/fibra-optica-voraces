package com.fibra.backend2.services;

import com.fibra.backend2.dto.ConexionDTO;
import com.fibra.backend2.repositories.ConexionRepository;

import java.util.List;

public class ConexionService {

    private final ConexionRepository conexionRepository;

    public ConexionService(ConexionRepository conexionRepository) {
        this.conexionRepository = conexionRepository;
    }

    public ConexionDTO guardarConexion(ConexionDTO conexion) {
        return conexionRepository.guardar(conexion);
    }

    public List<ConexionDTO> listarConexiones() {
        return conexionRepository.listarTodas();
    }
}
