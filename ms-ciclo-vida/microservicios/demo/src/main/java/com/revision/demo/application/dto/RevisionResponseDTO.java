package com.revision.demo.application.dto;

import com.revision.demo.domain.model.Estado;
import java.util.List;

public record RevisionResponseDTO(
    String idRevision,
    Long idPregunta,
    Long idRevisor,
    Estado estado,
    List<String> observaciones
) {
    public RevisionResponseDTO {
        observaciones = List.copyOf(observaciones);
    }
}
