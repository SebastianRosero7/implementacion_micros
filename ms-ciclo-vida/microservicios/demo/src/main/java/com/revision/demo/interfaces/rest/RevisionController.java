package com.revision.demo.interfaces.rest;

import com.revision.demo.application.dto.AsignarRequestDTO;
import com.revision.demo.application.dto.AsignarResponseDTO;
import com.revision.demo.application.dto.DictamenRequestDto;
import com.revision.demo.application.dto.DictamenResponseDTO;
import com.revision.demo.application.dto.EvaluarPreguntaResponseDTO;
import com.revision.demo.application.dto.RegistrarObservacionDTO;
import com.revision.demo.application.dto.RevisionResponseDTO;
import com.revision.demo.application.usecase.AsignarRevisorUseCase;
import com.revision.demo.application.usecase.EmitirDictamenUseCase;
import com.revision.demo.application.usecase.IniciarRevisionUseCase;
import com.revision.demo.application.usecase.RegistrarObservacionUseCase;
import com.revision.demo.domain.repository.RevisionRepository;
import java.util.NoSuchElementException;
import io.grpc.StatusRuntimeException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/revisiones")
public class RevisionController {

    private final AsignarRevisorUseCase asignarRevisorUseCase;
    private final IniciarRevisionUseCase iniciarRevisionUseCase;
    private final RegistrarObservacionUseCase registrarObservacionUseCase;
    private final EmitirDictamenUseCase emitirDictamenUseCase;
    private final RevisionRepository revisionRepository;

    public RevisionController(
        AsignarRevisorUseCase asignarRevisorUseCase,
        IniciarRevisionUseCase iniciarRevisionUseCase,
        RegistrarObservacionUseCase registrarObservacionUseCase,
        EmitirDictamenUseCase emitirDictamenUseCase,
        RevisionRepository revisionRepository
    ) {
        this.asignarRevisorUseCase = asignarRevisorUseCase;
        this.iniciarRevisionUseCase = iniciarRevisionUseCase;
        this.registrarObservacionUseCase = registrarObservacionUseCase;
        this.emitirDictamenUseCase = emitirDictamenUseCase;
        this.revisionRepository = revisionRepository;
    }

    @PostMapping("/asignar")
    public ResponseEntity<AsignarResponseDTO> asignarRevisores(@RequestBody AsignarRequestDTO asignarDTO) {
        AsignarResponseDTO response = asignarRevisorUseCase.ejecutar(asignarDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{idRevision}/iniciar")
    public ResponseEntity<RevisionResponseDTO> iniciarRevision(@PathVariable String idRevision) {
        RevisionResponseDTO response = iniciarRevisionUseCase.ejecutar(idRevision);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/evaluar")
    public ResponseEntity<EvaluarPreguntaResponseDTO> evaluarPregunta(
        @RequestBody RegistrarObservacionDTO evaluacionDTO
    ) {
        EvaluarPreguntaResponseDTO response = registrarObservacionUseCase.ejecutar(evaluacionDTO);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/pregunta/{idPregunta}")
    public ResponseEntity<RevisionResponseDTO> consultarPorPregunta(@PathVariable Long idPregunta) {
        var revision = revisionRepository.buscarPorPreguntaId(idPregunta);
        if (revision.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        var encontrada = revision.orElseThrow();
        return ResponseEntity.ok(new RevisionResponseDTO(
                encontrada.getId(),
                encontrada.getIdPregunta(),
                encontrada.getIdRevisor(),
                encontrada.getEstado(),
                encontrada.getObservaciones().stream().map(observacion ->
                    observacion.getCodigo() + ": " + observacion.getDescripcion()
                ).toList()
            ));
    }

    @PostMapping("/dictamen")
    public ResponseEntity<DictamenResponseDTO> registrarDictamen(@RequestBody DictamenRequestDto dictamenDTO) {
        DictamenResponseDTO response = emitirDictamenUseCase.ejecutar(dictamenDTO);
        return ResponseEntity.ok(response);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> manejarArgumentoInvalido(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<String> manejarNoEncontrado(NoSuchElementException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<String> manejarEstadoInvalido(IllegalStateException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }

    @ExceptionHandler(StatusRuntimeException.class)
    public ResponseEntity<String> manejarServicioPreguntaNoDisponible(StatusRuntimeException ex) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
            .body("El servicio de preguntas no está disponible: " + ex.getStatus().getCode());
    }
}