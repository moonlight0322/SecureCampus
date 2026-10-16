package com.ds.securecampus.service;

import java.nio.charset.StandardCharsets;

public final class PoliticaContrasena {
    private PoliticaContrasena() {
    }

    public static boolean esValida(String password) {
        if (password == null || password.length() < 8
                || password.getBytes(StandardCharsets.UTF_8).length > 72) {
            return false;
        }
        boolean mayuscula = password.codePoints().anyMatch(Character::isUpperCase);
        boolean numero = password.codePoints().anyMatch(Character::isDigit);
        boolean especial = password.codePoints()
                .anyMatch(caracter -> !Character.isLetterOrDigit(caracter));
        return mayuscula && numero && especial;
    }

    public static String requisitos() {
        return "La contraseña debe tener al menos 8 caracteres, una mayúscula, un número y un carácter especial.";
    }
}
