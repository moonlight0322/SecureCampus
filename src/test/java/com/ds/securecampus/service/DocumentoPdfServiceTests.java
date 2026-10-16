package com.ds.securecampus.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.ds.securecampus.model.MateriaKardex;
import com.ds.securecampus.model.PerfilEstudiante;

class DocumentoPdfServiceTests {
    private final DocumentoPdfService documentoPdfService = new DocumentoPdfService();

    @Test
    void transcriptPdfContainsBrandedHeaderAndStudentDetails() {
        PerfilEstudiante perfil = new PerfilEstudiante(
                "22280691", "Montserrat Hernández Fabián", "alumna@example.edu",
                "Ingeniería en Sistemas", new BigDecimal("86.00"));

        byte[] pdf = documentoPdfService.generarKardexPdf(perfil, List.of(
                new MateriaKardex("Programación", "ISC-101", "Ago-Dic 2026",
                        new BigDecimal("95.00"), "APROBADA", "PRIMERA")));
        String pdfText = new String(pdf, StandardCharsets.ISO_8859_1);

        assertThat(pdfText).startsWith("%PDF-1.4");
        assertThat(pdfText).contains("INSTITUTO TECNOLÓGICO DE TOLUCA");
        assertThat(pdfText).contains("1 1 1 rg\nBT /F1 9 Tf 36 766 Td");
        assertThat(pdfText).contains(
                "0.86 0.89 0.93 RG 36 684 m 576 684 l S\n0 0 0 rg\nBT /F1 10 Tf 40 663 Td");
        assertThat(pdfText).contains("Montserrat Hernández Fabián");
        assertThat(pdfText).contains("KÁRDEX ACADÉMICO");
        assertThat(pdfText).contains("/BaseFont /Helvetica");
        assertThat(pdfText).contains("startxref");
    }
}
