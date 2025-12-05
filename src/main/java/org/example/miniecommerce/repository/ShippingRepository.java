package org.example.miniecommerce.repository;

import org.example.miniecommerce.entity.Shipping;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class ShippingRepository {

    private final JdbcTemplate jdbcTemplate;

    public ShippingRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void save(Shipping shipping) {
        String sql = "INSERT INTO shipments (order_id, shipped_by, address, method, fee, status, notes, created_at, updated_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, NOW(), NOW())";
        jdbcTemplate.update(sql,
            shipping.getOrderId(),
            shipping.getShippedById(),
            shipping.getAddress(),
            shipping.getMethod().name(),
            shipping.getFee(),
            shipping.getStatus().name(),
            shipping.getNotes()
        );
    }

    public void update(Shipping shipping) {
        String sql = "UPDATE shipments SET shipped_by = ?, address = ?, method = ?, fee = ?, status = ?, shipped_at = ?, delivered_at = ?, notes = ?, updated_at = NOW() WHERE id = ?";
        jdbcTemplate.update(sql,
            shipping.getShippedById(),
            shipping.getAddress(),
            shipping.getMethod().name(),
            shipping.getFee(),
            shipping.getStatus().name(),
            shipping.getShippedAt(),
            shipping.getDeliveredAt(),
            shipping.getNotes(),
            shipping.getId()
        );
    }

    public Optional<Shipping> findById(Long id) {
        String sql = "SELECT id, order_id, shipped_by, address, method, fee, status, shipped_at, delivered_at, notes, created_at, updated_at FROM shipments WHERE id = ?";
        List<Shipping> results = jdbcTemplate.query(sql, ps -> ps.setLong(1, id), (rs, rowNum) -> {
            Shipping s = new Shipping();
            s.setId(rs.getLong("id"));
            s.setOrderId(rs.getLong("order_id"));
            s.setShippedById(rs.getLong("shipped_by"));
            s.setAddress(rs.getString("address"));
            s.setMethod(Shipping.Method.valueOf(rs.getString("method")));
            s.setFee(rs.getBigDecimal("fee"));
            s.setStatus(Shipping.Status.valueOf(rs.getString("status")));
            if (rs.getTimestamp("shipped_at") != null) {
                s.setShippedAt(rs.getTimestamp("shipped_at").toLocalDateTime());
            }
            if (rs.getTimestamp("delivered_at") != null) {
                s.setDeliveredAt(rs.getTimestamp("delivered_at").toLocalDateTime());
            }
            if (rs.getTimestamp("created_at") != null) {
                s.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
            }
            if (rs.getTimestamp("updated_at") != null) {
                s.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
            }
            s.setNotes(rs.getString("notes"));
            return s;
        });
        return results.stream().findFirst();
    }

    public List<Shipping> findByOrderId(Long orderId) {
        String sql = "SELECT id, order_id, shipped_by, address, method, fee, status, shipped_at, delivered_at, notes, created_at, updated_at FROM shipments WHERE order_id = ?";
        return jdbcTemplate.query(sql, ps -> ps.setLong(1, orderId), (rs, rowNum) -> {
            Shipping s = new Shipping();
            s.setId(rs.getLong("id"));
            s.setOrderId(rs.getLong("order_id"));
            s.setShippedById(rs.getLong("shipped_by"));
            s.setAddress(rs.getString("address"));
            s.setMethod(Shipping.Method.valueOf(rs.getString("method")));
            s.setFee(rs.getBigDecimal("fee"));
            s.setStatus(Shipping.Status.valueOf(rs.getString("status")));
            if (rs.getTimestamp("shipped_at") != null) {
                s.setShippedAt(rs.getTimestamp("shipped_at").toLocalDateTime());
            }
            if (rs.getTimestamp("delivered_at") != null) {
                s.setDeliveredAt(rs.getTimestamp("delivered_at").toLocalDateTime());
            }
            if (rs.getTimestamp("created_at") != null) {
                s.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
            }
            if (rs.getTimestamp("updated_at") != null) {
                s.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
            }
            s.setNotes(rs.getString("notes"));
            return s;
        });
    }
}