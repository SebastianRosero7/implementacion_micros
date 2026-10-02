package com.revision.demo.infrastructure.grpc;

import co.unicauca.cicloVidaRevision.grpc.ObtenerPreguntaRequest;
import co.unicauca.cicloVidaRevision.grpc.PreguntaCompletaResponse;
import co.unicauca.cicloVidaRevision.grpc.PreguntaServiceGrpc;
import com.revision.demo.application.dto.PreguntaCompletaDTO;
import com.revision.demo.application.port.PreguntaCatalogo;
import java.util.concurrent.TimeUnit;
import java.util.NoSuchElementException;
import org.springframework.stereotype.Component;

@Component
public class PreguntaGrpcClient implements PreguntaCatalogo {
    private final PreguntaServiceGrpc.PreguntaServiceBlockingStub stub;

    public PreguntaGrpcClient(PreguntaServiceGrpc.PreguntaServiceBlockingStub stub) {
        this.stub = stub;
    }

    @Override
    public PreguntaCompletaDTO obtenerCompleta(Long idPregunta) {
        PreguntaCompletaResponse pregunta = stub.withDeadlineAfter(3, TimeUnit.SECONDS)
            .obtenerPreguntaCompleta(ObtenerPreguntaRequest.newBuilder()
                .setPreguntaId(idPregunta.toString())
                .build());
        if (!pregunta.getEncontrada()) {
            throw new NoSuchElementException("No existe la pregunta " + idPregunta + " en MS-1");
        }
        return new PreguntaCompletaDTO(
            pregunta.getPreguntaId(),
            pregunta.getContexto(),
            pregunta.getPreguntaDirecta(),
            pregunta.getDistractoresList().stream()
                .map(distractor -> new PreguntaCompletaDTO.DistractorDTO(
                    distractor.getId(),
                    distractor.getTexto()
                ))
                .toList(),
            pregunta.getRespuestaCorrectaId(),
            pregunta.getJustificacion(),
            pregunta.getCompetencia(),
            pregunta.getTema(),
            pregunta.getSubtema(),
            pregunta.getNivelDificultad(),
            pregunta.getEstado(),
            pregunta.getBibliografia()
        );
    }
}
