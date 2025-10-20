-- 4천원 할인 쿠폰 추가 (main.html에서 사용)
INSERT INTO coupon (coupon_id, coupon_name, coupon_code, description, discount_amount, min_order_amount, expiry_date)
VALUES (1, '4천원 할인 쿠폰', 'EVENT4000', '선착순 100명! 모든 주문에 사용 가능한 4천원 할인 쿠폰', 4000, 15000, DATE_ADD(NOW(), INTERVAL 30 DAY));

-- 추가 샘플 쿠폰들
INSERT INTO coupon (coupon_name, coupon_code, description, discount_amount, min_order_amount, expiry_date)
VALUES 
('첫 주문 1만원 할인', 'FIRST10000', '첫 주문 고객 전용 1만원 할인 쿠폰', 10000, 30000, DATE_ADD(NOW(), INTERVAL 60 DAY)),
('2천원 할인', 'SAVE2000', '최소 주문금액 없이 바로 사용 가능', 2000, 0, DATE_ADD(NOW(), INTERVAL 14 DAY)),
('5천원 할인', 'DISCOUNT5000', '2만원 이상 주문시 5천원 할인', 5000, 20000, DATE_ADD(NOW(), INTERVAL 45 DAY));

-- 확인
SELECT * FROM coupon;

