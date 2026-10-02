package com.revision.demo.application.usecase;

import com.revision.demo.application.dto.RegistrarObservacionDTO;
import com.revision.demo.application.dto.EvaluarPreguntaResponseDTO;
import com.revision.demo.application.port.PreguntaCatalogo;
import com.revision.demo.domain.model.Observacion;
import com.revision.demo.domain.model.Revision;
import com.revision.demo.domain.repository.RevisionRepository;
import com.revision.demo.domain.service.GestorTransicionesEstado;
import java.util.NoSuchElementException;
import java.util.Objects;

public class RegistrarObservacionUseCase {
    private final RevisionRepository revisionRepository;
    private final GestorTransicionesEstado gestorTransicionesEstado;
    private final PreguntaCatalogo preguntaCatalogo;

    public RegistrarObservacionUseCase(
        RevisionRepository revisionRepository,
        GestorTransicionesEstado gestorTransicionesEstado,
        PreguntaCatalogo preguntaCatalogo
    ) {
        this.revisionRepository = Objects.requireNonNull(revisionRepository);
        this.gestorTransicionesEstado = Objects.requireNonNull(gestorTransicionesEstado);
        this.preguntaCatalogo = Objects.requireNonNull(preguntaCatalogo);
    }

    public EvaluarPreguntaResponseDTO ejecutar(RegistrarObservacionDTO request) {
        Objects.requireNonNull(request, "La solicitud de observación es obligatoria");
        Revision revision = revisionRepository.buscarPorId(request.idRevision())
            .orElseThrow(() -> new NoSuchElementException(
                "No existe la revisión con ID " + request.idRevision()
            ));
        var pregunta = preguntaCatalogo.obtenerCompleta(revision.getIdPregunta());
        revision.agregarObservacion(
            new Observacion(request.codigo(), request.descripcion(), request.categoria()),
            gestorTransicionesEstado
        );
        return new EvaluarPreguntaResponseDTO(
            RevisionResponseMapper.toResponse(revisionRepository.guardar(revision)),
            pregunta
        );
    }
}
