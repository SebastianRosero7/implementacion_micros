CREATE SCHEMA IF NOT EXISTS revision;

CREATE TABLE IF NOT EXISTS revision.revisiones (
    id_revision VARCHAR(36) PRIMARY KEY,
    id_pregunta BIGINT NOT NULL CHECK (id_pregunta > 0),
    id_revisor BIGINT CHECK (id_revisor > 0),
    fecha_asignacion DATE,
    estado VARCHAR(32) NOT NULL CHECK (
        estado IN (
            'PENDIENTE',
            'EN_REVISION',
            'CON_OBSERVACIONES',
            'APROBADO',
            'RECHAZADO',
            'FINALIZADO'
        )
    )
);

CREATE TABLE IF NOT EXISTS revision.observaciones (
    id_revision VARCHAR(36) NOT NULL,
    orden INTEGER NOT NULL CHECK (orden > 0),
    codigo VARCHAR(100) NOT NULL,
    descripcion TEXT NOT NULL,
    categoria VARCHAR(100),
    PRIMARY KEY (id_revision, orden),
    CONSTRAINT fk_observaciones_revision
        FOREIGN KEY (id_revision)
        REFERENCES revision.revisiones (id_revision)
        ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS revision.dictamenes (
    id_revision VARCHAR(36) PRIMARY KEY,
    resultado VARCHAR(16) NOT NULL CHECK (resultado IN ('APROBADA', 'RECHAZADA')),
    justificacion TEXT NOT NULL,
    fecha_emision DATE NOT NULL,
    CONSTRAINT fk_dictamenes_revision
        FOREIGN KEY (id_revision)
        REFERENCES revision.revisiones (id_revision)
        ON DELETE CASCADE
);
