package com.revision.demo.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.revision.demo.application.dto.AsignarRequestDTO;
import com.revision.demo.application.dto.DictamenRequestDto;
import com.revision.demo.application.dto.RegistrarObservacionDTO;
import com.revision.demo.domain.model.Estado;
import com.revision.demo.domain.model.Revision;
import com.revision.demo.domain.model.TipoDictamen;
import com.revision.demo.domain.repository.RevisionRepository;
import com.revision.demo.domain.service.GestorTransicionesEstado;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.List;
import org.junit.jupiter.api.Test;

class RevisionUseCasesTest {
    @Test
    void debeCompletarFlujoDeAsignacionObservacionYDictamen() {
        RevisionRepository repository = new InMemoryRevisionRepository();
        GestorTransicionesEstado gestor = new GestorTransicionesEstado();
        var revisionPendiente = repository.registrarPreguntaCreada(101L);
        var asignacion = new AsignarRevisorUseCase(repository).ejecutar(
            new AsignarRequestDTO(101L, 202L, LocalDate.now())
        );

        assertEquals(Estado.PENDIENTE, asignacion.estado());
        assertEquals(101L, asignacion.idSolicitud());
        assertEquals(revisionPendiente.getId(), asignacion.idRevision());

        new IniciarRevisionUseCase(repository, gestor).ejecutar(asignacion.idRevision());
        var observacion = new RegistrarObservacionUseCase(
            repository,
            gestor,
            preguntaId -> new com.revision.demo.application.dto.PreguntaCompletaDTO(
                preguntaId.toString(),
                "Contexto",
                "Pregunta",
                List.of(),
                "A",
                "Justificación",
                "Competencia",
                "Tema",
                "Subtema",
                "MEDIO",
                "PENDIENTE_REVISION",
                ""
            )
        ).ejecutar(
            new RegistrarObservacionDTO(
                asignacion.idRevision(),
                "OBS-1",
                "Aclarar el enunciado",
                "FORMA"
            )
        );
        assertEquals(Estado.CON_OBSERVACIONES, observacion.revision().estado());

        new IniciarRevisionUseCase(repository, gestor).ejecutar(asignacion.idRevision());
        var dictamen = new EmitirDictamenUseCase(repository, gestor).ejecutar(
            new DictamenRequestDto(
                asignacion.idRevision(),
                TipoDictamen.APROBADA,
                "La pregunta cumple los criterios"
            )
        );

        assertEquals(TipoDictamen.APROBADA, dictamen.resultado());
        assertEquals(Estado.APROBADO, repository.buscarPorId(asignacion.idRevision()).orElseThrow().getEstado());
    }

    private static final class InMemoryRevisionRepository implements RevisionRepository {
        private final Map<String, Revision> revisiones = new HashMap<>();

        @Override
        public Revision guardar(Revision revision) {
            revisiones.put(revision.getId(), revision);
            return revision;
        }

        @Override
        public Optional<Revision> buscarPorId(String id) {
            return Optional.ofNullable(revisiones.get(id));
        }

        @Override
        public Optional<Revision> buscarPorPreguntaId(Long idPregunta) {
            return revisiones.values().stream()
                .filter(revision -> revision.getIdPregunta().equals(idPregunta))
                .findFirst();
        }

        @Override
        public Revision registrarPreguntaCreada(Long idPregunta) {
            return buscarPorPreguntaId(idPregunta).orElseGet(() -> {
                Revision revision = Revision.pendiente(java.util.UUID.randomUUID().toString(), idPregunta);
                return guardar(revision);
            });
        }
    }
}
