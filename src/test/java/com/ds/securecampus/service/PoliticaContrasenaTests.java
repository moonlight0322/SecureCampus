package com.ds.securecampus.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class PoliticaContrasenaTests {
    @Test
    void requiresAtLeastEightCharactersUppercaseDigitAndSpecialCharacter() {
        assertThat(PoliticaContrasena.esValida("Abcdef1!")).isTrue();
        assertThat(PoliticaContrasena.esValida("Ab1!xyz")).isFalse();
        assertThat(PoliticaContrasena.esValida("abcdef1!")).isFalse();
        assertThat(PoliticaContrasena.esValida("Abcdefgh!")).isFalse();
        assertThat(PoliticaContrasena.esValida("Abcdefg1")).isFalse();
        assertThat(PoliticaContrasena.esValida("Ábcdefg1!")).isTrue();
        assertThat(PoliticaContrasena.esValida("A" + "b".repeat(70) + "1!")).isFalse();
    }
}
