-- 쿠폰 테이블 생성
CREATE TABLE IF NOT EXISTS coupon (
    coupon_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    coupon_name VARCHAR(100) NOT NULL COMMENT '쿠폰 이름',
    coupon_code VARCHAR(50) NOT NULL UNIQUE COMMENT '쿠폰 코드',
    description VARCHAR(255) COMMENT '쿠폰 설명',
    discount_amount INT NOT NULL COMMENT '할인 금액',
    min_order_amount INT NOT NULL DEFAULT 0 COMMENT '최소 주문 금액',
    expiry_date DATETIME NOT NULL COMMENT '쿠폰 만료일',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
    INDEX idx_coupon_code (coupon_code),
    INDEX idx_expiry_date (expiry_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='쿠폰 정보';

-- 사용자 쿠폰 테이블 생성
CREATE TABLE IF NOT EXISTS userCoupon (
    user_coupon_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '사용자 ID',
    coupon_id BIGINT NOT NULL COMMENT '쿠폰 ID',
    is_used BOOLEAN DEFAULT FALSE COMMENT '사용 여부',
    used_at DATETIME COMMENT '사용 일시',
    issued_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '발급 일시',
    expires_at DATETIME NOT NULL COMMENT '만료 일시',
    FOREIGN KEY (user_id) REFERENCES user(user_id) ON DELETE CASCADE,
    FOREIGN KEY (coupon_id) REFERENCES coupon(coupon_id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_coupon_id (coupon_id),
    INDEX idx_is_used (is_used),
    INDEX idx_expires_at (expires_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='사용자별 보유 쿠폰';

-- 테이블 확인
SHOW TABLES LIKE '%coupon%';

-- 테이블 구조 확인
DESCRIBE coupon;
DESCRIBE userCoupon;

