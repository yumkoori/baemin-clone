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
    checkFavoriteStatus(); // 찜 상태 확인
    
    // 하트 버튼에 마우스 이벤트 리스너 추가
    const heartBtn = document.getElementById('heartBtn');
    if (heartBtn) {
        // 마우스 오버 이벤트
        heartBtn.addEventListener('mouseenter', function() {
            const isFavorite = this.getAttribute('data-favorite') === 'true';
            if (isFavorite) {
                // 찜한 상태에서 마우스 오버 시 깨진 하트 표시
                this.textContent = '💔';
            } else {
                // 찜하지 않은 상태에서 마우스 오버 시 빨간 하트 표시
                this.textContent = '❤️';
            }
        });
        
        // 마우스 아웃 이벤트
        heartBtn.addEventListener('mouseleave', function() {
            const isFavorite = this.getAttribute('data-favorite') === 'true';
            if (isFavorite) {
                // 찜한 상태로 복원
                this.textContent = '❤️';
            } else {
                // 찜하지 않은 상태로 복원
                this.textContent = '🤍';
            }
        });
    }
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

// 찜 상태 확인
function checkFavoriteStatus() {
    const storeId = getStoreIdFromUrl();
    if (!storeId) return;
    
    fetch(`/api/stores/${storeId}/favorite-status`)
    .then(response => {
        if (!response.ok) {
            // 로그인하지 않은 사용자나 오류 발생 시 기본값 설정
            updateHeartButton(false);
            return;
        }
        return response.json();
    })
    .then(data => {
        if (data && data.isFavorite !== undefined) {
            updateHeartButton(data.isFavorite);
        }
    })
    .catch(error => {
        console.log('찜 상태 확인 실패:', error);
        // 오류 발생 시 기본값 설정
        updateHeartButton(false);
    });
}

// 하트 버튼 업데이트
function updateHeartButton(isFavorite) {
    const heartBtn = document.getElementById('heartBtn');
    if (heartBtn) {
        heartBtn.textContent = isFavorite ? '❤️' : '🤍';
        // data-favorite 속성으로 현재 상태 저장
        heartBtn.setAttribute('data-favorite', isFavorite);
    }
}

// 찜 토글
function toggleFavorite() {
    const storeId = getStoreIdFromUrl();
    if (!storeId) return;
    
    fetch(`/api/stores/${storeId}/toggle-favorite`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        }
    })
    .then(response => {
        console.log('찜 토글 응답 상태:', response.status);
        // 401 Unauthorized 또는 403 Forbidden일 경우 로그인 필요
        if (response.status === 401 || response.status === 403) {
            showLoginRequiredModal();
            return Promise.reject('Unauthorized');
        }
        // 다른 오류 상태일 경우
        if (!response.ok) {
            alert('찜 처리 중 오류가 발생했습니다.');
            return Promise.reject('Error');
        }
        return response.json();
    })
    .then(data => {
        if (data && data.isFavorite !== undefined) {
            updateHeartButton(data.isFavorite);
        }
    })
    .catch(error => {
        console.log('찜 토글 실패:', error);
        // 네트워크 오류 등으로 catch로 넘어온 경우
        if (error !== 'Unauthorized' && error !== 'Error') {
            alert('찜 처리 중 오류가 발생했습니다.');
        }
    });
}

// 로그인 필요 모달 표시
function showLoginRequiredModal() {
    // 간단한 알림창으로 구현 (필요시 모달로 변경 가능)
    if (confirm('찜 기능을 사용하려면 로그인이 필요합니다. 로그인 페이지로 이동하시겠습니까?')) {
        window.location.href = '/api/login';
    }
}

// URL에서 storeId 추출
function getStoreIdFromUrl() {
    const pathParts = window.location.pathname.split('/');
    const storeIndex = pathParts.indexOf('store');
    if (storeIndex !== -1 && pathParts[storeIndex + 1]) {
        return pathParts[storeIndex + 1];
    }
    return null;
}
