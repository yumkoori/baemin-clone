package com.sist.baemin.order.payment.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.sist.baemin.order.domain.CartEntity;
import com.sist.baemin.order.domain.CartItemEntity;
import com.sist.baemin.order.domain.CartItemOptionsEntity;
import com.sist.baemin.order.domain.OrderEntity;
import com.sist.baemin.order.payment.domain.PaymentEntity;
import com.sist.baemin.order.payment.repository.PaymentRepository;
import com.sist.baemin.order.repository.PaymentViewRepository;
import com.sist.baemin.store.domain.StoreEntity;
import com.sist.baemin.user.domain.UserEntity;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentViewRepository paymentViewRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public PaymentEntity createPayment(OrderEntity order, Long price, String status) {
        OrderEntity orderForPayment = order;

        // controller에서 order가 null로 넘어오는 기존 흐름을 보완: 요청 파라미터를 이용해 주문 생성
        if (orderForPayment == null) {
            try {
                RequestAttributes attrs = RequestContextHolder.getRequestAttributes();
                if (attrs instanceof ServletRequestAttributes servletAttrs) {
                    HttpServletRequest request = servletAttrs.getRequest();
                    String cartItemOptionIdStr = request.getParameter("cartItemOptionId");
                    String payMethod = request.getParameter("pay_method");

                    if (cartItemOptionIdStr != null && !cartItemOptionIdStr.isBlank()) {
                        Long cartItemOptionId = Long.valueOf(cartItemOptionIdStr);
                        CartItemOptionsEntity cio = paymentViewRepository
                                .findByIdWithAllJoins(cartItemOptionId)
                                .orElse(null);
                        if (cio != null) {
                            CartItemEntity cartItem = cio.getCartItem();
                            CartEntity cart = cartItem.getCart();
                            StoreEntity store = cart.getStore();
                            UserEntity user = cart.getUser();

                            OrderEntity newOrder = new OrderEntity();
                            newOrder.setUser(user);
                            newOrder.setStore(store);
                            if (price != null) {
                                newOrder.setTotalPrice(BigDecimal.valueOf(price));
                            }
                            newOrder.setOrderStatus("ORDERED");
                            newOrder.setPaymentMethod(payMethod);
                            newOrder.setOrderCreatedAt(LocalDateTime.now());

                            entityManager.persist(newOrder);
                            orderForPayment = newOrder;
                        }
                    }
                }
            } catch (Exception ignored) {
                // 주문 생성 실패 시 결제는 주문 연결 없이 저장 (기존 동작 유지)
            }
        }

        PaymentEntity payment = PaymentEntity.builder()
                .order(orderForPayment)
                .paymentPrice(price)
                .paymentStatus(status)
                .paymentAt(LocalDateTime.now())
                .build();
        return paymentRepository.save(payment);
    }
}


