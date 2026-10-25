package edu.helpdesk.repository.jdbc;

import edu.helpdesk.config.Database;
import edu.helpdesk.exception.DataAccessException;
import edu.helpdesk.model.TicketComment;
import edu.helpdesk.repository.CommentRepository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class JdbcCommentRepository implements CommentRepository {
    @Override
    public TicketComment add(long ticketId, long authorId, String text) {
        String sql = """
                INSERT INTO ticket_comments(ticket_id, author_id, text)
                VALUES (?, ?, ?)
                RETURNING *
                """;
        try (Connection connection = Database.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, ticketId);
            ps.setLong(2, authorId);
            ps.setString(3, text);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return map(rs);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Не удалось добавить комментарий", e);
        }
    }

    @Override
    public List<TicketComment> findByTicketId(long ticketId) {
        String sql = "SELECT * FROM ticket_comments WHERE ticket_id = ? ORDER BY created_at";
        List<TicketComment> result = new ArrayList<>();
        try (Connection connection = Database.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, ticketId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) result.add(map(rs));
                return result;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Не удалось получить комментарии", e);
        }
    }

    private TicketComment map(ResultSet rs) throws SQLException {
        return new TicketComment(
                rs.getLong("id"),
                rs.getLong("ticket_id"),
                rs.getLong("author_id"),
                rs.getString("text"),
                rs.getTimestamp("created_at").toLocalDateTime()
        );
    }
}
