package com.revision.demo.domain.repository;

import com.revision.demo.domain.model.Revision;
import java.util.Optional;

public interface RevisionRepository {
    Revision guardar(Revision revision);

    Optional<Revision> buscarPorId(String id);

    Optional<Revision> buscarPorPreguntaId(Long idPregunta);

    Revision registrarPreguntaCreada(Long idPregunta);
}