
/** Ejecuta una peticion contra la API y maneja los errores del backend. */
async function llamarApi(ruta, opciones) {
    const configuracion = Object.assign({
        headers: { 'Content-Type': 'application/json' }
    }, opciones || {});

    const respuesta = await fetch(ruta, configuracion);

    // 204 No Content: no hay cuerpo que leer
    if (respuesta.status === 204) {
        return null;
    }

    const texto = await respuesta.text();
    const datos = texto ? JSON.parse(texto) : null;

    if (!respuesta.ok) {
        let detalle = (datos && datos.mensaje) || ('Error HTTP ' + respuesta.status);
        if (datos && datos.errores) {
            detalle += ': ' + Object.values(datos.errores).join(', ');
        }
        throw new Error(detalle);
    }
    return datos;
}

function mostrarMensaje(id, texto, tipo) {
    const caja = document.getElementById(id);
    if (!caja) return;
    caja.textContent = texto;
    caja.className = 'mensaje ' + tipo;
    if (tipo === 'ok') {
        setTimeout(() => { caja.className = 'mensaje'; }, 6000);
    }
}

function aMoneda(valor) {
    if (valor === null || valor === undefined) return '-';
    return new Intl.NumberFormat('es-CO', {
        style: 'currency', currency: 'COP', maximumFractionDigits: 2
    }).format(valor);
}

function aFechaHora(valor) {
    if (!valor) return '-';
    return String(valor).replace('T', ' ').substring(0, 16);
}

function sello(valor, textoSi, textoNo) {
    return valor
        ? '<span class="sello-estado est-si">' + textoSi + '</span>'
        : '<span class="sello-estado est-no">' + textoNo + '</span>';
}

function limpiar(texto) {
    if (texto === null || texto === undefined) return '';
    return String(texto)
        .replace(/&/g, '&amp;').replace(/</g, '&lt;')
        .replace(/>/g, '&gt;').replace(/"/g, '&quot;');
}
