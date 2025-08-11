// API 호출 유틸리티
class ApiClient {
    constructor(baseUrl = '/api') {
        this.baseUrl = baseUrl;
    }

    async request(endpoint, options = {}) {
        const url = `${this.baseUrl}${endpoint}`;
        
        const config = {
            headers: {
                'Content-Type': 'application/json',
                ...options.headers
            },
            ...options
        };

        if (config.body && typeof config.body === 'object') {
            config.body = JSON.stringify(config.body);
        }

        try {
            const response = await fetch(url, config);
            const data = await response.json();
            
            if (!response.ok) {
                throw new Error(data.message || `HTTP error! status: ${response.status}`);
            }
            
            // success 필드가 없으면 resultCode로 판단
            if (data.success === undefined && data.resultCode !== undefined) {
                data.success = data.resultCode >= 200 && data.resultCode < 300;
            }
            
            return data;
        } catch (error) {
            console.error('API Request failed:', error);
            throw error;
        }
    }

    // GET 요청
    get(endpoint, params = {}) {
        const queryString = new URLSearchParams(params).toString();
        const url = queryString ? `${endpoint}?${queryString}` : endpoint;
        
        return this.request(url, {
            method: 'GET'
        });
    }

    // POST 요청
    post(endpoint, data = {}) {
        return this.request(endpoint, {
            method: 'POST',
            body: data
        });
    }

    // PUT 요청
    put(endpoint, data = {}) {
        return this.request(endpoint, {
            method: 'PUT',
            body: data
        });
    }

    // DELETE 요청
    delete(endpoint) {
        return this.request(endpoint, {
            method: 'DELETE'
        });
    }
}

// API 클라이언트 인스턴스
const api = new ApiClient();

// 장바구니 API
const cartApi = {
    // 장바구니 조회
    getCart: () => api.get('/cart'),
    
    // 장바구니에 추가
    addItem: (item) => api.post('/cart/items', item),
    
    // 수량 변경
    updateItem: (cartItemId, data) => api.put(`/cart/items/${cartItemId}`, data),
    
    // 항목 삭제
    deleteItem: (cartItemId) => api.delete(`/cart/items/${cartItemId}`),
    
    // 전체 삭제
    clearCart: () => api.delete('/cart'),
    
    // 옵션 변경
    updateItemOptions: (cartItemId, options) => api.put(`/cart/items/${cartItemId}/options`, { options })
};

// 주문 API
const orderApi = {
    // 주문서 정보 조회
    getCheckout: () => api.get('/orders/checkout'),
    
    // 주문 생성
    createOrder: (orderData) => api.post('/orders', orderData),
    
    // 주문 상태 조회
    getOrderStatus: (orderId) => api.get(`/orders/${orderId}/status`),
    
    // 주문 내역 조회
    getOrderHistory: (params) => api.get('/orders', params)
};

// 결제 API
const paymentApi = {
    // 결제 처리
    processPayment: (paymentData) => api.post('/payments', paymentData),
    
    // 결제 내역 조회
    getPaymentInfo: (orderId) => api.get('/payments', { orderId }),
    
    // 결제 수단 조회
    getPaymentMethods: () => api.get('/users/payment-methods'),
    
    // 결제 수단 추가
    addPaymentMethod: (methodData) => api.post('/users/payment-methods', methodData),
    
    // 결제 수단 삭제
    deletePaymentMethod: (methodId) => api.delete(`/users/payment-methods/${methodId}`)
};

// 메뉴 옵션 API
const menuApi = {
    // 메뉴의 전체 옵션 조회
    getMenuOptions: (menuId) => api.get(`/menu/${menuId}/options`)
};

// 찜 API
const favoriteApi = {
    // 찜 목록 조회
    getFavorites: (params) => api.get('/favorites', params),
    
    // 찜 추가
    addFavorite: (favoriteData) => api.post('/favorites', favoriteData),
    
    // 찜 삭제
    deleteFavorite: (favoriteId) => api.delete(`/favorites/${favoriteId}`),
    
    // 찜 상태 확인
    checkFavoriteStatus: (type, targetId) => api.get('/favorites/check', { type, targetId })
};

// 공통 유틸리티 함수
const utils = {
    // 가격 포맷팅
    formatPrice: (price) => {
        return new Intl.NumberFormat('ko-KR').format(price) + '원';
    },
    
    // 날짜 포맷팅
    formatDate: (dateString) => {
        const date = new Date(dateString);
        return date.toLocaleDateString('ko-KR', {
            year: 'numeric',
            month: 'long',
            day: 'numeric',
            hour: '2-digit',
            minute: '2-digit'
        });
    },
    
    // 알림 표시
    showAlert: (message, type = 'info') => {
        const alertDiv = document.createElement('div');
        alertDiv.className = `alert alert-${type}`;
        alertDiv.textContent = message;
        alertDiv.style.cssText = `
            position: fixed;
            top: 20px;
            right: 20px;
            padding: 12px 20px;
            border-radius: 8px;
            color: white;
            font-weight: 600;
            z-index: 1000;
            animation: slideIn 0.3s ease;
        `;
        
        // 배경색 설정
        switch (type) {
            case 'success':
                alertDiv.style.backgroundColor = '#28a745';
                break;
            case 'error':
                alertDiv.style.backgroundColor = '#dc3545';
                break;
            case 'warning':
                alertDiv.style.backgroundColor = '#ffc107';
                alertDiv.style.color = '#333';
                break;
            default:
                alertDiv.style.backgroundColor = '#17a2b8';
        }
        
        document.body.appendChild(alertDiv);
        
        // 3초 후 자동 제거
        setTimeout(() => {
            alertDiv.remove();
        }, 3000);
    },
    
    // 로딩 스피너 표시/숨김
    showLoading: (show = true) => {
        let spinner = document.getElementById('loading-spinner');
        
        if (show) {
            if (!spinner) {
                spinner = document.createElement('div');
                spinner.id = 'loading-spinner';
                spinner.innerHTML = `
                    <div style="
                        position: fixed;
                        top: 0;
                        left: 0;
                        width: 100%;
                        height: 100%;
                        background: rgba(0,0,0,0.5);
                        display: flex;
                        justify-content: center;
                        align-items: center;
                        z-index: 9999;
                    ">
                        <div style="
                            background: white;
                            padding: 20px;
                            border-radius: 8px;
                            text-align: center;
                        ">
                            <div style="
                                width: 40px;
                                height: 40px;
                                border: 4px solid #f3f3f3;
                                border-top: 4px solid #2ac1bc;
                                border-radius: 50%;
                                animation: spin 1s linear infinite;
                                margin: 0 auto 10px;
                            "></div>
                            <div>처리 중...</div>
                        </div>
                    </div>
                `;
                document.body.appendChild(spinner);
            }
        } else {
            if (spinner) {
                spinner.remove();
            }
        }
    }
};

// CSS 애니메이션 추가
const style = document.createElement('style');
style.textContent = `
    @keyframes slideIn {
        from { transform: translateX(100%); opacity: 0; }
        to { transform: translateX(0); opacity: 1; }
    }
    
    @keyframes spin {
        0% { transform: rotate(0deg); }
        100% { transform: rotate(360deg); }
    }
`;
document.head.appendChild(style);