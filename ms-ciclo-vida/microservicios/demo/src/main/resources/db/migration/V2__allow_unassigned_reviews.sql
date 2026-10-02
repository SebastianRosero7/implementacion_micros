ALTER TABLE revision.revisiones
    ALTER COLUMN id_revisor DROP NOT NULL;

ALTER TABLE revision.revisiones
    ALTER COLUMN fecha_asignacion DROP NOT NULL;

CREATE UNIQUE INDEX IF NOT EXISTS uq_revisiones_pregunta
    ON revision.revisiones (id_pregunta);
