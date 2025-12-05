package org.example.miniecommerce.dto.shipping;

public enum UpdateShipmentType {
    ASSIGN_TASK,    // Khi nhân viên nhận task giao hàng: update shipped_by, status, shipped_at
    COMPLETE_TASK   // Khi nhân viên hoàn thành: update status, delivered_at
}
