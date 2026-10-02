package com.revision.demo.application.dto;

import com.revision.demo.domain.model.Estado;
import java.time.LocalDate;

public record AsignarResponseDTO(
    String idRevision,
    Long idSolicitud,
    Long idEvaluador,
    Estado estado,
    String mensaje,
    LocalDate fechaAsignacion
) {}