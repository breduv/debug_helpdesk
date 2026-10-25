package edu.helpdesk.repository.jdbc;

import edu.helpdesk.config.Database;
import edu.helpdesk.exception.DataAccessException;
import edu.helpdesk.model.Ticket;
import edu.helpdesk.model.TicketPriority;
import edu.helpdesk.model.TicketStatus;
import edu.helpdesk.repository.TicketRepository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcTicketRepository implements TicketRepository {

    @Override
    public Ticket create(String title, String description, TicketPriority priority, long authorId, Long categoryId) {
        String sql = """
                INSERT INTO tickets(title, description, status, priority, author_id, category_id)
                VALUES (?, ?, 'NEW', ?, ?, ?)
                RETURNING *
                """;
        try (Connection connection = Database.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, title);
            ps.setString(2, description);
            ps.setString(3, priority.name());
            ps.setLong(4, authorId);
            if (categoryId == null) ps.setNull(5, Types.BIGINT); else ps.setLong(5, categoryId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return mapTicket(rs);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Не удалось создать заявку", e);
        }
    }

    @Override
    public Optional<Ticket> findById(long id) {
        String sql = "SELECT * FROM tickets WHERE id = ?";
        try (Connection connection = Database.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapTicket(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new DataAccessException("Ошибка чтения заявки", e);
        }
    }

    @Override
    public List<Ticket> findAll() {
        String sql = "SELECT * FROM tickets ORDER BY id";
        return list(sql, ps -> { });
    }

    @Override
    public List<Ticket> findByStatus(TicketStatus status) {
        String sql = "SELECT * FROM tickets WHERE status = ? ORDER BY id";
        return list(sql, ps -> ps.setString(1, status.name()));
    }

    @Override
    public List<Ticket> searchByTitle(String query) {
        String sql = "SELECT * FROM tickets WHERE LOWER(title) = LOWER(?) ORDER BY id";
        return list(sql, ps -> ps.setString(1, query));
    }

    @Override
    public int assign(long ticketId, long assigneeId) {
        String sql = "UPDATE tickets SET assignee_id = ?, updated_at = CURRENT_TIMESTAMP";
        try (Connection connection = Database.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, assigneeId);
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Ошибка назначения исполнителя", e);
        }
    }

    @Override
    public void changeStatusAndRecordHistory(long ticketId,
                                             TicketStatus oldStatus,
                                             TicketStatus newStatus,
                                             long changedBy) {
        String update = "UPDATE tickets SET status = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        String history = """
                INSERT INTO ticket_status_history(ticket_id, old_status, new_status, changed_by)
                VALUES (?, ?, ?, ?)
                """;

        try (Connection connection = Database.getConnection();
             PreparedStatement ps = connection.prepareStatement(update)) {
            ps.setString(1, newStatus.name());
            ps.setLong(2, ticketId);
            if (ps.executeUpdate() != 1) {
                throw new SQLException("Заявка не найдена: " + ticketId);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Не удалось изменить статус", e);
        }

        try (Connection connection = Database.getConnection();
             PreparedStatement ps = connection.prepareStatement(history)) {
            ps.setLong(1, ticketId);
            ps.setString(2, oldStatus.name());
            ps.setString(3, newStatus.name());
            ps.setLong(4, changedBy);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Не удалось записать историю статуса", e);
        }
    }

    private List<Ticket> list(String sql, SqlBinder binder) {
        List<Ticket> result = new ArrayList<>();
        try (Connection connection = Database.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            binder.bind(ps);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) result.add(mapTicket(rs));
            }
            return result;
        } catch (SQLException e) {
            throw new DataAccessException("Ошибка выборки заявок", e);
        }
    }

    private Ticket mapTicket(ResultSet rs) throws SQLException {
        return new Ticket(
                rs.getLong("id"),
                rs.getString("title"),
                rs.getString("description"),
                TicketStatus.valueOf(rs.getString("status")),
                TicketPriority.valueOf(rs.getString("priority")),
                rs.getLong("author_id"),
                nullableLong(rs, "assignee_id"),
                nullableLong(rs, "category_id"),
                rs.getTimestamp("created_at").toLocalDateTime(),
                rs.getTimestamp("updated_at").toLocalDateTime()
        );
    }

    private Long nullableLong(ResultSet rs, String column) throws SQLException {
        long value = rs.getLong(column);
        return rs.wasNull() ? null : value;
    }

    @FunctionalInterface
    private interface SqlBinder {
        void bind(PreparedStatement statement) throws SQLException;
    }
}
