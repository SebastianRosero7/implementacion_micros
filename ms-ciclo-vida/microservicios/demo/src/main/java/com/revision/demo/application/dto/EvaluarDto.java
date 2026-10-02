package com.revision.demo.application.dto;

public record EvaluarDto(
    Long idEvaluacion,
    String decision,
    String observacionesFinales
) {
    public EvaluarDto {
        if (idEvaluacion == null || idEvaluacion <= 0) {
            throw new IllegalArgumentException("El ID de la evaluación debe ser positivo");
        }
        if (decision == null || decision.isBlank()) {
            throw new IllegalArgumentException("La decisión del dictamen es obligatoria");
        }
    }
}