package com.revision.demo.infrastructure.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.revision.demo.domain.model.Estado;
import com.revision.demo.domain.model.Observacion;
import com.revision.demo.domain.model.Revision;
import com.revision.demo.domain.model.TipoDictamen;
import com.revision.demo.domain.service.GestorTransicionesEstado;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class JdbcRevisionRepositoryTest {
    @Autowired
    private JdbcRevisionRepository repository;

    @Test
    void debePersistirYReconstruirRevisionConObservacionesYDictamen() {
        Revision revision = new Revision(UUID.randomUUID().toString(), 101L, 202L, LocalDate.now());
        GestorTransicionesEstado transiciones = new GestorTransicionesEstado();
        revision.iniciar(transiciones);
        revision.agregarObservacion(
            new Observacion("OBS-1", "Aclarar el enunciado", "FORMA"),
            transiciones
        );
        repository.guardar(revision);

        Revision revisionConObservacion = repository.buscarPorId(revision.getId()).orElseThrow();
        assertEquals(Estado.CON_OBSERVACIONES, revisionConObservacion.getEstado());
        assertEquals(1, revisionConObservacion.getObservaciones().size());
        assertEquals("FORMA", revisionConObservacion.getObservaciones().get(0).getCategoria());

        revisionConObservacion.iniciar(transiciones);
        revisionConObservacion.emitirDictamen(
            TipoDictamen.APROBADA,
            "Cumple los criterios",
            LocalDate.now(),
            transiciones
        );
        repository.guardar(revisionConObservacion);

        Revision recuperada = repository.buscarPorId(revision.getId()).orElseThrow();
        assertEquals(Estado.APROBADO, recuperada.getEstado());
        assertEquals(1, recuperada.getObservaciones().size());
        assertNotNull(recuperada.getDictamen());
        assertEquals(Estado.APROBADO, recuperada.getDictamen().getResultado());
        assertEquals("Cumple los criterios", recuperada.getDictamen().getConcepto());
    }

    @Test
    void debeCrearRevisionPendienteIdempotenteHastaAsignarRevisor() {
        long idPregunta = System.nanoTime();
        Revision creada = repository.registrarPreguntaCreada(idPregunta);
        Revision duplicada = repository.registrarPreguntaCreada(idPregunta);

        assertEquals(creada.getId(), duplicada.getId());
        assertEquals(Estado.PENDIENTE, duplicada.getEstado());
        assertNull(duplicada.getIdRevisor());
        assertNull(duplicada.getFechaAsignacion());

        duplicada.asignarRevisor(202L, LocalDate.now());
        repository.guardar(duplicada);
        assertEquals(202L, repository.buscarPorPreguntaId(idPregunta).orElseThrow().getIdRevisor());
    }
}
