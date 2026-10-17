package com.gestorPedidos.repository;

import com.gestorPedidos.model.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface PedidoRepository  extends JpaRepository<Pedido, Long> {

    List<Pedido> findByClienteIdOrderByIdAsc(Long clienteId);

    List<Pedido> findAllByOrderByIdAsc();

    List<Pedido> findByEntregadoFalseOrderByFechaPedidoAsc();

    long countByEntregadoTrue();

    @Query("SELECT COALESCE(SUM(p.valorTotal), 0) FROM Pedido p")
    BigDecimal sumarValorTotal();

    long countByClienteId(Long clienteId);

}
