package com.revision.demo.application.usecase;

import com.revision.demo.application.dto.AsignarRequestDTO;
import com.revision.demo.application.dto.AsignarResponseDTO;
import com.revision.demo.domain.repository.RevisionRepository;
import java.util.Objects;
import java.util.NoSuchElementException;

public class AsignarRevisorUseCase {

    private final RevisionRepository revisionRepository;

    public AsignarRevisorUseCase(RevisionRepository revisionRepository) {
        this.revisionRepository = Objects.requireNonNull(revisionRepository);
    }

    public AsignarResponseDTO ejecutar(AsignarRequestDTO request) {
        Objects.requireNonNull(request, "La solicitud de asignación es obligatoria");

        var revision = revisionRepository.buscarPorPreguntaId(request.idSolicitud())
            .orElseThrow(() -> new NoSuchElementException(
                "No existe una revisión pendiente para la pregunta " + request.idSolicitud()
            ));
        revision.asignarRevisor(request.idEvaluador(), request.fechaAsignacion());
        var guardada = Objects.requireNonNull(revisionRepository.guardar(revision),
            "El repositorio debe devolver la revisión guardada");

        return new AsignarResponseDTO(
            guardada.getId(),
            guardada.getIdPregunta(),
            guardada.getIdRevisor(),
            guardada.getEstado(),
            "Revisor asignado exitosamente",
            guardada.getFechaAsignacion()
        );
    }
}