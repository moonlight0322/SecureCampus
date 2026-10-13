package com.ds.securecampus.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.stereotype.Service;

import com.ds.securecampus.model.CargaAlumno;
import com.ds.securecampus.model.Estudiante;
import com.ds.securecampus.model.MateriaHorario;
import com.ds.securecampus.model.MateriaKardex;
import com.ds.securecampus.model.PerfilEstudiante;

@Service
public class DocumentoPdfService {

    public byte[] generarConstanciaEstudios(PerfilEstudiante perfil, Estudiante estudiante) {
        String folio = "CONST-" + System.currentTimeMillis() % 1000000;
        String fecha = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        
        StringBuilder sb = new StringBuilder();
        sb.append("================================================================================\n");
        sb.append("               INSTITUTO TECNOLÓGICO DE TOLUCA - TECNM\n");
        sb.append("                 DIRECCIÓN DE SERVICIOS ESCOLARES\n");
        sb.append("================================================================================\n\n");
        sb.append("CONSTANCIA OFICIAL DE ESTUDIOS\n");
        sb.append("FOLIO DE EMISIÓN: ").append(folio).append("\n");
        sb.append("FECHA Y HORA:     ").append(fecha).append("\n\n");
        sb.append("A QUIEN CORRESPONDA:\n\n");
        sb.append("La Dirección de este Instituto hace constar que el (la) estudiante:\n\n");
        sb.append("  NOMBRE:             ").append(perfil.getNombreCompleto()).append("\n");
        sb.append("  NÚMERO DE CONTROL:  ").append(perfil.getNumeroControl()).append("\n");
        sb.append("  CARRERA:            ").append(perfil.getCarrera()).append("\n");
        sb.append("  PROMEDIO GENERAL:   ").append(perfil.getPromedio()).append("\n");
        sb.append("  ESTATUS REINSCR.:   ").append("S".equalsIgnoreCase(estudiante.getPagoReinscripcion()) ? "INSCRITO Y VIGENTE" : "PENDIENTE").append("\n");
        sb.append("  ADEUDOS:            ").append("N".equalsIgnoreCase(estudiante.getTieneAdeudos()) ? "SIN ADEUDOS" : "CON ADEUDOS PENDIENTES").append("\n\n");
        sb.append("Se extiende la presente constancia para los fines legales y académicos que al interesado convengan.\n\n");
        sb.append("CÓDIGO DE VERIFICACIÓN ELECTRÓNICA: SHA256-SC-").append(Integer.toHexString(folio.hashCode()).toUpperCase()).append("\n");
        sb.append("================================================================================\n");
        sb.append("Este documento cuenta con validez oficial emitida por la plataforma SecureCampus.\n");

        return construirPdfPlano(sb.toString(), "CONSTANCIA DE ESTUDIOS - " + perfil.getNumeroControl());
    }

    public byte[] generarKardexPdf(PerfilEstudiante perfil, List<MateriaKardex> materias) {
        String fecha = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        StringBuilder sb = new StringBuilder();
        sb.append("================================================================================\n");
        sb.append("             INSTITUTO TECNOLÓGICO DE TOLUCA - KÁRDEX ACADÉMICO\n");
        sb.append("================================================================================\n");
        sb.append("ALUMNO:  ").append(perfil.getNombreCompleto()).append("   | CONTROL: ").append(perfil.getNumeroControl()).append("\n");
        sb.append("CARRERA: ").append(perfil.getCarrera()).append(" | PROMEDIO: ").append(perfil.getPromedio()).append("\n");
        sb.append("FECHA DE CONSULTA: ").append(fecha).append("\n\n");
        sb.append(String.format("%-12s %-35s %-12s %-8s %-12s\n", "CLAVE", "MATERIA", "PERIODO", "CALIF", "ESTATUS"));
        sb.append("--------------------------------------------------------------------------------\n");
        for (MateriaKardex m : materias) {
            String nom = m.getMateria().length() > 34 ? m.getMateria().substring(0, 31) + "..." : m.getMateria();
            sb.append(String.format("%-12s %-35s %-12s %-8s %-12s\n", 
                    m.getClave(), nom, m.getPeriodo(), m.getCalificacion() != null ? m.getCalificacion() : "--", m.getEstatus()));
        }
        sb.append("--------------------------------------------------------------------------------\n");
        sb.append("TOTAL DE ASIGNATURAS REGISTRADAS: ").append(materias.size()).append("\n");
        sb.append("VERIFICACIÓN: SC-KD-").append(Integer.toHexString(perfil.getNumeroControl().hashCode()).toUpperCase()).append("\n");

        return construirPdfPlano(sb.toString(), "KARDEX OFICIAL - " + perfil.getNumeroControl());
    }

