package com.revision.demo.application.port;

import com.revision.demo.domain.model.Revision;
import com.revision.demo.domain.model.TipoDictamen;

public interface DictamenPublisher {
    void publicar(Revision revision, TipoDictamen tipo, String justificacion);
}
