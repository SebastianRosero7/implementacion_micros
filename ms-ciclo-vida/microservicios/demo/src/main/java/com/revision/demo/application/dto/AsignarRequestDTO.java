package com.revision.demo.application.dto;

import java.time.LocalDate;
import java.util.Objects;

public record AsignarRequestDTO(
    Long idSolicitud,
    Long idEvaluador,
    LocalDate fechaAsignacion
) {
    public AsignarRequestDTO {
        if (idSolicitud == null || idSolicitud <= 0) {
            throw new IllegalArgumentException("El ID de la solicitud debe ser positivo");
        }
        if (idEvaluador == null || idEvaluador <= 0) {
            throw new IllegalArgumentException("El ID del evaluador debe ser positivo");
        }
        Objects.requireNonNull(fechaAsignacion, "La fecha de asignación es obligatoria");
    }
}