package com.gestorPedidos.controller;

import com.gestorPedidos.dto.PedidoDTO;
import com.gestorPedidos.dto.ResumenDTO;
import com.gestorPedidos.service.PedidoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pedidos")
@CrossOrigin(origins = "*")
public class PedidoController {

    private final PedidoService pedidoService;

    public PedidoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    /** GET /api/pedidos */
    @GetMapping
    public ResponseEntity<List<PedidoDTO>> listar() {
        return ResponseEntity.ok(pedidoService.listar());
    }

    /** GET /api/pedidos/pendientes */
    @GetMapping("/pendientes")
    public ResponseEntity<List<PedidoDTO>> listarPendientes() {
        return ResponseEntity.ok(pedidoService.listarPendientes());
    }

    /** GET /api/pedidos/cliente/{clienteId} */
    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<List<PedidoDTO>> listarPorCliente(@PathVariable Long clienteId) {
        return ResponseEntity.ok(pedidoService.listarPorCliente(clienteId));
    }

    /** GET /api/pedidos/resumen */
    @GetMapping("/resumen")
    public ResponseEntity<ResumenDTO> resumen() {
        return ResponseEntity.ok(pedidoService.obtenerResumen());
    }

    /** GET /api/pedidos/{id} */
    @GetMapping("/{id}")
    public ResponseEntity<PedidoDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(pedidoService.buscarPorId(id));
    }

    /** POST /api/pedidos */
    @PostMapping
    public ResponseEntity<PedidoDTO> crear(@Valid @RequestBody PedidoDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(pedidoService.crear(dto));
    }

    /** PATCH /api/pedidos/{id}/entregar */
    @PatchMapping("/{id}/entregar")
    public ResponseEntity<PedidoDTO> marcarEntregado(@PathVariable Long id) {
        return ResponseEntity.ok(pedidoService.marcarEntregado(id));
    }

    /** DELETE /api/pedidos/{id} */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        pedidoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
