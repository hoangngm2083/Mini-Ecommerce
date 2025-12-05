package org.example.miniecommerce.service;

import lombok.RequiredArgsConstructor;
import org.example.miniecommerce.dto.shipping.CreateShipmentRequest;
import org.example.miniecommerce.dto.shipping.UpdateShipmentRequest;
import org.example.miniecommerce.dto.shipping.UpdateShipmentType;
import org.example.miniecommerce.entity.Order;
import org.example.miniecommerce.entity.Shipping;
import org.example.miniecommerce.entity.User;
import org.example.miniecommerce.factory.ShippingFactory;
import org.example.miniecommerce.mediator.OrderProcessingMediator;
import org.example.miniecommerce.repository.ShippingRepository;
import org.example.miniecommerce.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ShippingServiceImpl implements ShippingService {

    private final ShippingRepository repo;
    private final OrderLookupService orderLookup;
    private final OrderProcessingMediator mediator;
    private final UserRepository userRepository;
    private final ShippingFactory shippingFactory;
    private final org.example.miniecommerce.service.shipping.ShippingFeeCalculator feeCalculator;


    @Override
    @Transactional
    public Shipping create(CreateShipmentRequest req) {
        Order order = orderLookup.findByIdOrThrow(req.orderId());
        User shippedBy = null;
        if (req.shippedBy() != null) {
            shippedBy = userRepository.findById(req.shippedBy())
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));
        }
        Shipping s = shippingFactory.fromCreateRequest(req, order, shippedBy);
        repo.save(s);

        // Notify mediator to update order status
        mediator.notifyShipmentCreated(s);

        // Sau khi save, query lại để lấy Shipping mới nhất
        return repo.findByOrderId(order.getId())
                .stream()
                .reduce((first, second) -> second)
                .orElse(s);
    }

    @Override
    @Transactional
    public Shipping update(Long id, UpdateShipmentRequest req, Long userId) {
        Shipping s = repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Đơn vận không tồn tại!"));

        // Validate based on update type
        if (req.type() == UpdateShipmentType.ASSIGN_TASK) {
            // For ASSIGN_TASK: validate status must be CREATED
            if (s.getStatus() != Shipping.Status.CREATED) {
                throw new IllegalArgumentException("Chỉ có thể assign task khi trạng thái là CREATED!");
            }
        } else if (req.type() == UpdateShipmentType.COMPLETE_TASK) {
            // For COMPLETE_TASK: validate userId matches shippedBy and status is SHIPPING
            if (s.getShippedById() == null || !s.getShippedById()
                    .equals(userId)) {
                throw new IllegalArgumentException("Chỉ nhân viên được assign mới có thể hoàn thành task!");
            }
            if (s.getStatus() != Shipping.Status.SHIPPING) {
                throw new IllegalArgumentException("Chỉ có thể hoàn thành task khi trạng thái là SHIPPING!");
            }
        }

        // Get user from userId (from header) instead of req.shippedBy()
        User shippedBy = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Nhân viên không tồn tại!"));
        // if (!"STAFF".equals(userRole)) {
        //     throw new IllegalArgumentException("Chỉ nhân viên (STAFF) mới có thể thực hiện thao tác giao hàng!");
        // }
        ShippingFactory.applyUpdate(s, req, shippedBy);

        // Notify mediator to update order status
        mediator.notifyShipmentUpdated(s);

        repo.update(s);

        return s;
    }

    @Override
    public java.math.BigDecimal getFeeByMethod(String method) {
        return feeCalculator.getFeeByMethod(method);
    }
}
