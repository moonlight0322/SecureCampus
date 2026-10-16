package com.ds.securecampus.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;

import com.ds.securecampus.model.MateriaKardex;
import com.ds.securecampus.model.PerfilEstudiante;

class DocumentoPdfServiceTest {
    private final DocumentoPdfService documentoPdfService = new DocumentoPdfService();

    @Test
    void createsMultipagePdfWithSpanishAccentsForLongTranscript() {
        PerfilEstudiante perfil = new PerfilEstudiante("12345678", "María Hernández",
                "maria@example.edu", "Ingeniería en Sistemas", new BigDecimal("90.00"));
        List<MateriaKardex> materias = IntStream.range(0, 70)
                .mapToObj(i -> new MateriaKardex("Programación " + i, "ISC-" + i,
                        "Ago-Dic 2026", new BigDecimal("90.00"), "APROBADA", "PRIMERA"))
                .toList();

        byte[] pdf = documentoPdfService.generarKardexPdf(perfil, materias);
        String pdfText = new String(pdf, StandardCharsets.ISO_8859_1);

        assertThat(pdfText).startsWith("%PDF-1.4");
        assertThat(pdfText).contains("/Count 6", "0 0 0 rg", "María Hernández",
                "KÁRDEX ACADÉMICO", "startxref");
    }
}
