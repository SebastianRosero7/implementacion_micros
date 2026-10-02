package com.revision.demo.application.dto;

public record EvaluarPreguntaResponseDTO(
    RevisionResponseDTO revision,
    PreguntaCompletaDTO pregunta
) {}
