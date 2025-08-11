package com.sist.baemin.order.payment.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sist.baemin.order.domain.OrderEntity;
import com.sist.baemin.order.payment.domain.PaymentEntity;
import com.sist.baemin.order.payment.repository.PaymentRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;

    @Transactional
    public PaymentEntity createPayment(OrderEntity order, Long price, String status) {
        PaymentEntity payment = PaymentEntity.builder()
                .order(order)
                .paymentPrice(price)
                .paymentStatus(status)
                .paymentAt(LocalDateTime.now())
                .build();
        return paymentRepository.save(payment);
    }
}


