package com.gestorPedidos.service.impl;

import com.gestorPedidos.dto.PedidoDTO;
import com.gestorPedidos.dto.ResumenDTO;
import com.gestorPedidos.exception.RecursoNoEncontradoException;
import com.gestorPedidos.exception.ReglaNegocioException;
import com.gestorPedidos.model.Cliente;
import com.gestorPedidos.model.Pedido;
import com.gestorPedidos.repository.ClienteRepository;
import com.gestorPedidos.repository.PedidoRepository;
import com.gestorPedidos.service.PedidoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class PedidoServiceImpl implements PedidoService {

    private static final Logger log = LoggerFactory.getLogger(PedidoServiceImpl.class);

    private final PedidoRepository pedidoRepository;
    private final ClienteRepository clienteRepository;

    public PedidoServiceImpl(PedidoRepository pedidoRepository,
                             ClienteRepository clienteRepository) {
        this.pedidoRepository = pedidoRepository;
        this.clienteRepository = clienteRepository;
    }

    @Override
    @Transactional
    public PedidoDTO crear(PedidoDTO dto) {
        // Regla 1: el cliente debe existir (integridad referencial)
        Cliente cliente = clienteRepository.findById(dto.getClienteId())
                .orElseThrow(() -> new ReglaNegocioException(
                        "No existe el cliente con id " + dto.getClienteId()));

        // Regla 2: un cliente inactivo no puede generar pedidos
        if (Boolean.FALSE.equals(cliente.getActivo())) {
            throw new ReglaNegocioException("El cliente " + cliente.getNombre()
                    + " esta inactivo y no puede generar pedidos");
        }

        Pedido pedido = new Pedido();
        pedido.setCliente(cliente);
        pedido.setDescripcion(dto.getDescripcion().trim());
        pedido.setCantidad(dto.getCantidad());
        pedido.setValorTotal(dto.getValorTotal() != null ? dto.getValorTotal() : BigDecimal.ZERO);
        pedido.setFechaPedido(dto.getFechaPedido() != null
                ? dto.getFechaPedido() : LocalDateTime.now());
        pedido.setEntregado(dto.getEntregado() != null ? dto.getEntregado() : Boolean.FALSE);

        Pedido guardado = pedidoRepository.save(pedido);
        log.info("Pedido creado con id {} para el cliente {}",
                guardado.getId(), cliente.getId());
        return convertirADTO(guardado);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PedidoDTO> listar() {
        return pedidoRepository.findAllByOrderByIdAsc().stream()
                .map(this::convertirADTO).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PedidoDTO> listarPorCliente(Long clienteId) {
        if (!clienteRepository.existsById(clienteId)) {
            throw new RecursoNoEncontradoException("No existe el cliente con id " + clienteId);
        }
        return pedidoRepository.findByClienteIdOrderByIdAsc(clienteId).stream()
                .map(this::convertirADTO).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PedidoDTO> listarPendientes() {
        return pedidoRepository.findByEntregadoFalseOrderByFechaPedidoAsc().stream()
                .map(this::convertirADTO).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PedidoDTO buscarPorId(Long id) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No existe el pedido con id " + id));
        return convertirADTO(pedido);
    }

    @Override
    @Transactional
    public PedidoDTO marcarEntregado(Long id) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No existe el pedido con id " + id));

        if (Boolean.TRUE.equals(pedido.getEntregado())) {
            throw new ReglaNegocioException("El pedido " + id + " ya figura como entregado");
        }

        pedido.setEntregado(Boolean.TRUE);
        return convertirADTO(pedido);
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        if (!pedidoRepository.existsById(id)) {
            throw new RecursoNoEncontradoException("No existe el pedido con id " + id);
        }
        pedidoRepository.deleteById(id);
        log.info("Pedido {} eliminado", id);
    }

    @Override
    @Transactional(readOnly = true)
    public ResumenDTO obtenerResumen() {
        long totalPedidos = pedidoRepository.count();
        long entregados = pedidoRepository.countByEntregadoTrue();
        return new ResumenDTO(
                clienteRepository.count(),
                totalPedidos,
                entregados,
                totalPedidos - entregados,
                pedidoRepository.sumarValorTotal());
    }

    // ------------------------------------------------------------------

    private PedidoDTO convertirADTO(Pedido pedido) {
        PedidoDTO dto = new PedidoDTO();
        dto.setId(pedido.getId());
        dto.setClienteId(pedido.getCliente().getId());
        dto.setNombreCliente(pedido.getCliente().getNombre());
        dto.setDescripcion(pedido.getDescripcion());
        dto.setCantidad(pedido.getCantidad());
        dto.setValorTotal(pedido.getValorTotal());
        dto.setFechaPedido(pedido.getFechaPedido());
        dto.setEntregado(pedido.getEntregado());
        return dto;
    }
}
