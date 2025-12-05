package org.example.miniecommerce.repository.Impl;

import lombok.RequiredArgsConstructor;

import org.example.miniecommerce.entity.Role;
import org.example.miniecommerce.entity.User;
import org.example.miniecommerce.repository.UserRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<User> userRowMapper = new RowMapper<>() {
        @Override
        public User mapRow(ResultSet rs, int rowNum) throws SQLException {
            User user = new User();
            user.setId(rs.getLong("id"));
            user.setEmail(rs.getString("email"));
            user.setPassword(rs.getString("password"));
            user.setName(rs.getString("name"));
            user.setRole(Role.valueOf(rs.getString("role")));
            user.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
            user.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
            return user;
        }
    };

    @Override
    public Optional<User> findById(Long id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        List<User> result = jdbcTemplate.query(sql, userRowMapper, id);
        return result.stream().findFirst();
    }

    @Override
    public Optional<User> findByEmail(String email) {
        String sql = "SELECT * FROM users WHERE email = ?";
        List<User> result = jdbcTemplate.query(sql, userRowMapper, email);
        return result.stream().findFirst();
    }

    @Override
    public boolean existsByEmail(String email) {
        String sql = "SELECT COUNT(*) FROM users WHERE email = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, email);
        return count != null && count > 0;
    }

    @Override
    public List<User> findAll() {
        String sql = "SELECT * FROM users where deleted_at IS NULL";
        return jdbcTemplate.query(sql, userRowMapper);
    }

    @Override
    public void save(User user) {
        LocalDateTime now = LocalDateTime.now();
        if (user.getId() == null) {
            // Insert mới
            String sql = "INSERT INTO users (email, password, name, role,created_at) VALUES (?, ?, ?, ?, ?)";
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql, new String[] { "id" });
                ps.setString(1, user.getEmail());
                ps.setString(2, user.getPassword());
                ps.setString(3, user.getName());
                ps.setString(4, user.getRole().toString());
                ps.setObject(5, now);
                return ps;
            }, keyHolder);
            // Lấy id tự động tạo 
            Long generatedId = keyHolder.getKey().longValue();
            user.setId(generatedId); // Gán id cho user
            user.setCreatedAt(now);

        } else {
            // Update
            String sql = "UPDATE users SET email = ?, password = ?, name = ?, updated_at = ? WHERE id = ?";
            jdbcTemplate.update(sql, user.getEmail(), user.getPassword(), user.getName(), now, user.getId());
            user.setUpdatedAt(now);
        }
    }

    @Override
    public void deleteById(Long id) {
        // 2. Cập nhật deleted_at (xoá mềm)
        String softDeleteSql = "UPDATE users SET deleted_at = ? WHERE id = ?";
        jdbcTemplate.update(softDeleteSql, LocalDateTime.now(), id);
    }

}
