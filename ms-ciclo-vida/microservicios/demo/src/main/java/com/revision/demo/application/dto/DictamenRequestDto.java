package com.revision.demo.application.dto;

import com.revision.demo.domain.model.TipoDictamen;
import java.util.Objects;

public record DictamenRequestDto(
    String evaluacionId,
    TipoDictamen resultado,
    String justificacion
) {
    public DictamenRequestDto {
        validarTexto(evaluacionId, "El ID de la evaluación es obligatorio");
        Objects.requireNonNull(resultado, "El resultado del dictamen es obligatorio");
        validarTexto(justificacion, "La justificación del dictamen es obligatoria");
    }

    private static void validarTexto(String valor, String mensaje) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(mensaje);
        }
    }
}
