package com.gestorPedidos.service;

import com.gestorPedidos.dto.ClienteDTO;

import java.util.List;

public interface ClienteService {

    ClienteDTO crear(ClienteDTO dto);

    ClienteDTO actualizar(Long id, ClienteDTO dto);

    List<ClienteDTO> listar();

    List<ClienteDTO> listarActivos();

    ClienteDTO buscarPorId(Long id);

    void eliminar(Long id);

    long contar();
}
