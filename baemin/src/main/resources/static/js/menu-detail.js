let quantity = 1;
let basePrice = 0;
let selectedOptions = new Set();
let storeId = 0;
let menuId = 0;

// 쿠키에서 값 가져오는 함수
function getCookie(name) {
    const value = `; ${document.cookie}`;
    const parts = value.split(`; ${name}=`);
    if (parts.length === 2) return parts.pop().split(';').shift();
    return null;
}

// 페이지 로드 시 초기화
document.addEventListener('DOMContentLoaded', function() {
    // HTML에서 설정한 데이터 가져오기
    basePrice = parseInt(document.querySelector('[data-base-price]').getAttribute('data-base-price'));
    storeId = parseInt(document.querySelector('[data-store-id]').getAttribute('data-store-id'));
    menuId = parseInt(document.querySelector('[data-menu-id]').getAttribute('data-menu-id'));
    
    // 필수 옵션들 자동 선택
    const requiredOptions = document.querySelectorAll('input[type="checkbox"]:disabled');
    requiredOptions.forEach(checkbox => {
        checkbox.checked = true;
        selectedOptions.add(checkbox.id.replace('checkbox-', ''));
    });
    updateTotalPrice();
});

function toggleOption(optionId) {
    const checkbox = document.getElementById('checkbox-' + optionId);
    const optionItem = document.getElementById('option-' + optionId);
    
    if (checkbox.checked) {
        checkbox.checked = false;
        optionItem.classList.remove('selected');
        selectedOptions.delete(optionId.toString());
    } else {
        checkbox.checked = true;
        optionItem.classList.add('selected');
        selectedOptions.add(optionId.toString());
    }
    
    updateTotalPrice();
}

function increaseQuantity() {
    quantity++;
    document.getElementById('quantity').textContent = quantity;
    updateTotalPrice();
}

function decreaseQuantity() {
    if (quantity > 1) {
        quantity--;
        document.getElementById('quantity').textContent = quantity;
        updateTotalPrice();
    }
}

function updateTotalPrice() {
    // 옵션 가격 계산
    let optionPrice = 0;
    selectedOptions.forEach(optionId => {
        const optionElement = document.getElementById('option-' + optionId);
        if (optionElement) {
            const priceElement = optionElement.querySelector('.option-price');
            if (priceElement) {
                // data-price 속성에서 직접 가격 가져오기
                const priceData = priceElement.getAttribute('data-price');
                const price = priceData ? parseInt(priceData) : 0;
                optionPrice += price;
            }
        }
    });
    
    const totalPrice = (basePrice + optionPrice) * quantity;
    document.getElementById('totalPrice').textContent = '₩' + totalPrice.toLocaleString() + ' 담기';
}

function addToCart() {
    // 버튼 비활성화
    const cartButton = document.querySelector('.btn-cart');
    cartButton.disabled = true;
    cartButton.textContent = '담는 중...';
    
    // 선택된 옵션들을 API 스펙에 맞게 변환
    const options = Array.from(selectedOptions).map(optionId => {
        const optionElement = document.getElementById('option-' + optionId);
        const optionName = optionElement.querySelector('.option-name').textContent;
        
        return {
            optionId: parseInt(optionId),
            optionValue: optionName
        };
    });
    
    const cartData = {
        storeId: storeId,
        menuId: menuId,
        quantity: quantity,
        options: options
    };
    
    // JWT 토큰 가져오기 (쿠키에서)
    const token = getCookie('Authorization');
    const headers = {
        'Content-Type': 'application/json',
    };
    
    // 토큰이 있으면 Authorization 헤더 추가
    if (token) {
        headers['Authorization'] = 'Bearer ' + token;
    }
    
    fetch('/api/cart/items', {
        method: 'POST',
        headers: headers,
        body: JSON.stringify(cartData)
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            alert(data.message);
            // 가게 상세 페이지로 이동
            location.href = '/api/store/' + storeId;
        } else {
            alert('장바구니 담기에 실패했습니다: ' + data.message);
            // 버튼 상태 복원
            cartButton.disabled = false;
            cartButton.innerHTML = '<span id="totalPrice">' + document.getElementById('totalPrice').textContent + '</span>';
        }
    })
    .catch(error => {
        console.error('Error:', error);
        alert('장바구니 담기에 실패했습니다.');
        // 버튼 상태 복원
        cartButton.disabled = false;
        cartButton.innerHTML = '<span id="totalPrice">' + document.getElementById('totalPrice').textContent + '</span>';
    });
}
