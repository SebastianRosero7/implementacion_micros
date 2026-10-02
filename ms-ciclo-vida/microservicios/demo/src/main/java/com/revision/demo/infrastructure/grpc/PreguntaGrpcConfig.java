package com.revision.demo.infrastructure.grpc;

import co.unicauca.cicloVidaRevision.grpc.PreguntaServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PreguntaGrpcConfig {
    @Bean(destroyMethod = "shutdownNow")
    public ManagedChannel preguntaManagedChannel(
        @Value("${integracion.banco-preguntas.grpc.address:localhost:50051}") String address
    ) {
        return ManagedChannelBuilder.forTarget(address).usePlaintext().build();
    }

    @Bean
    public PreguntaServiceGrpc.PreguntaServiceBlockingStub preguntaServiceBlockingStub(
        ManagedChannel preguntaManagedChannel
    ) {
        return PreguntaServiceGrpc.newBlockingStub(preguntaManagedChannel);
    }
}