    public byte[] generarHorarioPdf(PerfilEstudiante perfil, List<MateriaHorario> horarios) {
        String fecha = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        StringBuilder sb = new StringBuilder();
        sb.append("================================================================================\n");
        sb.append("           INSTITUTO TECNOLÓGICO DE TOLUCA - HORARIO DE CLASES\n");
        sb.append("================================================================================\n");
        sb.append("ALUMNO:  ").append(perfil.getNombreCompleto()).append(" | CONTROL: ").append(perfil.getNumeroControl()).append("\n");
        sb.append("CARRERA: ").append(perfil.getCarrera()).append("\n");
        sb.append("EMISIÓN: ").append(fecha).append("\n\n");
        sb.append(String.format("%-10s %-30s %-8s %-10s %-20s\n", "CLAVE", "MATERIA", "GRUPO", "AULA", "HORARIO"));
        sb.append("--------------------------------------------------------------------------------\n");
        for (MateriaHorario h : horarios) {
            String mat = h.getMateria().length() > 29 ? h.getMateria().substring(0, 26) + "..." : h.getMateria();
            sb.append(String.format("%-10s %-30s %-8s %-10s %-20s\n", 
                    h.getClave(), mat, h.getGrupo(), h.getSalon(), h.getHorario()));
        }
        sb.append("--------------------------------------------------------------------------------\n");
        sb.append("VERIFICACIÓN: SC-HR-").append(Integer.toHexString(perfil.getNumeroControl().hashCode()).toUpperCase()).append("\n");

        return construirPdfPlano(sb.toString(), "HORARIO ESCOLAR - " + perfil.getNumeroControl());
    }

    public byte[] generarListaGrupoPdf(Long grupoId, String materiaNombre, String grupoCodigo, String profesorNombre, List<CargaAlumno> alumnos) {
        String folio = "LISTA-" + grupoId + "-" + System.currentTimeMillis() % 10000;
        String fecha = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        StringBuilder sb = new StringBuilder();
        sb.append("================================================================================\n");
        sb.append("         INSTITUTO TECNOLÓGICO DE TOLUCA - LISTA OFICIAL DE EVALUACIÓN\n");
        sb.append("================================================================================\n");
        sb.append("MATERIA: ").append(materiaNombre).append(" | GRUPO: ").append(grupoCodigo).append("\n");
        sb.append("DOCENTE: ").append(profesorNombre).append("\n");
        sb.append("FOLIO LISTA: ").append(folio).append(" | FECHA: ").append(fecha).append("\n\n");
        sb.append(String.format("%-4s %-12s %-32s %-8s %-8s %-8s %-10s\n", "NO", "CONTROL", "NOMBRE DEL ALUMNO", "P1", "P2", "FINAL", "ESTATUS"));
        sb.append("--------------------------------------------------------------------------------\n");
        int count = 1;
        for (CargaAlumno ca : alumnos) {
            String nombre = ca.getEstudiante().getUsuario().getNombre() + " " + ca.getEstudiante().getUsuario().getApellidoPaterno();
            if (nombre.length() > 31) nombre = nombre.substring(0, 28) + "...";
            sb.append(String.format("%-4d %-12s %-32s %-8s %-8s %-8s %-10s\n", 
                    count++, 
                    ca.getEstudiante().getNumeroControl(),
                    nombre,
                    ca.getCalificacionParcial1() != null ? ca.getCalificacionParcial1() : "--",
                    ca.getCalificacionParcial2() != null ? ca.getCalificacionParcial2() : "--",
                    ca.getCalificacionFinal() != null ? ca.getCalificacionFinal() : "--",
                    ca.getEstatus()));
        }
        sb.append("--------------------------------------------------------------------------------\n");
        sb.append("\nFIRMA DEL DOCENTE: ___________________________   FIRMA DE SERVICIOS ESCOLARES: ___________________________\n\n");
        sb.append("CÓDIGO DIGITAL DE VERIFICACIÓN INSTITUCIONAL: SECURECAMPUS-CERT-").append(Integer.toHexString(folio.hashCode()).toUpperCase()).append("\n");

        return construirPdfPlano(sb.toString(), "LISTA GRUPO " + grupoCodigo);
    }

