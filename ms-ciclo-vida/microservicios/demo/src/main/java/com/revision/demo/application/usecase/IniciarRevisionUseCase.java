package com.revision.demo.application.usecase;

import com.revision.demo.application.dto.RevisionResponseDTO;
import com.revision.demo.domain.model.Revision;
import com.revision.demo.domain.repository.RevisionRepository;
import com.revision.demo.domain.service.GestorTransicionesEstado;
import java.util.NoSuchElementException;
import java.util.Objects;

public class IniciarRevisionUseCase {
    private final RevisionRepository revisionRepository;
    private final GestorTransicionesEstado gestorTransicionesEstado;

    public IniciarRevisionUseCase(
        RevisionRepository revisionRepository,
        GestorTransicionesEstado gestorTransicionesEstado
    ) {
        this.revisionRepository = Objects.requireNonNull(revisionRepository);
        this.gestorTransicionesEstado = Objects.requireNonNull(gestorTransicionesEstado);
    }

    public RevisionResponseDTO ejecutar(String idRevision) {
        if (idRevision == null || idRevision.isBlank()) {
            throw new IllegalArgumentException("El ID de la revisión es obligatorio");
        }
        Revision revision = revisionRepository.buscarPorId(idRevision)
            .orElseThrow(() -> new NoSuchElementException("No existe la revisión con ID " + idRevision));
        revision.iniciar(gestorTransicionesEstado);
        return RevisionResponseMapper.toResponse(revisionRepository.guardar(revision));
    }
}
