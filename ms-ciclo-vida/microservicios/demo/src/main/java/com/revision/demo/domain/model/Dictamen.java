package com.revision.demo.domain.model;

import java.time.LocalDate;
import java.util.Objects;

public class Dictamen {
    private final Estado resultado;
    private final String concepto;
    private final LocalDate fechaEmision;

    public Dictamen(Estado resultado, String concepto, LocalDate fechaEmision) {
        this.resultado = Objects.requireNonNull(resultado, "El resultado del dictamen es obligatorio");
        if (resultado != Estado.APROBADO && resultado != Estado.RECHAZADO) {
            throw new IllegalArgumentException("El resultado del dictamen debe ser APROBADO o RECHAZADO");
        }
        this.concepto = Objects.requireNonNull(concepto, "El concepto es obligatorio");
        if (concepto.isBlank()) {
            throw new IllegalArgumentException("El concepto es obligatorio");
        }
        this.fechaEmision = Objects.requireNonNull(fechaEmision, "La fecha de emisión es obligatoria");
    }

    public Estado getResultado() {
        return resultado;
    }

    public String getConcepto() {
        return concepto;
    }

    public LocalDate getFechaEmision() {
        return fechaEmision;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Dictamen dictamen = (Dictamen) o;
        return resultado == dictamen.resultado && Objects.equals(concepto, dictamen.concepto);
    }

    @Override
    public int hashCode() {
        return Objects.hash(resultado, concepto);
    }
}