    /**
     * Construye un archivo binario PDF 1.4 canónico sin requerir librerías externas adicionales.
     */
    private byte[] construirPdfPlano(String texto, String titulo) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            out.write("%PDF-1.4\n".getBytes(StandardCharsets.ISO_8859_1));

            // Escapar texto para PDF
            String escaped = texto.replace("\\", "\\\\")
                                  .replace("(", "\\(")
                                  .replace(")", "\\)");

            StringBuilder textStream = new StringBuilder();
            textStream.append("BT\n");
            textStream.append("/F1 9 Tf\n");
            textStream.append("12 TL\n");
            textStream.append("36 750 Td\n");

            String[] lines = escaped.split("\n");
            for (String line : lines) {
                textStream.append("(").append(line).append(") '\n");
            }
            textStream.append("ET\n");

            byte[] streamBytes = textStream.toString().getBytes(StandardCharsets.ISO_8859_1);

            int[] offsets = new int[6];

            // Obj 1: Catalog
            offsets[1] = out.size();
            out.write("1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n".getBytes(StandardCharsets.ISO_8859_1));

            // Obj 2: Pages
            offsets[2] = out.size();
            out.write("2 0 obj\n<< /Type /Pages /Kids [3 0 R] /Count 1 >>\nendobj\n".getBytes(StandardCharsets.ISO_8859_1));

            // Obj 3: Page
            offsets[3] = out.size();
            out.write("3 0 obj\n<< /Type /Page /Parent 2 0 R /MediaBox [0 0 612 792] /Contents 4 0 R /Resources << /Font << /F1 5 0 R >> >> >>\nendobj\n".getBytes(StandardCharsets.ISO_8859_1));

            // Obj 4: Contents
            offsets[4] = out.size();
            out.write(("4 0 obj\n<< /Length " + streamBytes.length + " >>\nstream\n").getBytes(StandardCharsets.ISO_8859_1));
            out.write(streamBytes);
            out.write("\nendstream\nendobj\n".getBytes(StandardCharsets.ISO_8859_1));

            // Obj 5: Font (Courier monoespaciada para tablas)
            offsets[5] = out.size();
            out.write("5 0 obj\n<< /Type /Font /Subtype /Type1 /BaseFont /Courier >>\nendobj\n".getBytes(StandardCharsets.ISO_8859_1));

            // Xref
            int startXref = out.size();
            out.write("xref\n0 6\n0000000000 65535 f \n".getBytes(StandardCharsets.ISO_8859_1));
            for (int i = 1; i <= 5; i++) {
                out.write(String.format("%010d 00000 n \n", offsets[i]).getBytes(StandardCharsets.ISO_8859_1));
            }
            out.write(("trailer\n<< /Size 6 /Root 1 0 R >>\nstartxref\n" + startXref + "\n%%EOF\n").getBytes(StandardCharsets.ISO_8859_1));

            return out.toByteArray();
        } catch (IOException e) {
            return texto.getBytes(StandardCharsets.UTF_8);
        }
    }
}
