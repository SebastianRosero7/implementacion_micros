package com.revision.demo.domain.service;

import com.revision.demo.domain.model.Estado;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

public class GestorTransicionesEstado {
    private static final Map<Estado, Set<Estado>> TRANSICIONES = crearTransiciones();

    public Estado transicionar(Estado actual, Estado siguiente) {
        if (actual == null || siguiente == null) {
            throw new IllegalArgumentException("Los estados de origen y destino son obligatorios");
        }
        if (!TRANSICIONES.getOrDefault(actual, Set.of()).contains(siguiente)) {
            throw new IllegalStateException(
                "Transición de estado no permitida: " + actual + " -> " + siguiente
            );
        }
        return siguiente;
    }

    private static Map<Estado, Set<Estado>> crearTransiciones() {
        Map<Estado, Set<Estado>> transiciones = new EnumMap<>(Estado.class);
        transiciones.put(Estado.PENDIENTE, Set.of(Estado.EN_REVISION));
        transiciones.put(
            Estado.EN_REVISION,
            Set.of(Estado.CON_OBSERVACIONES, Estado.APROBADO, Estado.RECHAZADO)
        );
        transiciones.put(Estado.CON_OBSERVACIONES, Set.of(Estado.EN_REVISION));
        transiciones.put(Estado.APROBADO, Set.of(Estado.FINALIZADO));
        transiciones.put(Estado.RECHAZADO, Set.of(Estado.FINALIZADO));
        transiciones.put(Estado.FINALIZADO, Set.of());
        return Map.copyOf(transiciones);
    }
}
