package com.revision.demo.application.port;

import com.revision.demo.application.dto.PreguntaCompletaDTO;

public interface PreguntaCatalogo {
    PreguntaCompletaDTO obtenerCompleta(Long idPregunta);
}
