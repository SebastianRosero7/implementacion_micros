package com.revision.demo.application.dto;

import java.time.LocalDateTime;

public record EvaluarResponseDTO(
    Long idEvaluacion,
    Double calificacionFinal,
    String resultadoPreliminar,
    LocalDateTime fechaEvaluacion
) {}