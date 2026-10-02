package com.revision.demo.domain.model;

import java.util.Objects;

public class Observacion {
    private final String codigo;
    private final String descripcion;
    private final String categoria; // Ejemplo: "De forma", "De fondo", "Metodología"

    public Observacion(String codigo, String descripcion, String categoria) {
        this.codigo = validarTexto(codigo, "El código es obligatorio");
        this.descripcion = validarTexto(descripcion, "La descripción es obligatoria");
        if (categoria != null && categoria.isBlank()) {
            throw new IllegalArgumentException("La categoría no puede estar vacía");
        }
        this.categoria = categoria;
    }

    private static String validarTexto(String valor, String mensaje) {
        Objects.requireNonNull(valor, mensaje);
        if (valor.isBlank()) {
            throw new IllegalArgumentException(mensaje);
        }
        return valor;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getCategoria() {
        return categoria;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Observacion that = (Observacion) o;
        return Objects.equals(codigo, that.codigo) && Objects.equals(descripcion, that.descripcion);
    }

    @Override
    public int hashCode() {
        return Objects.hash(codigo, descripcion);
    }
}