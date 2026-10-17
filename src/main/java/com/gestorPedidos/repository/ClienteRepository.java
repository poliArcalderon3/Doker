package com.gestorPedidos.repository;

import com.gestorPedidos.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    Optional<Cliente> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByDocumento(String documento);
    List<Cliente> findByActivoTrueOrderByNombreAsc();
    @Query("SELECT DISTINCT c FROM Cliente c LEFT JOIN FETCH c.pedidos ORDER BY c.id")
    List<Cliente> buscarTodosConPedidos();

}
