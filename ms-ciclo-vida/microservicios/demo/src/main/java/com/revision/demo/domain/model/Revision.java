package com.revision.demo.domain.model;

import com.revision.demo.domain.service.GestorTransicionesEstado;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Revision {
    private final String id;
    private final Long idPregunta;
    private Long idRevisor;
    private LocalDate fechaAsignacion;
    private final List<Observacion> observaciones = new ArrayList<>();
    private Estado estado = Estado.PENDIENTE;
    private Dictamen dictamen;

    public Revision(String id, Long idPregunta, Long idRevisor, LocalDate fechaAsignacion) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("El ID de la revisión es obligatorio");
        }
        if (idPregunta == null || idPregunta <= 0) {
            throw new IllegalArgumentException("El ID de la pregunta debe ser positivo");
        }
        if (idRevisor != null && idRevisor <= 0) {
            throw new IllegalArgumentException("El ID del revisor debe ser positivo");
        }
        if ((idRevisor == null) != (fechaAsignacion == null)) {
            throw new IllegalArgumentException("El revisor y la fecha de asignación deben definirse juntos");
        }
        this.id = id;
        this.idPregunta = idPregunta;
        this.idRevisor = idRevisor;
        this.fechaAsignacion = fechaAsignacion;
    }

    public static Revision pendiente(String id, Long idPregunta) {
        return new Revision(id, idPregunta, null, null);
    }

    public void asignarRevisor(Long idRevisor, LocalDate fechaAsignacion) {
        if (estado != Estado.PENDIENTE || this.idRevisor != null) {
            throw new IllegalStateException("Solo se puede asignar un revisor a una revisión pendiente sin asignar");
        }
        if (idRevisor == null || idRevisor <= 0) {
            throw new IllegalArgumentException("El ID del revisor debe ser positivo");
        }
        this.idRevisor = idRevisor;
        this.fechaAsignacion = Objects.requireNonNull(fechaAsignacion, "La fecha de asignación es obligatoria");
    }

    public static Revision rehidratar(
        String id,
        Long idPregunta,
        Long idRevisor,
        LocalDate fechaAsignacion,
        Estado estado,
        List<Observacion> observaciones,
        Dictamen dictamen
    ) {
        Revision revision = new Revision(id, idPregunta, idRevisor, fechaAsignacion);
        revision.estado = Objects.requireNonNull(estado, "El estado de la revisión es obligatorio");
        revision.observaciones.addAll(List.copyOf(observaciones));
        revision.dictamen = dictamen;
        return revision;
    }

    public void iniciar(GestorTransicionesEstado gestor) {
        estado = Objects.requireNonNull(gestor, "El gestor de transiciones es obligatorio")
            .transicionar(estado, Estado.EN_REVISION);
    }

    public void agregarObservacion(Observacion observacion, GestorTransicionesEstado gestor) {
        Objects.requireNonNull(observacion, "La observación es obligatoria");
        Objects.requireNonNull(gestor, "El gestor de transiciones es obligatorio");
        if (estado != Estado.EN_REVISION) {
            throw new IllegalStateException("Solo se pueden registrar observaciones durante una revisión");
        }

        Estado siguiente = gestor.transicionar(estado, Estado.CON_OBSERVACIONES);
        observaciones.add(observacion);
        estado = siguiente;
    }

    public void emitirDictamen(TipoDictamen tipo, String concepto, LocalDate fechaEmision,
                               GestorTransicionesEstado gestor) {
        Objects.requireNonNull(gestor, "El gestor de transiciones es obligatorio");
        if (estado != Estado.EN_REVISION) {
            throw new IllegalStateException("El dictamen solo se puede emitir durante una revisión");
        }

        Estado resultado = switch (Objects.requireNonNull(tipo, "El tipo de dictamen es obligatorio")) {
            case APROBADA -> Estado.APROBADO;
            case RECHAZADA -> Estado.RECHAZADO;
        };
        Dictamen nuevoDictamen = new Dictamen(resultado, concepto, fechaEmision);
        Estado siguiente = gestor.transicionar(estado, resultado);
        dictamen = nuevoDictamen;
        estado = siguiente;
    }

    public void finalizar(GestorTransicionesEstado gestor) {
        estado = Objects.requireNonNull(gestor, "El gestor de transiciones es obligatorio")
            .transicionar(estado, Estado.FINALIZADO);
    }

    public String getId() {
        return id;
    }

    public Long getIdPregunta() {
        return idPregunta;
    }

    public Long getIdRevisor() {
        return idRevisor;
    }

    public LocalDate getFechaAsignacion() {
        return fechaAsignacion;
    }

    public List<Observacion> getObservaciones() {
        return Collections.unmodifiableList(observaciones);
    }

    public Estado getEstado() {
        return estado;
    }

    public Dictamen getDictamen() {
        return dictamen;
    }
}
