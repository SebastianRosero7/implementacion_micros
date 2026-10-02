package com.revision.demo.application.usecase;

import com.revision.demo.application.dto.DictamenRequestDto;
import com.revision.demo.application.dto.DictamenResponseDTO;
import com.revision.demo.domain.model.Estado;
import com.revision.demo.domain.model.Revision;
import com.revision.demo.domain.model.TipoDictamen;
import com.revision.demo.domain.repository.RevisionRepository;
import com.revision.demo.domain.service.GestorTransicionesEstado;
import java.time.LocalDate;
import java.util.NoSuchElementException;
import java.util.Objects;

public class EmitirDictamenUseCase {

    private final RevisionRepository revisionRepository;
    private final GestorTransicionesEstado gestorTransicionesEstado;

    public EmitirDictamenUseCase(
        RevisionRepository revisionRepository,
        GestorTransicionesEstado gestorTransicionesEstado
    ) {
        this.revisionRepository = Objects.requireNonNull(revisionRepository);
        this.gestorTransicionesEstado = Objects.requireNonNull(gestorTransicionesEstado);
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