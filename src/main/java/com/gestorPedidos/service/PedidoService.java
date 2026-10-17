package com.gestorPedidos.service;

import com.gestorPedidos.dto.PedidoDTO;
import com.gestorPedidos.dto.ResumenDTO;

import java.util.List;

public interface PedidoService {

    PedidoDTO crear(PedidoDTO dto);

    List<PedidoDTO> listar();

    List<PedidoDTO> listarPorCliente(Long clienteId);

    List<PedidoDTO> listarPendientes();

    PedidoDTO buscarPorId(Long id);

    PedidoDTO marcarEntregado(Long id);

    void eliminar(Long id);

    ResumenDTO obtenerResumen();
}

