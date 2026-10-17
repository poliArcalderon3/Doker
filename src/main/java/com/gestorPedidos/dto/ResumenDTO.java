package com.gestorPedidos.dto;
import java.math.BigDecimal;

/**
 * Indicadores que se muestran en la pantalla de consulta.
 * Se calculan en la capa de negocio con consultas de agregacion.
 */
public class ResumenDTO {

    private long totalClientes;
    private long totalPedidos;
    private long pedidosEntregados;
    private long pedidosPendientes;
    private BigDecimal valorAcumulado;

    public ResumenDTO() {
    }

    public ResumenDTO(long totalClientes, long totalPedidos, long pedidosEntregados,
                      long pedidosPendientes, BigDecimal valorAcumulado) {
        this.totalClientes = totalClientes;
        this.totalPedidos = totalPedidos;
        this.pedidosEntregados = pedidosEntregados;
        this.pedidosPendientes = pedidosPendientes;
        this.valorAcumulado = valorAcumulado;
    }

    public long getTotalClientes() {
        return totalClientes;
    }

    public void setTotalClientes(long totalClientes) {
        this.totalClientes = totalClientes;
    }

    public long getTotalPedidos() {
        return totalPedidos;
    }

    public void setTotalPedidos(long totalPedidos) {
        this.totalPedidos = totalPedidos;
    }

    public long getPedidosEntregados() {
        return pedidosEntregados;
    }

    public void setPedidosEntregados(long pedidosEntregados) {
        this.pedidosEntregados = pedidosEntregados;
    }

    public long getPedidosPendientes() {
        return pedidosPendientes;
    }

    public void setPedidosPendientes(long pedidosPendientes) {
        this.pedidosPendientes = pedidosPendientes;
    }

    public BigDecimal getValorAcumulado() {
        return valorAcumulado;
    }

    public void setValorAcumulado(BigDecimal valorAcumulado) {
        this.valorAcumulado = valorAcumulado;
    }
}
