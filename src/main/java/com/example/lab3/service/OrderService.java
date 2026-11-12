package com.example.lab3.service;

import com.example.lab3.dto.OrderDto;
import com.example.lab3.dto.OrderItemDto;
import com.example.lab3.entity.Order;
import com.example.lab3.entity.OrderItem;
import com.example.lab3.entity.Product;
import com.example.lab3.repository.OrderRepository;
import com.example.lab3.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepo;
    private final ProductRepository productRepo;

    @Transactional
    public OrderDto create(OrderDto dto) {
        Order order = new Order();
        orderRepo.save(order);                       // 1) persister la commande
        for (OrderItemDto i : dto.items()) {         // 2) puis chaque item
            Product p = productRepo.findById(i.productId())
                    .orElseThrow(() -> new EntityNotFoundException("Product not found"));
            OrderItem item = new OrderItem();
            item.setProduct(p);
            item.setQuantity(i.quantity());
            item.setOrder(order);
            order.getItems().add(item);
        }
        return toDto(orderRepo.save(order));
    }

    @Transactional(readOnly = true)
    public OrderDto getById(Long id) {
        return toDto(orderRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Order not found")));
    }

    private OrderDto toDto(Order o) {
        return new OrderDto(o.getId(), o.getOrderDate(),
                o.getItems().stream()
                        .map(it -> new OrderItemDto(it.getId(), it.getProduct().getId(), it.getQuantity()))
                        .toList());
    }
}