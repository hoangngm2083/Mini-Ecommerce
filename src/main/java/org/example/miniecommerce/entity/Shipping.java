package org.example.miniecommerce.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "shipments")
@SQLDelete(sql = "UPDATE shipments SET deleted_at = NOW() WHERE id=?")
@SQLRestriction("deleted_at IS NULL")


public class Shipping extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(name = "order_id", insertable = false, updatable = false)
    private Long orderId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shipped_by")
    private User shippedBy;

    @Column(name = "shipped_by", insertable = false, updatable = false)
    private Long shippedById;

    @Column(name = "address")
    private String address;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Method method = Method.STANDARD;

    @Column(name = "fee", precision = 12, scale = 2, nullable = false)
    private BigDecimal fee = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.CREATED;

    @Column(name = "shipped_at")
    private LocalDateTime shippedAt;

    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

    private String notes;

    public enum Status {CREATED, SHIPPING, COMPLETED, REJECTED}
    public enum Method {STANDARD, EXPRESS}

}