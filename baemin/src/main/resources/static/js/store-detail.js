function showMenuTab(tabName) {
    // 모든 탭 숨기기
    const tabs = document.querySelectorAll('.menu-tab');
    tabs.forEach(tab => tab.style.display = 'none');
    
    // 모든 탭 버튼 비활성화
    const tabButtons = document.querySelectorAll('.tab-item');
    tabButtons.forEach(btn => btn.classList.remove('active'));
    
    // 선택된 탭 보이기
    document.getElementById(tabName).style.display = 'block';
    
    // 선택된 탭 버튼 활성화
    event.target.classList.add('active');
}

// 페이지 로드 시 장바구니 개수 업데이트
document.addEventListener('DOMContentLoaded', function() {
    updateCartCount();
});

function updateCartCount() {
    fetch('/api/cart')
    .then(response => {
        if (!response.ok) {
            throw new Error('Cart API response not ok');
        }
        return response.json();
    })
    .then(data => {
        if (data.success && data.data && data.data.items) {
            const totalItems = data.data.items.length;
            
            // 장바구니 버튼 업데이트
            const cartButton = document.querySelector('.cart-button');
            const cartSpan = document.querySelector('.cart-button span');
            
            if (totalItems > 0) {
                if (cartButton) {
                    cartButton.style.display = 'block';
                }
                if (cartSpan) {
                    cartSpan.textContent = totalItems;
                }
            } else {
                if (cartButton) {
                    cartButton.style.display = 'none';
                }
            }
        }
    })
    .catch(error => {
        console.log('장바구니 정보 조회 실패:', error);
        // 에러 발생 시 장바구니 버튼 숨기기
        const cartButton = document.querySelector('.cart-button');
        if (cartButton) {
            cartButton.style.display = 'none';
        }
    });
}
