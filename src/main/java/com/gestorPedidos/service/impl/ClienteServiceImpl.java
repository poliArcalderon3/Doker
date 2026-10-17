package com.gestorPedidos.service.impl;

import com.gestorPedidos.dto.ClienteDTO;
import com.gestorPedidos.exception.RecursoNoEncontradoException;
import com.gestorPedidos.exception.ReglaNegocioException;
import com.gestorPedidos.model.Cliente;
import com.gestorPedidos.repository.ClienteRepository;
import com.gestorPedidos.service.ClienteService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
@Service
public class ClienteServiceImpl implements ClienteService {
    private static final Logger log = LoggerFactory.getLogger(ClienteServiceImpl.class);

    private final ClienteRepository clienteRepository;

    /** Inyeccion de dependencias por constructor. */
    public ClienteServiceImpl(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    @Override
    @Transactional
    public ClienteDTO crear(ClienteDTO dto) {
        // Regla 1: el email no se puede repetir
        if (clienteRepository.existsByEmail(dto.getEmail())) {
            throw new ReglaNegocioException(
                    "Ya existe un cliente registrado con el email " + dto.getEmail());
        }
        // Regla 2: el documento tampoco se puede repetir
        if (clienteRepository.existsByDocumento(dto.getDocumento())) {
            throw new ReglaNegocioException(
                    "Ya existe un cliente con el documento " + dto.getDocumento());
        }

        Cliente cliente = convertirAEntidad(dto);
        Cliente guardado = clienteRepository.save(cliente);
        log.info("Cliente creado con id {}", guardado.getId());
        return convertirADTO(guardado, 0);
    }

    @Override
    @Transactional
    public ClienteDTO actualizar(Long id, ClienteDTO dto) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No existe el cliente con id " + id));

        // Si cambia el email, se verifica que el nuevo no este tomado
        if (!cliente.getEmail().equals(dto.getEmail())
                && clienteRepository.existsByEmail(dto.getEmail())) {
            throw new ReglaNegocioException(
                    "El email " + dto.getEmail() + " ya esta en uso por otro cliente");
        }

        cliente.setNombre(dto.getNombre().trim());
        cliente.setEmail(dto.getEmail().trim());
        cliente.setDocumento(dto.getDocumento().trim());
        cliente.setFechaNacimiento(dto.getFechaNacimiento());
        cliente.setActivo(dto.getActivo() != null ? dto.getActivo() : Boolean.TRUE);
        cliente.setSaldo(dto.getSaldo() != null ? dto.getSaldo() : BigDecimal.ZERO);

        // Al estar dentro de una transaccion, Hibernate detecta el cambio
        // y genera el UPDATE automaticamente (dirty checking).
        return convertirADTO(cliente, cliente.getPedidos().size());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClienteDTO> listar() {
        return clienteRepository.buscarTodosConPedidos().stream()
                .map(c -> convertirADTO(c, c.getPedidos().size()))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClienteDTO> listarActivos() {
        return clienteRepository.findByActivoTrueOrderByNombreAsc().stream()
                .map(c -> convertirADTO(c, null))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ClienteDTO buscarPorId(Long id) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No existe el cliente con id " + id));
        return convertirADTO(cliente, cliente.getPedidos().size());
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        if (!clienteRepository.existsById(id)) {
            throw new RecursoNoEncontradoException("No existe el cliente con id " + id);
        }
        // El cascade de la entidad borra tambien los pedidos asociados
        clienteRepository.deleteById(id);
        log.info("Cliente {} eliminado junto con sus pedidos", id);
    }

    @Override
    @Transactional(readOnly = true)
    public long contar() {
        return clienteRepository.count();
    }

    // ------------------------------------------------------------------
    // Conversiones entre entidad y DTO
    // ------------------------------------------------------------------

    private Cliente convertirAEntidad(ClienteDTO dto) {
        Cliente cliente = new Cliente();
        cliente.setNombre(dto.getNombre().trim());
        cliente.setEmail(dto.getEmail().trim());
        cliente.setDocumento(dto.getDocumento().trim());
        cliente.setFechaNacimiento(dto.getFechaNacimiento());
        cliente.setActivo(dto.getActivo() != null ? dto.getActivo() : Boolean.TRUE);
        cliente.setSaldo(dto.getSaldo() != null ? dto.getSaldo() : BigDecimal.ZERO);
        return cliente;
    }

    private ClienteDTO convertirADTO(Cliente cliente, Integer totalPedidos) {
        ClienteDTO dto = new ClienteDTO();
        dto.setId(cliente.getId());
        dto.setNombre(cliente.getNombre());
        dto.setEmail(cliente.getEmail());
        dto.setDocumento(cliente.getDocumento());
        dto.setFechaNacimiento(cliente.getFechaNacimiento());
        dto.setActivo(cliente.getActivo());
        dto.setSaldo(cliente.getSaldo());
        dto.setTotalPedidos(totalPedidos);
        return dto;
    }
}
