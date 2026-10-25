package edu.helpdesk.repository.jdbc;

import edu.helpdesk.config.Database;
import edu.helpdesk.exception.DataAccessException;
import edu.helpdesk.model.User;
import edu.helpdesk.model.UserRole;
import edu.helpdesk.repository.UserRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class JdbcUserRepository implements UserRepository {
    @Override
    public Optional<User> findById(long id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        try (Connection connection = Database.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                long department = rs.getLong("department_id");
                Long departmentId = rs.wasNull() ? null : department;
                return Optional.of(new User(
                        rs.getLong("id"),
                        rs.getString("full_name"),
                        rs.getString("email"),
                        UserRole.valueOf(rs.getString("role")),
                        departmentId
                ));
            }
        } catch (SQLException e) {
            throw new DataAccessException("Ошибка чтения пользователя", e);
        }
    }
}
