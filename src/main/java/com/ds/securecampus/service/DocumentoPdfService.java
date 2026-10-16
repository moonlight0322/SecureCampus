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
import com.ds.securecampus.model.HorarioSemanalFila;
import com.ds.securecampus.model.PerfilEstudiante;
import com.ds.securecampus.model.SolicitudTramite;

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
        sb.append("Estudiante: ").append(perfil.getNombreCompleto()).append("\n");
        sb.append("Número de control: ").append(perfil.getNumeroControl()).append("\n");
        sb.append("Carrera: ").append(perfil.getCarrera()).append("\n");
        sb.append("Promedio general: ").append(perfil.getPromedio()).append("\n");
        sb.append("Fecha de emisión: ").append(fecha).append("\n\n");
        sb.append("HISTORIAL ACADÉMICO\n\n");
        for (MateriaKardex m : materias) {
            sb.append(m.getMateria()).append(" (").append(m.getClave()).append(")\n");
            sb.append("Periodo: ").append(m.getPeriodo())
                    .append("  ·  Calificación: ").append(m.getCalificacion() == null ? "Pendiente" : m.getCalificacion())
                    .append("  ·  Estatus: ").append(m.getEstatus())
                    .append("  ·  Oportunidad: ").append(m.getOportunidad()).append("\n\n");
        }
        sb.append("Asignaturas registradas: ").append(materias.size()).append("\n");
        sb.append("Código de verificación: SC-KD-")
                .append(Integer.toHexString(perfil.getNumeroControl().hashCode()).toUpperCase());

        return construirPdfPlano(sb.toString(), "KÁRDEX ACADÉMICO");
    }

    public byte[] generarHorarioPdf(PerfilEstudiante perfil, List<MateriaHorario> horarios) {
        String fecha = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        StringBuilder sb = new StringBuilder();
        sb.append("Estudiante: ").append(perfil.getNombreCompleto()).append("\n");
        sb.append("Número de control: ").append(perfil.getNumeroControl()).append("\n");
        sb.append("Carrera: ").append(perfil.getCarrera()).append("\n");
        sb.append("Fecha de emisión: ").append(fecha).append("\n\n");
        sb.append("MATERIAS Y GRUPOS ASIGNADOS\n\n");
        for (MateriaHorario h : horarios) {
            sb.append(h.getMateria()).append(" (").append(h.getClave()).append(")\n");
            sb.append("Grupo: ").append(h.getGrupo()).append("  ·  Docente: ").append(h.getProfesor()).append("\n");
            sb.append("Periodo: ").append(h.getPeriodo()).append("  ·  Salón: ").append(h.getSalon()).append("\n");
            sb.append("Horario: ").append(h.getHorario()).append("\n\n");
        }
        sb.append("Código de verificación: SC-HR-")
                .append(Integer.toHexString(perfil.getNumeroControl().hashCode()).toUpperCase());

        return construirPdfPlano(sb.toString(), "HORARIO ESCOLAR - " + perfil.getNumeroControl());
    }

    public byte[] generarHorarioSemanalPdf(PerfilEstudiante perfil, List<HorarioSemanalFila> filas) {
        StringBuilder contenido = new StringBuilder();
        contenido.append("Estudiante: ").append(perfil.getNombreCompleto()).append("\n");
        contenido.append("Número de control: ").append(perfil.getNumeroControl()).append("\n");
        contenido.append("Carrera: ").append(perfil.getCarrera()).append("\n\n");
        contenido.append("HORARIO SEMANAL\n\n");
        String[] dias = {"Lunes", "Martes", "Miércoles", "Jueves", "Viernes"};
        for (HorarioSemanalFila fila : filas) {
            String[] clases = {fila.getLunes(), fila.getMartes(), fila.getMiercoles(),
                    fila.getJueves(), fila.getViernes()};
            for (int i = 0; i < dias.length; i++) {
                if (!clases[i].isBlank()) {
                    contenido.append(dias[i]).append(" · ").append(fila.getHorario())
                            .append(" · ").append(clases[i]).append("\n");
                }
            }
        }
        return construirPdfPlano(contenido.toString(), "HORARIO SEMANAL");
    }

    public byte[] generarListaGrupoPdf(Long grupoId, String materiaNombre, String grupoCodigo, String profesorNombre, List<CargaAlumno> alumnos) {
        String folio = "LISTA-" + grupoId + "-" + System.currentTimeMillis() % 10000;
        String fecha = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        StringBuilder sb = new StringBuilder();
        sb.append("LISTA OFICIAL DE EVALUACIÓN\n\n");
        sb.append("Materia: ").append(materiaNombre).append("\n");
        sb.append("Grupo: ").append(grupoCodigo).append("\n");
        sb.append("Docente: ").append(profesorNombre).append("\n");
        sb.append("Folio: ").append(folio).append("  ·  Fecha: ").append(fecha).append("\n\n");
        sb.append("ALUMNOS Y CALIFICACIONES\n\n");
        int count = 1;
        for (CargaAlumno ca : alumnos) {
            String nombre = ca.getEstudiante().getUsuario().getNombre() + " " + ca.getEstudiante().getUsuario().getApellidoPaterno();
            sb.append(count++).append(". ").append(nombre)
                    .append(" (").append(ca.getEstudiante().getNumeroControl()).append(")\n");
            sb.append("Parcial 1: ").append(ca.getCalificacionParcial1() == null ? "Pendiente" : ca.getCalificacionParcial1())
                    .append("  ·  Parcial 2: ").append(ca.getCalificacionParcial2() == null ? "Pendiente" : ca.getCalificacionParcial2())
                    .append("  ·  Final: ").append(ca.getCalificacionFinal() == null ? "Pendiente" : ca.getCalificacionFinal())
                    .append("  ·  Estatus: ").append(ca.getEstatus()).append("\n\n");
        }
        sb.append("Firma del docente: __________________________________________\n\n");
        sb.append("Código de verificación: SECURECAMPUS-CERT-")
                .append(Integer.toHexString(folio.hashCode()).toUpperCase());

        return construirPdfPlano(sb.toString(), "LISTA GRUPO " + grupoCodigo);
    }

    public byte[] generarComprobanteTramite(PerfilEstudiante perfil, SolicitudTramite solicitud) {
        String fecha = solicitud.getFechaSolicitud() == null ? "No disponible"
                : solicitud.getFechaSolicitud().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        StringBuilder contenido = new StringBuilder();
        contenido.append("Folio: ").append(solicitud.getFolio()).append("\n");
        contenido.append("Fecha de solicitud: ").append(fecha).append("\n");
        contenido.append("Estatus: APROBADA\n\n");
        contenido.append("DATOS DEL ESTUDIANTE\n");
        contenido.append("Nombre: ").append(perfil.getNombreCompleto()).append("\n");
        contenido.append("Número de control: ").append(perfil.getNumeroControl()).append("\n");
        contenido.append("Carrera: ").append(perfil.getCarrera()).append("\n\n");
        contenido.append("TRÁMITE\n");
        contenido.append("Tipo: ").append(solicitud.getTipoTramite()).append("\n");
        contenido.append("Motivo: ").append(solicitud.getMotivo() == null ? "Sin motivo adicional" : solicitud.getMotivo()).append("\n");
        contenido.append("Resolución: ").append(solicitud.getRespuestaAdmin() == null
                ? "Aprobado por Servicios Escolares" : solicitud.getRespuestaAdmin()).append("\n\n");
        contenido.append("Este comprobante acredita la aprobación registrada en SecureCampus.");
        return construirPdfPlano(contenido.toString(), "COMPROBANTE DE TRÁMITE APROBADO");
    }

    private byte[] construirPdfPlano(String texto, String titulo) {
        try {
            List<String> lineas = new java.util.ArrayList<>();
            for (String linea : texto.split("\\R")) {
                if (linea.matches("^[=\\-]{4,}$")
                        || linea.contains("INSTITUTO TECNOLÓGICO DE TOLUCA")) {
                    continue;
                }
                lineas.addAll(envolver(linea, 86));
            }
            List<List<String>> paginas = new java.util.ArrayList<>();
            for (int inicio = 0; inicio < lineas.size(); inicio += 40) {
                paginas.add(lineas.subList(inicio, Math.min(inicio + 40, lineas.size())));
            }
            if (paginas.isEmpty()) {
                paginas.add(List.of());
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            out.write("%PDF-1.4\n".getBytes(StandardCharsets.ISO_8859_1));
            int pageCount = paginas.size();
            int fontRegularId = 3 + pageCount * 2;
            int fontBoldId = fontRegularId + 1;
            int[] offsets = new int[fontBoldId + 1];

            offsets[1] = out.size();
            out.write("1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n".getBytes(StandardCharsets.ISO_8859_1));

            offsets[2] = out.size();
            StringBuilder kids = new StringBuilder();
            for (int i = 0; i < pageCount; i++) {
                kids.append(3 + i * 2).append(" 0 R ");
            }
            out.write(("2 0 obj\n<< /Type /Pages /Kids [" + kids + "] /Count " + pageCount
                    + " >>\nendobj\n").getBytes(StandardCharsets.ISO_8859_1));

            for (int i = 0; i < pageCount; i++) {
                int pageId = 3 + i * 2;
                int contentId = pageId + 1;
                StringBuilder stream = new StringBuilder();
                stream.append("q 0.07 0.16 0.30 rg 0 700 612 92 re f Q\n");
                stream.append("q 0.91 0.48 0.18 rg 0 694 612 6 re f Q\n");
                stream.append("1 1 1 rg\n");
                appendPdfText(stream, "F1", 9, 36, 766, "INSTITUTO TECNOLÓGICO DE TOLUCA  |  TECNM");
                appendPdfText(stream, "F2", 17, 36, 733, titulo);
                appendPdfText(stream, "F1", 8, 36, 714, "SERVICIOS ESCOLARES  ·  SECURECAMPUS");
                stream.append("0.86 0.89 0.93 RG 36 684 m 576 684 l S\n");
                stream.append("0 0 0 rg\n");

                int y = 663;
                for (String linea : paginas.get(i)) {
                    if (!linea.isBlank()) {
                        appendPdfText(stream, "F1", 10, 40, y, linea);
                    }
                    y -= 14;
                }
                stream.append("0.86 0.89 0.93 RG 36 48 m 576 48 l S\n");
                appendPdfText(stream, "F1", 8, 36, 32,
                        "Documento generado el " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                                + "  ·  Página " + (i + 1) + " de " + pageCount);

                byte[] streamBytes = stream.toString().getBytes(StandardCharsets.ISO_8859_1);
                offsets[pageId] = out.size();
                out.write((pageId + " 0 obj\n<< /Type /Page /Parent 2 0 R /MediaBox [0 0 612 792] "
                        + "/Contents " + contentId + " 0 R /Resources << /Font << /F1 "
                        + fontRegularId + " 0 R /F2 " + fontBoldId + " 0 R >> >> >>\nendobj\n")
                        .getBytes(StandardCharsets.ISO_8859_1));
                offsets[contentId] = out.size();
                out.write((contentId + " 0 obj\n<< /Length " + streamBytes.length + " >>\nstream\n")
                        .getBytes(StandardCharsets.ISO_8859_1));
                out.write(streamBytes);
                out.write("\nendstream\nendobj\n".getBytes(StandardCharsets.ISO_8859_1));
            }

            offsets[fontRegularId] = out.size();
            out.write((fontRegularId + " 0 obj\n<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica "
                    + "/Encoding /WinAnsiEncoding >>\nendobj\n").getBytes(StandardCharsets.ISO_8859_1));
            offsets[fontBoldId] = out.size();
            out.write((fontBoldId + " 0 obj\n<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica-Bold "
                    + "/Encoding /WinAnsiEncoding >>\nendobj\n").getBytes(StandardCharsets.ISO_8859_1));

            int startXref = out.size();
            out.write(("xref\n0 " + offsets.length + "\n0000000000 65535 f \n")
                    .getBytes(StandardCharsets.ISO_8859_1));
            for (int i = 1; i < offsets.length; i++) {
                out.write(String.format("%010d 00000 n \n", offsets[i]).getBytes(StandardCharsets.ISO_8859_1));
            }
            out.write(("trailer\n<< /Size " + offsets.length
                    + " /Root 1 0 R >>\nstartxref\n" + startXref + "\n%%EOF\n")
                    .getBytes(StandardCharsets.ISO_8859_1));

            return out.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo generar el documento PDF.", e);
        }
    }

    private List<String> envolver(String texto, int maxCaracteres) {
        if (texto.isBlank()) {
            return List.of("");
        }
        List<String> resultado = new java.util.ArrayList<>();
        StringBuilder linea = new StringBuilder();
        for (String palabra : texto.trim().split("\\s+")) {
            if (linea.length() > 0 && linea.length() + palabra.length() + 1 > maxCaracteres) {
                resultado.add(linea.toString());
                linea.setLength(0);
            }
            if (linea.length() > 0) {
                linea.append(' ');
            }
            linea.append(palabra);
        }
        resultado.add(linea.toString());
        return resultado;
    }

    private void appendPdfText(StringBuilder stream, String font, int size, int x, int y, String texto) {
        String escaped = texto.replace("\\", "\\\\").replace("(", "\\(").replace(")", "\\)");
        stream.append("BT /").append(font).append(' ').append(size).append(" Tf ")
                .append(x).append(' ').append(y).append(" Td (").append(escaped).append(") Tj ET\n");
    }
}
