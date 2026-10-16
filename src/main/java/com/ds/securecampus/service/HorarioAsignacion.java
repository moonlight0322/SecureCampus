package com.ds.securecampus.service;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

record SesionClase(Byte dia, String horaInicio, String horaFin) {
}

final class HorarioAsignacion {
    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("HH:mm");
    private static final List<String> INICIOS = List.of("07:00", "09:00", "11:00", "13:00", "15:00", "17:00");

    private HorarioAsignacion() {
    }

    static List<SesionClase> crear(String patron, String inicio, String horaCorta) {
        if (!INICIOS.contains(inicio)) {
            throw new IllegalArgumentException("El inicio debe corresponder a un bloque: 07:00, 09:00, 11:00, 13:00, 15:00 o 17:00.");
        }
        LocalTime base = LocalTime.parse(inicio, FORMATO);
        List<Byte> dias = switch (patron == null ? "" : patron) {
            case "MW" -> List.of((byte) 1, (byte) 3);
            case "TR" -> List.of((byte) 2, (byte) 4);
            case "WF" -> List.of((byte) 3, (byte) 5);
            case "MTTR" -> List.of((byte) 1, (byte) 2, (byte) 4);
            case "TTRF" -> List.of((byte) 2, (byte) 4, (byte) 5);
            case "MWF" -> List.of((byte) 1, (byte) 3, (byte) 5);
            default -> throw new IllegalArgumentException("Selecciona una distribución semanal válida de 4 o 5 horas.");
        };
        boolean cincoHoras = dias.size() == 3;
        Byte diaCorto = null;
        String posicionCorta = null;
        if (cincoHoras) {
            String diaCortoNombre;
            if ("MWF".equals(patron)) {
                if (horaCorta == null || !List.of("LUNES_INICIO", "LUNES_FIN",
                        "VIERNES_INICIO", "VIERNES_FIN").contains(horaCorta)) {
                    throw new IllegalArgumentException("Selecciona lunes o viernes y el inicio o final del bloque para la sesión corta.");
                }
                String[] partes = horaCorta.split("_");
                diaCortoNombre = partes[0];
                posicionCorta = partes[1];
            } else {
                if (horaCorta == null || !List.of("INICIO", "FIN").contains(horaCorta)) {
                    throw new IllegalArgumentException("Selecciona si la sesión corta va al inicio o al final del bloque.");
                }
                diaCortoNombre = "MTTR".equals(patron) ? "LUNES" : "VIERNES";
                posicionCorta = horaCorta;
            }
            diaCorto = "LUNES".equals(diaCortoNombre) ? (byte) 1 : (byte) 5;
        } else if (horaCorta != null && !horaCorta.isBlank()) {
            throw new IllegalArgumentException("Las clases de cuatro horas no requieren una sesión corta.");
        }

        List<SesionClase> sesiones = new ArrayList<>();
        for (Byte dia : dias) {
            boolean corta = dia.equals(diaCorto);
            LocalTime horaInicio = corta && "FIN".equals(posicionCorta) ? base.plusHours(1) : base;
            LocalTime horaFin = horaInicio.plusHours(corta ? 1 : 2);
            if (horaFin.isAfter(LocalTime.of(19, 0))) {
                throw new IllegalArgumentException("La clase debe terminar a más tardar a las 19:00.");
            }
            sesiones.add(new SesionClase(dia, horaInicio.format(FORMATO), horaFin.format(FORMATO)));
        }
        return List.copyOf(sesiones);
    }
}
