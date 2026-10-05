package com.revision.demo.application.usecase;

import com.revision.demo.application.dto.DictamenRequestDto;
import com.revision.demo.application.dto.DictamenResponseDTO;
import com.revision.demo.domain.model.Estado;
import com.revision.demo.domain.model.Revision;
import com.revision.demo.domain.model.TipoDictamen;
import com.revision.demo.domain.repository.RevisionRepository;
import com.revision.demo.domain.service.GestorTransicionesEstado;
import com.revision.demo.application.port.DictamenPublisher;
import java.time.LocalDate;
import java.util.NoSuchElementException;
import java.util.Objects;

public class EmitirDictamenUseCase {

    private final RevisionRepository revisionRepository;
    private final GestorTransicionesEstado gestorTransicionesEstado;
    private final DictamenPublisher dictamenPublisher;

    public EmitirDictamenUseCase(
        RevisionRepository revisionRepository,
        GestorTransicionesEstado gestorTransicionesEstado
    ) {
        this(revisionRepository, gestorTransicionesEstado, null);
    }

    public EmitirDictamenUseCase(
        RevisionRepository revisionRepository,
        GestorTransicionesEstado gestorTransicionesEstado,
        DictamenPublisher dictamenPublisher
    ) {
        this.revisionRepository = Objects.requireNonNull(revisionRepository);
        this.gestorTransicionesEstado = Objects.requireNonNull(gestorTransicionesEstado);
        this.dictamenPublisher = dictamenPublisher;
    }

    public DictamenResponseDTO ejecutar(DictamenRequestDto request) {
        Objects.requireNonNull(request, "La solicitud del dictamen es obligatoria");

        Revision revision = revisionRepository.buscarPorId(request.evaluacionId())
            .orElseThrow(() -> new NoSuchElementException(
                "No existe la revisión con ID " + request.evaluacionId()
            ));
        revision.emitirDictamen(request.resultado(), request.justificacion(), LocalDate.now(),
            gestorTransicionesEstado);

        Revision guardada = Objects.requireNonNull(
            revisionRepository.guardar(revision),
            "El repositorio debe devolver la revisión guardada"
        );
        var dictamen = Objects.requireNonNull(guardada.getDictamen(),
            "La revisión guardada debe contener el dictamen emitido");

        if (dictamenPublisher != null) {
            dictamenPublisher.publicar(guardada, request.resultado(), request.justificacion());
        }

        return new DictamenResponseDTO(
            guardada.getId(),
            dictamen.getResultado() == Estado.APROBADO
                ? TipoDictamen.APROBADA
                : TipoDictamen.RECHAZADA,
            dictamen.getConcepto(),
            dictamen.getFechaEmision()
        );
    }
}