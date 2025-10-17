package com.sist.baemin.order.payment.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.sist.baemin.order.domain.CartEntity;
import com.sist.baemin.order.domain.CartItemEntity;
import com.sist.baemin.order.domain.CartItemOptionsEntity;
import com.sist.baemin.order.domain.OrderEntity;
import com.sist.baemin.order.domain.OrderItemsEntity;
import com.sist.baemin.order.payment.domain.PaymentEntity;
import com.sist.baemin.order.payment.dto.PaymentPrepareRequest;
import com.sist.baemin.order.payment.dto.PaymentPrepareResponse;
import com.sist.baemin.order.payment.repository.PaymentRepository;
import com.sist.baemin.order.repository.CartRepository;
import com.sist.baemin.order.repository.OrderRepository;
import com.sist.baemin.order.repository.OrderItemsRepository;
import com.sist.baemin.order.repository.PaymentViewRepository;
import com.sist.baemin.store.domain.StoreEntity;
import com.sist.baemin.user.domain.UserEntity;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentViewRepository paymentViewRepository;
    private final CartRepository cartRepository;
    private final OrderRepository orderRepository;
    private final OrderItemsRepository orderItemsRepository;

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * 결제 사전 검증 데이터 저장
     * 클라이언트에서 결제 요청 전에 호출하여 서버에서 계산한 금액을 저장
     * 이 단계에서 주문을 '보류' 상태로 먼저 생성
     */
    public PaymentPrepareResponse preparePayment(Long userId, PaymentPrepareRequest request) {
        try {
            log.info("결제 사전 검증 시작 - userId: {}, portonePaymentId: {}", userId, request.getPortonePaymentId());
            
            // 중복 결제 체크
            Optional<PaymentEntity> existingPayment = paymentRepository.findByPortonePaymentId(request.getPortonePaymentId());
            if (existingPayment.isPresent()) {
                log.warn("이미 존재하는 결제 ID: {}", request.getPortonePaymentId());
                return PaymentPrepareResponse.builder()
                        .success(false)
                        .message("이미 처리 중인 결제입니다.")
                        .build();
            }
            
            // 사용자의 장바구니 조회하여 실제 금액 계산
            log.info("장바구니 조회 중 - userId: {}", userId);
            Optional<CartEntity> cartOpt = cartRepository.findByUserId(userId);
            if (cartOpt.isEmpty()) {
                log.warn("장바구니를 찾을 수 없음 - userId: {}", userId);
                return PaymentPrepareResponse.builder()
                        .success(false)
                        .message("장바구니를 찾을 수 없습니다.")
                        .build();
            }
            
            CartEntity cart = cartOpt.get();
            log.info("장바구니 조회 완료 - cartId: {}, items: {}", cart.getCartId(), cart.getCartItems().size());
            
            // 장바구니 금액 계산
            log.info("장바구니 금액 계산 시작");
            Long calculatedAmount = calculateCartAmount(cart);
            log.info("장바구니 금액 계산 완료 - calculatedAmount: {}", calculatedAmount);
            
            // 클라이언트에서 보낸 금액과 서버에서 계산한 금액 비교
            if (!calculatedAmount.equals(request.getTotalAmount())) {
                log.warn("금액 불일치 - 클라이언트: {}, 서버: {}", request.getTotalAmount(), calculatedAmount);
                return PaymentPrepareResponse.builder()
                        .success(false)
                        .message("결제 금액이 일치하지 않습니다. 페이지를 새로고침해주세요.")
                        .build();
            }
            
            // 사전 검증 데이터 저장 및 보류 상태 주문 생성
            log.info("주문 및 결제 정보 저장 시작");
            PaymentEntity payment = savePreparePaymentWithOrder(request, userId, cart, calculatedAmount);
            
            log.info("결제 사전 검증 완료 - paymentId: {}, orderId: {}, expectedAmount: {}", 
                    payment.getPaymentId(), payment.getOrder().getOrderId(), calculatedAmount);
            
            return PaymentPrepareResponse.builder()
                    .success(true)
                    .message("결제 준비가 완료되었습니다.")
                    .portonePaymentId(request.getPortonePaymentId())
                    .expectedAmount(calculatedAmount)
                    .build();
                    
        } catch (Exception e) {
            log.error("결제 사전 검증 중 오류 발생 - userId: {}, error: {}", userId, e.getMessage(), e);
            return PaymentPrepareResponse.builder()
                    .success(false)
                    .message("결제 준비 중 오류가 발생했습니다: " + e.getMessage())
                    .build();
        }
    }
    
    /**
     * 결제 사전 검증 데이터 저장 및 보류 상태 주문 생성 (트랜잭션)
     */
    @Transactional
    private PaymentEntity savePreparePaymentWithOrder(PaymentPrepareRequest request, Long userId, 
                                                      CartEntity cart, Long calculatedAmount) {
        // 1. PENDING_PAYMENT 상태 주문 생성
        OrderEntity order = new OrderEntity();
        order.setUser(cart.getUser());
        order.setStore(cart.getStore());
        order.setTotalPrice(BigDecimal.valueOf(calculatedAmount));
        order.setOrderStatus("PENDING_PAYMENT"); // 결제 대기 상태
        order.setPaymentMethod(request.getPaymentMethod());
        order.setDeliveryAddress(request.getBuyerName() + " 님의 주소");
        order.setOrderCreatedAt(LocalDateTime.now());
        
        OrderEntity savedOrder = orderRepository.save(order);
        log.info("PENDING_PAYMENT 상태 주문 생성 완료 - orderId: {}", savedOrder.getOrderId());
        
        // 2. OrderItems 생성 (장바구니 기반)
        createOrderItemsFromCart(savedOrder, cart);
        
        // 3. Payment 엔티티 생성 (주문 연결)
        PaymentEntity payment = PaymentEntity.builder()
                .order(savedOrder)  // 주문 연결
                .portonePaymentId(request.getPortonePaymentId())
                .paymentStatus("PENDING")
                .expectedAmount(calculatedAmount)
                .paymentPrice(0L)
                .paymentMethod(request.getPaymentMethod())
                .orderName(request.getOrderName())
                .buyerName(request.getBuyerName())
                .buyerEmail(request.getBuyerEmail())
                .buyerTel(request.getBuyerTel())
                .userId(userId)
                .storeId(cart.getStore().getStoreId())
                .cartId(cart.getCartId())
                .createdAt(LocalDateTime.now())
                .verified(false)
                .build();
        
        return paymentRepository.save(payment);
    }
    
    /**
     * 장바구니 총 금액 계산
     */
    private Long calculateCartAmount(CartEntity cart) {
        try {
            Long totalAmount = 0L;
            
            log.debug("금액 계산 시작 - cartId: {}", cart.getCartId());
            
            for (CartItemEntity item : cart.getCartItems()) {
                log.debug("아이템 처리 - menuId: {}, menuName: {}", 
                         item.getMenu().getMenuId(), item.getMenu().getMenuName());
                
                Long menuPrice = (long) item.getMenu().getPrice();
                Long itemTotal = menuPrice * item.getQuantity();
                
                log.debug("메뉴 가격: {}, 수량: {}, 소계: {}", menuPrice, item.getQuantity(), itemTotal);
                
                // 옵션 금액 추가
                for (CartItemOptionsEntity option : item.getOptions()) {
                    Long optionPrice = option.getMenuOptionValue().getAdditionalPrice().longValue();
                    itemTotal += optionPrice * item.getQuantity();
                    log.debug("옵션 추가 - 옵션명: {}, 가격: {}", 
                             option.getMenuOption().getOptionName(), optionPrice);
                }
                
                totalAmount += itemTotal;
                log.debug("현재 총액: {}", totalAmount);
            }
            
            // 가게별 배달비 적용
            Long deliveryFee = (long) cart.getStore().getDeliveryFee();
            totalAmount += deliveryFee;
            
            log.debug("배달비: {}, 최종 총액: {}", deliveryFee, totalAmount);
            
            return totalAmount;
        } catch (Exception e) {
            log.error("금액 계산 중 오류 발생 - cartId: {}", cart.getCartId(), e);
            throw new RuntimeException("금액 계산 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }
    
    /**
     * 웹훅에서 결제 검증 및 최종 저장
     */
    @Transactional
    public boolean verifyAndCompletePayment(String portonePaymentId, String transactionId, 
                                           Long paidAmount, String status) {
        try {
            log.info("결제 검증 시작 - portonePaymentId: {}, status: {}", portonePaymentId, status);
            
            // 사전 저장된 결제 정보 조회
            Optional<PaymentEntity> paymentOpt = paymentRepository.findByPortonePaymentId(portonePaymentId);
            if (paymentOpt.isEmpty()) {
                log.error("사전 저장된 결제 정보를 찾을 수 없음 - portonePaymentId: {}", portonePaymentId);
                return false;
            }
            
            PaymentEntity payment = paymentOpt.get();
            
            // 이미 검증 완료된 결제인지 확인
            if (Boolean.TRUE.equals(payment.getVerified())) {
                log.warn("이미 검증 완료된 결제 - portonePaymentId: {}", portonePaymentId);
                return true;
            }
            
            // 결제 성공 시 금액 검증
            if ("PAID".equals(status)) {
                // 금액이 웹훅에 없으면 expectedAmount 사용
                if (paidAmount == null) {
                    log.warn("웹훅에 금액 정보 없음, expectedAmount 사용: {}", payment.getExpectedAmount());
                    paidAmount = payment.getExpectedAmount();
                }
                
                if (!payment.getExpectedAmount().equals(paidAmount)) {
                    log.error("결제 금액 불일치 - 예상: {}, 실제: {}", payment.getExpectedAmount(), paidAmount);
                    payment.setPaymentStatus("FAILED");
                    payment.setFailReason("결제 금액 불일치");
                    payment.setPaymentAt(LocalDateTime.now());
                    paymentRepository.save(payment);
                    
                    // 주문도 실패 처리 (주문이 있는 경우)
                    if (payment.getOrder() != null) {
                        OrderEntity order = payment.getOrder();
                        order.setOrderStatus("FAILED");
                        orderRepository.save(order);
                    }
                    return false;
                }
                
                // 주문 상태 업데이트 (보류 -> PAYMENT)
                OrderEntity order = payment.getOrder();
                if (order == null) {
                    // 주문이 없는 경우 (구버전 호환) 새로 생성
                    log.warn("결제에 연결된 주문이 없음, 새로 생성 - paymentId: {}", payment.getPaymentId());
                    order = createOrderFromPayment(payment);
                    payment.setOrder(order);
                } else {
                    // 주문 상태를 'PENDING_PAYMENT'에서 'PAYMENT'로 업데이트
                    log.info("주문 상태 업데이트 - orderId: {}, PENDING_PAYMENT -> PAYMENT", order.getOrderId());
                    order.setOrderStatus("PAYMENT");
                    order.setTotalPrice(BigDecimal.valueOf(paidAmount));
                    orderRepository.save(order);
                }
                
                // 결제 정보 업데이트
                payment.setTransactionId(transactionId);
                payment.setPaymentPrice(paidAmount);
                payment.setPaymentStatus("PAID");
                payment.setPaymentAt(LocalDateTime.now());
                payment.setVerified(true);
                paymentRepository.save(payment);
                
                log.info("결제 검증 완료 및 주문 상태 업데이트 완료 - orderId: {}, status: PAYMENT", order.getOrderId());
                return true;
                
            } else if ("FAILED".equals(status) || "CANCELLED".equals(status)) {
                payment.setTransactionId(transactionId);
                payment.setPaymentStatus(status);
                payment.setPaymentAt(LocalDateTime.now());
                payment.setFailReason("결제 " + (status.equals("FAILED") ? "실패" : "취소"));
                paymentRepository.save(payment);
                
                // 주문 상태도 업데이트 (PENDING_PAYMENT -> 취소/실패)
                if (payment.getOrder() != null) {
                    OrderEntity order = payment.getOrder();
                    order.setOrderStatus(status.equals("FAILED") ? "FAILED" : "CANCELLED");
                    orderRepository.save(order);
                    log.info("주문 상태 업데이트 - orderId: {}, status: {}", order.getOrderId(), order.getOrderStatus());
                }
                
                log.info("결제 {}됨 - portonePaymentId: {}", status.equals("FAILED") ? "실패" : "취소", portonePaymentId);
                return false;
            }
            
            return false;
            
        } catch (Exception e) {
            log.error("결제 검증 중 오류 발생", e);
            return false;
        }
    }
    
    /**
     * 결제 정보로부터 주문 생성
     */
    private OrderEntity createOrderFromPayment(PaymentEntity payment) {
        // Payment에 저장된 userId, storeId, cartId로 주문 생성
        
        // 장바구니 조회
        Optional<CartEntity> cartOpt = cartRepository.findById(payment.getCartId());
        if (cartOpt.isEmpty()) {
            log.error("장바구니를 찾을 수 없음 - cartId: {}", payment.getCartId());
            // 장바구니 없이도 기본 주문 생성
            OrderEntity order = new OrderEntity();
            order.setTotalPrice(BigDecimal.valueOf(payment.getPaymentPrice()));
            order.setOrderStatus("PAYMENT"); // 결제 완료 상태
            order.setPaymentMethod(payment.getPaymentMethod());
            order.setDeliveryAddress(payment.getBuyerName() + " 님의 주소");
            order.setOrderCreatedAt(LocalDateTime.now());
            return orderRepository.save(order);
        }
        
        CartEntity cart = cartOpt.get();
        
        // 주문 생성
        OrderEntity order = new OrderEntity();
        order.setUser(cart.getUser());
        order.setStore(cart.getStore());
        order.setTotalPrice(BigDecimal.valueOf(payment.getPaymentPrice()));
        order.setOrderStatus("PAYMENT"); // 결제 완료 상태
        order.setPaymentMethod(payment.getPaymentMethod());
        order.setDeliveryAddress(payment.getBuyerName() + " 님의 주소"); // 실제로는 주소 정보를 별도로 저장해야 함
        order.setOrderCreatedAt(LocalDateTime.now());
        
        OrderEntity savedOrder = orderRepository.save(order);
        
        // OrderItems 생성 (장바구니 아이템 기반)
        createOrderItemsFromCart(savedOrder, cart);
        
        // 결제 완료 후 장바구니 삭제
        // cartRepository.delete(cart); // 필요 시 활성화
        
        log.info("주문 생성 완료 - orderId: {}, userId: {}, storeId: {}, items: {}, status: PAYMENT", 
                 savedOrder.getOrderId(), cart.getUser().getUserId(), cart.getStore().getStoreId(), 
                 cart.getCartItems().size());
        
        return savedOrder;
    }
    
    /**
     * 장바구니 아이템을 OrderItems로 변환하여 저장
     */
    private void createOrderItemsFromCart(OrderEntity order, CartEntity cart) {
        for (CartItemEntity cartItem : cart.getCartItems()) {
            // 메뉴 기본 가격
            BigDecimal menuPrice = BigDecimal.valueOf(cartItem.getMenu().getPrice());
            
            // 옵션 가격 합계
            BigDecimal optionPriceTotal = BigDecimal.ZERO;
            for (CartItemOptionsEntity option : cartItem.getOptions()) {
                Integer additionalPrice = option.getMenuOptionValue().getAdditionalPrice();
                if (additionalPrice != null) {
                    optionPriceTotal = optionPriceTotal.add(BigDecimal.valueOf(additionalPrice));
                }
            }
            
            // 아이템 총 가격 (메뉴 + 옵션) * 수량
            BigDecimal itemTotalPrice = menuPrice.add(optionPriceTotal)
                    .multiply(BigDecimal.valueOf(cartItem.getQuantity()));
            
            // OrderItems 생성
            OrderItemsEntity orderItem = new OrderItemsEntity();
            orderItem.setOrder(order);
            orderItem.setMenu(cartItem.getMenu());
            orderItem.setMenuName(cartItem.getMenu().getMenuName());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setPrice(itemTotalPrice);
            
            orderItemsRepository.save(orderItem);
            
            log.debug("OrderItem 생성 - menuName: {}, quantity: {}, price: {}", 
                     cartItem.getMenu().getMenuName(), cartItem.getQuantity(), itemTotalPrice);
        }
    }

    /**
     * 기존 createPayment 메서드 (하위 호환성 유지)
     */
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
                            // 결제 완료 시 PAYMENT, 그 외에는 PENDING_PAYMENT 상태
                            newOrder.setOrderStatus(status != null && status.equals("PAID") ? "PAYMENT" : "PENDING_PAYMENT");
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
