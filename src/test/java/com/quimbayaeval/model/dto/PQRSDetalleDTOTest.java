package com.quimbayaeval.model.dto;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class PQRSDetalleDTOTest {

    @Test
    void calcularSLA_pendienteMenorA15Dias_retornaATiempo() {
        PQRSDetalleDTO dto = new PQRSDetalleDTO();
        dto.setEstado("Pendiente");
        dto.setFechaCreacion(LocalDateTime.now().minusDays(5));

        dto.calcularSLA();

        assertEquals(5L, dto.getDiasTranscurridos());
        assertEquals("A_TIEMPO", dto.getEstadoSLA());
        assertTrue(dto.getEnPlazo());
    }

    @Test
    void calcularSLA_pendienteMayorA15Dias_retornaVencido() {
        PQRSDetalleDTO dto = new PQRSDetalleDTO();
        dto.setEstado("En trámite");
        dto.setFechaCreacion(LocalDateTime.now().minusDays(20));

        dto.calcularSLA();

        assertEquals(20L, dto.getDiasTranscurridos());
        assertEquals("VENCIDO", dto.getEstadoSLA());
        assertFalse(dto.getEnPlazo());
    }

    @Test
    void calcularSLA_resueltaATiempo_retornaAtendidoATiempo() {
        PQRSDetalleDTO dto = new PQRSDetalleDTO();
        dto.setEstado("Resuelta");
        dto.setFechaCreacion(LocalDateTime.now().minusDays(10));
        dto.setFechaRespuesta(LocalDateTime.now().minusDays(2));

        dto.calcularSLA();

        assertEquals(8L, dto.getDiasTranscurridos());
        assertEquals("ATENDIDO_A_TIEMPO", dto.getEstadoSLA());
        assertTrue(dto.getEnPlazo());
    }
}
