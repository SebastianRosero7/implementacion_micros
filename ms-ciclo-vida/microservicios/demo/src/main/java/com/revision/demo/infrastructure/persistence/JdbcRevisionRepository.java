package com.revision.demo.infrastructure.persistence;

import com.revision.demo.domain.model.Dictamen;
import com.revision.demo.domain.model.Estado;
import com.revision.demo.domain.model.Observacion;
import com.revision.demo.domain.model.Revision;
import com.revision.demo.domain.model.TipoDictamen;
import com.revision.demo.domain.repository.RevisionRepository;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class JdbcRevisionRepository implements RevisionRepository {
    private static final RowMapper<RevisionRow> REVISION_ROW_MAPPER = JdbcRevisionRepository::mapRevision;

    private final JdbcTemplate jdbcTemplate;

    public JdbcRevisionRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    @Transactional
    public Revision guardar(Revision revision) {
        int actualizadas = jdbcTemplate.update(
            """
                UPDATE revision.revisiones
                SET id_pregunta = ?, id_revisor = ?, fecha_asignacion = ?, estado = ?
                WHERE id_revision = ?
                """,
            revision.getIdPregunta(),
            revision.getIdRevisor(),
            revision.getFechaAsignacion(),
            revision.getEstado().name(),
            revision.getId()
        );
        if (actualizadas == 0) {
            jdbcTemplate.update(
                """
                    INSERT INTO revision.revisiones
                        (id_revision, id_pregunta, id_revisor, fecha_asignacion, estado)
                    VALUES (?, ?, ?, ?, ?)
                    """,
                revision.getId(),
                revision.getIdPregunta(),
                revision.getIdRevisor(),
                revision.getFechaAsignacion(),
                revision.getEstado().name()
            );
        }

        jdbcTemplate.update(
            "DELETE FROM revision.observaciones WHERE id_revision = ?",
            revision.getId()
        );
        for (int index = 0; index < revision.getObservaciones().size(); index++) {
            Observacion observacion = revision.getObservaciones().get(index);
            jdbcTemplate.update(
                """
                    INSERT INTO revision.observaciones
                        (id_revision, orden, codigo, descripcion, categoria)
                    VALUES (?, ?, ?, ?, ?)
                    """,
                revision.getId(),
                index + 1,
                observacion.getCodigo(),
                observacion.getDescripcion(),
                observacion.getCategoria()
            );
        }

        jdbcTemplate.update(
            "DELETE FROM revision.dictamenes WHERE id_revision = ?",
            revision.getId()
        );
        Dictamen dictamen = revision.getDictamen();
        if (dictamen != null) {
            jdbcTemplate.update(
                """
                    INSERT INTO revision.dictamenes
                        (id_revision, resultado, justificacion, fecha_emision)
                    VALUES (?, ?, ?, ?)
                    """,
                revision.getId(),
                dictamen.getResultado() == Estado.APROBADO
                    ? TipoDictamen.APROBADA.name()
                    : TipoDictamen.RECHAZADA.name(),
                dictamen.getConcepto(),
                dictamen.getFechaEmision()
            );
        }
        return revision;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Revision> buscarPorId(String id) {
        List<RevisionRow> rows = jdbcTemplate.query(
            """
                SELECT id_revision, id_pregunta, id_revisor, fecha_asignacion, estado
                FROM revision.revisiones
                WHERE id_revision = ?
                """,
            REVISION_ROW_MAPPER,
            id
        );
        if (rows.isEmpty()) {
            return Optional.empty();
        }

        RevisionRow row = rows.get(0);
        List<Observacion> observaciones = jdbcTemplate.query(
            """
                SELECT codigo, descripcion, categoria
                FROM revision.observaciones
                WHERE id_revision = ?
                ORDER BY orden
                """,
            (resultSet, index) -> new Observacion(
                resultSet.getString("codigo"),
                resultSet.getString("descripcion"),
                resultSet.getString("categoria")
            ),
            id
        );
        List<Dictamen> dictamenes = jdbcTemplate.query(
            """
                SELECT resultado, justificacion, fecha_emision
                FROM revision.dictamenes
                WHERE id_revision = ?
                """,
            (resultSet, index) -> mapDictamen(resultSet),
            id
        );
        if (dictamenes.size() > 1) {
            throw new IllegalStateException("Una revisión no puede tener más de un dictamen");
        }

        return Optional.of(Revision.rehidratar(
            row.id(),
            row.idPregunta(),
            row.idRevisor(),
            row.fechaAsignacion(),
            row.estado(),
            observaciones,
            dictamenes.isEmpty() ? null : dictamenes.get(0)
        ));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Revision> buscarPorPreguntaId(Long idPregunta) {
        List<String> ids = jdbcTemplate.query(
            "SELECT id_revision FROM revision.revisiones WHERE id_pregunta = ?",
            (resultSet, index) -> resultSet.getString("id_revision"),
            idPregunta
        );
        if (ids.isEmpty()) {
            return Optional.empty();
        }
        if (ids.size() > 1) {
            throw new IllegalStateException("Una pregunta no puede tener más de una revisión");
        }
        return buscarPorId(ids.get(0));
    }

    @Override
    @Transactional
    public Revision registrarPreguntaCreada(Long idPregunta) {
        return buscarPorPreguntaId(idPregunta).orElseGet(() ->
            guardar(Revision.pendiente(UUID.randomUUID().toString(), idPregunta))
        );
    }

    private static RevisionRow mapRevision(ResultSet resultSet, int rowNumber) throws SQLException {
        var fechaAsignacion = resultSet.getDate("fecha_asignacion");
        long idRevisor = resultSet.getLong("id_revisor");
        Long revisor = resultSet.wasNull() ? null : idRevisor;
        return new RevisionRow(
            resultSet.getString("id_revision"),
            resultSet.getLong("id_pregunta"),
            revisor,
            fechaAsignacion == null ? null : fechaAsignacion.toLocalDate(),
            Estado.valueOf(resultSet.getString("estado"))
        );
    }

    private static Dictamen mapDictamen(ResultSet resultSet) throws SQLException {
        TipoDictamen resultado = TipoDictamen.valueOf(resultSet.getString("resultado"));
        Estado estado = resultado == TipoDictamen.APROBADA ? Estado.APROBADO : Estado.RECHAZADO;
        return new Dictamen(
            estado,
            resultSet.getString("justificacion"),
            resultSet.getDate("fecha_emision").toLocalDate()
        );
    }

    private record RevisionRow(
        String id,
        Long idPregunta,
        Long idRevisor,
        java.time.LocalDate fechaAsignacion,
        Estado estado
    ) {}
}
