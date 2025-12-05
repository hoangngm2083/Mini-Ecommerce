package org.example.miniecommerce.mediator;

import org.example.miniecommerce.entity.Payment;
import org.example.miniecommerce.entity.Shipping;

public interface OrderProcessingMediator {

    /**
     * Notify when payment is confirmed (success or failed)
     */
    void notifyPaymentConfirmed(Payment payment, boolean success);

    /**
     * Notify when shipment is created
     */
    void notifyShipmentCreated(Shipping shipping);

    /**
     * Notify when shipment status is updated
     */
    void notifyShipmentUpdated(Shipping shipping);
}
