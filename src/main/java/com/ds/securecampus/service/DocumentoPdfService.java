package com.ds.securecampus.service;

import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.ds.securecampus.model.CargaAlumno;
import com.ds.securecampus.model.Estudiante;
import com.ds.securecampus.model.MateriaHorario;
import com.ds.securecampus.model.MateriaKardex;
import com.ds.securecampus.model.PerfilEstudiante;

@Service
public class DocumentoPdfService {
    private static final Charset PDF_TEXT_CHARSET = Charset.forName("windows-1252");
    private static final int PDF_LINES_PER_PAGE = 55;

    public byte[] generarConstanciaEstudios(PerfilEstudiante perfil, Estudiante estudiante) {
        String folio = "CONST-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
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
        sb.append("FOLIO DE CONSULTA: SC-KD-").append(UUID.randomUUID().toString().substring(0, 8).toUpperCase()).append("\n");

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
        sb.append("FOLIO DE CONSULTA: SC-HR-").append(UUID.randomUUID().toString().substring(0, 8).toUpperCase()).append("\n");

        return construirPdfPlano(sb.toString(), "HORARIO ESCOLAR - " + perfil.getNumeroControl());
    }

    public byte[] generarListaGrupoPdf(Long grupoId, String materiaNombre, String grupoCodigo, String profesorNombre, List<CargaAlumno> alumnos) {
        String folio = "LISTA-" + grupoId + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
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
        return construirPdfPlano(sb.toString(), "LISTA GRUPO " + grupoCodigo);
    }

    private byte[] construirPdfPlano(String texto, String titulo) {
        String[] lines = texto.split("\n", -1);
        int pageCount = Math.max(1, (lines.length + PDF_LINES_PER_PAGE - 1) / PDF_LINES_PER_PAGE);
        int fontId = 3 + pageCount * 2;
        int infoId = fontId + 1;
        int objectCount = infoId;
        List<Integer> offsets = new ArrayList<>(objectCount + 1);
        for (int i = 0; i <= objectCount; i++) {
            offsets.add(0);
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.writeBytes("%PDF-1.4\n".getBytes(StandardCharsets.US_ASCII));
        writeObject(out, offsets, 1, ascii("<< /Type /Catalog /Pages 2 0 R /Info " + infoId + " 0 R >>"));

        StringBuilder kids = new StringBuilder("<< /Type /Pages /Kids [");
        for (int page = 0; page < pageCount; page++) {
            kids.append(3 + page * 2).append(" 0 R ");
        }
        kids.append("] /Count ").append(pageCount).append(" >>");
        writeObject(out, offsets, 2, ascii(kids.toString()));

        for (int page = 0; page < pageCount; page++) {
            int pageId = 3 + page * 2;
            int contentId = pageId + 1;
            writeObject(out, offsets, pageId, ascii("<< /Type /Page /Parent 2 0 R "
                    + "/MediaBox [0 0 612 792] /Contents " + contentId
                    + " 0 R /Resources << /Font << /F1 " + fontId + " 0 R >> >> >>"));

            StringBuilder textStream = new StringBuilder("BT\n/F1 9 Tf\n12 TL\n36 756 Td\n");
            int firstLine = page * PDF_LINES_PER_PAGE;
            int lastLine = Math.min(firstLine + PDF_LINES_PER_PAGE, lines.length);
            for (int line = firstLine; line < lastLine; line++) {
                textStream.append('(').append(escapePdfText(lines[line])).append(") Tj T*\n");
            }
            textStream.append("ET\n");
            byte[] streamBytes = textStream.toString().getBytes(PDF_TEXT_CHARSET);
            writeStreamObject(out, offsets, contentId, streamBytes);
        }

        writeObject(out, offsets, fontId, ascii("<< /Type /Font /Subtype /Type1 "
                + "/BaseFont /Courier /Encoding /WinAnsiEncoding >>"));
        ByteArrayOutputStream info = new ByteArrayOutputStream();
        info.writeBytes("<< /Title (".getBytes(StandardCharsets.US_ASCII));
        info.writeBytes(escapePdfText(titulo).getBytes(PDF_TEXT_CHARSET));
        info.writeBytes(") /Creator (SecureCampus) >>".getBytes(StandardCharsets.US_ASCII));
        writeObject(out, offsets, infoId, info.toByteArray());

        int startXref = out.size();
        out.writeBytes(ascii("xref\n0 " + (objectCount + 1) + "\n0000000000 65535 f \n"));
        for (int i = 1; i <= objectCount; i++) {
            out.writeBytes(ascii(String.format("%010d 00000 n \n", offsets.get(i))));
        }
        out.writeBytes(ascii("trailer\n<< /Size " + (objectCount + 1)
                + " /Root 1 0 R /Info " + infoId + " 0 R >>\nstartxref\n"
                + startXref + "\n%%EOF\n"));
        return out.toByteArray();
    }

    private static void writeObject(ByteArrayOutputStream out, List<Integer> offsets,
                                    int objectId, byte[] content) {
        offsets.set(objectId, out.size());
        out.writeBytes(ascii(objectId + " 0 obj\n"));
        out.writeBytes(content);
        out.writeBytes(ascii("\nendobj\n"));
    }

    private static void writeStreamObject(ByteArrayOutputStream out, List<Integer> offsets,
                                          int objectId, byte[] stream) {
        offsets.set(objectId, out.size());
        out.writeBytes(ascii(objectId + " 0 obj\n<< /Length " + stream.length + " >>\nstream\n"));
        out.writeBytes(stream);
        out.writeBytes(ascii("\nendstream\nendobj\n"));
    }

    private static byte[] ascii(String value) {
        return value.getBytes(StandardCharsets.US_ASCII);
    }

    private static String escapePdfText(String value) {
        return value.replace("\\", "\\\\").replace("(", "\\(").replace(")", "\\)");
    }
}
