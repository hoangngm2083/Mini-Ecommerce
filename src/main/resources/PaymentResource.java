package resources;

import org.example.miniecommerce.dto.payment.PaymentResponse;
import org.example.miniecommerce.entity.Payment;

import java.time.LocalDateTime;

public class PaymentResource {

    public static PaymentResponse toResponse(Payment p) {
        Long orderId = p.getOrder() != null ? p.getOrder().getId() : null;
        LocalDateTime created = null;
        LocalDateTime updated = null;
        try {
            created = (LocalDateTime) p.getCreatedAt(); // adjust if BaseEntity uses other type
            updated = (LocalDateTime) p.getUpdatedAt();
        } catch (Exception e) { /* fallback null */ }

        return new PaymentResponse(
                p.getId(),
                orderId,
                p.getAmount(),
                p.getMethod(),
                p.getStatus().name(),
                p.getPaidAt(),
                created,
                updated
        );
    }
}