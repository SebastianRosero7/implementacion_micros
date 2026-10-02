package com.revision.demo.application.usecase;

import com.revision.demo.application.dto.RevisionResponseDTO;
import com.revision.demo.domain.model.Revision;

final class RevisionResponseMapper {
    private RevisionResponseMapper() {}

    static RevisionResponseDTO toResponse(Revision revision) {
        if (revision == null) {
            throw new IllegalStateException("El repositorio debe devolver la revisión guardada");
        }
        return new RevisionResponseDTO(
            revision.getId(),
            revision.getIdPregunta(),
            revision.getIdRevisor(),
            revision.getEstado(),
            revision.getObservaciones().stream().map(observacion ->
                observacion.getCodigo() + ": " + observacion.getDescripcion()
            ).toList()
        );
    }
}
