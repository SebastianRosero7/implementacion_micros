package com.revision.demo.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.revision.demo.domain.service.GestorTransicionesEstado;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class RevisionTest {
    private final GestorTransicionesEstado gestor = new GestorTransicionesEstado();

    @Test
    void debeRechazarSaltosDeEstado() {
        Revision revision = nuevaRevision();

        assertThrows(
            IllegalStateException.class,
            () -> gestor.transicionar(revision.getEstado(), Estado.APROBADO)
        );
        assertEquals(Estado.PENDIENTE, revision.getEstado());
    }

    @Test
    void debePermitirObservacionesYDictamenSiguiendoElFlujo() {
        Revision revision = nuevaRevision();

        revision.iniciar(gestor);
        revision.agregarObservacion(new Observacion("OBS-1", "Corregir el enunciado", "FORMA"), gestor);
        assertEquals(Estado.CON_OBSERVACIONES, revision.getEstado());
        assertEquals(1, revision.getObservaciones().size());

        revision.iniciar(gestor);
        revision.emitirDictamen(TipoDictamen.APROBADA, "Cumple los criterios", LocalDate.now(), gestor);
        assertEquals(Estado.APROBADO, revision.getEstado());
        assertEquals(Estado.APROBADO, revision.getDictamen().getResultado());

        revision.finalizar(gestor);
        assertEquals(Estado.FINALIZADO, revision.getEstado());
        assertThrows(IllegalStateException.class, () -> revision.iniciar(gestor));
    }

    @Test
    void noDebePermitirDictamenAntesDeIniciarRevision() {
        Revision revision = nuevaRevision();

        assertThrows(
            IllegalStateException.class,
            () -> revision.emitirDictamen(
                TipoDictamen.RECHAZADA,
                "No cumple los criterios",
                LocalDate.now(),
                gestor
            )
        );
        assertEquals(Estado.PENDIENTE, revision.getEstado());
    }

    private static Revision nuevaRevision() {
        return new Revision("revision-1", 10L, 20L, LocalDate.now());
    }
}
