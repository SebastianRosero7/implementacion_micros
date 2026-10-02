package com.revision.demo.application.dto;

import java.util.List;

public record PreguntaCompletaDTO(
    String preguntaId,
    String contexto,
    String preguntaDirecta,
    List<DistractorDTO> distractores,
    String respuestaCorrectaId,
    String justificacion,
    String competencia,
    String tema,
    String subtema,
    String nivelDificultad,
    String estado,
    String bibliografia
) {
    public PreguntaCompletaDTO {
        distractores = List.copyOf(distractores);
    }

    public record DistractorDTO(String id, String texto) {}
}
