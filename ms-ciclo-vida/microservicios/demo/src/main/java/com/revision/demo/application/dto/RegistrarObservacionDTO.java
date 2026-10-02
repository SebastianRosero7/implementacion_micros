package com.revision.demo.application.dto;

public record RegistrarObservacionDTO(
    String idRevision,
    String codigo,
    String descripcion,
    String categoria
) {
    public RegistrarObservacionDTO {
        validarTexto(idRevision, "El ID de la revisión es obligatorio");
        validarTexto(codigo, "El código de la observación es obligatorio");
        validarTexto(descripcion, "La descripción de la observación es obligatoria");
        if (categoria != null && categoria.isBlank()) {
            throw new IllegalArgumentException("La categoría no puede estar vacía");
        }
    }

    private static void validarTexto(String valor, String mensaje) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(mensaje);
        }
    }
}
