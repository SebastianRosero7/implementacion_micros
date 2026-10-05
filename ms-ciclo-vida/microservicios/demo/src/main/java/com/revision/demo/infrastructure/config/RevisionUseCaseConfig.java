package com.revision.demo.infrastructure.config;

import com.revision.demo.application.usecase.AsignarRevisorUseCase;
import com.revision.demo.application.usecase.EmitirDictamenUseCase;
import com.revision.demo.application.usecase.IniciarRevisionUseCase;
import com.revision.demo.application.usecase.RegistrarObservacionUseCase;
import com.revision.demo.application.port.PreguntaCatalogo;
import com.revision.demo.domain.repository.RevisionRepository;
import com.revision.demo.domain.service.GestorTransicionesEstado;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RevisionUseCaseConfig {

    @Bean
    public GestorTransicionesEstado gestorTransicionesEstado() {
        return new GestorTransicionesEstado();
    }

    @Bean
    public AsignarRevisorUseCase asignarRevisorUseCase(RevisionRepository revisionRepository) {
        return new AsignarRevisorUseCase(revisionRepository);
    }

    @Bean
    public IniciarRevisionUseCase iniciarRevisionUseCase(
        RevisionRepository revisionRepository,
        GestorTransicionesEstado gestorTransicionesEstado
    ) {
        return new IniciarRevisionUseCase(revisionRepository, gestorTransicionesEstado);
    }

    @Bean
    public RegistrarObservacionUseCase registrarObservacionUseCase(
        RevisionRepository revisionRepository,
        GestorTransicionesEstado gestorTransicionesEstado,
        PreguntaCatalogo preguntaCatalogo
    ) {
        return new RegistrarObservacionUseCase(
            revisionRepository,
            gestorTransicionesEstado,
            preguntaCatalogo
        );
    }

    @Bean
    public EmitirDictamenUseCase emitirDictamenUseCase(
        RevisionRepository revisionRepository,
        GestorTransicionesEstado gestorTransicionesEstado,
        com.revision.demo.application.port.DictamenPublisher dictamenPublisher
    ) {
        return new EmitirDictamenUseCase(revisionRepository, gestorTransicionesEstado, dictamenPublisher);
    }
}

