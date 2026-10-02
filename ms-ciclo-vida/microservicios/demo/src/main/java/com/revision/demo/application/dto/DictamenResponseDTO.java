package com.revision.demo.application.dto;

import com.revision.demo.domain.model.TipoDictamen;
import java.time.LocalDate;

public record DictamenResponseDTO(
    String evaluacionId,
    TipoDictamen resultado,
    String justificacion,
    LocalDate fechaEmision
) {}