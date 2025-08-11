let quantity = 1;
let basePrice = 0;
let selectedOptions = new Map(); // optionId -> Set of optionValueIds
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
    
    // 옵션 값 이벤트 리스너 등록
    setupOptionValueListeners();
    updateTotalPrice();
});

// 옵션 값 이벤트 리스너 설정
function setupOptionValueListeners() {
    // 체크박스와 라디오 버튼에 이벤트 리스너 등록
    const optionValueCheckboxes = document.querySelectorAll('.option-value-checkbox');
    const optionValueRadios = document.querySelectorAll('.option-value-radio');
    
    optionValueCheckboxes.forEach(checkbox => {
        checkbox.addEventListener('change', handleOptionValueChange);
    });
    
    optionValueRadios.forEach(radio => {
        radio.addEventListener('change', handleOptionValueChange);
    });
}

// 옵션 값 변경 처리
function handleOptionValueChange(event) {
    const input = event.target;
    const optionId = input.getAttribute('data-option-id');
    const valueId = input.getAttribute('data-value-id');
    
    if (!selectedOptions.has(optionId)) {
        selectedOptions.set(optionId, new Set());
    }
    
    if (input.type === 'checkbox') {
        // 체크박스: 다중 선택 가능
        if (input.checked) {
            selectedOptions.get(optionId).add(valueId);
        } else {
            selectedOptions.get(optionId).delete(valueId);
        }
    } else if (input.type === 'radio') {
        // 라디오: 단일 선택만 가능
        selectedOptions.get(optionId).clear();
        if (input.checked) {
            selectedOptions.get(optionId).add(valueId);
        }
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
    
    selectedOptions.forEach((valueIds, optionId) => {
        valueIds.forEach(valueId => {
            const input = document.querySelector(`[data-value-id="${valueId}"]`);
            if (input && input.checked) {
                const price = parseInt(input.getAttribute('data-price')) || 0;
                optionPrice += price;
            }
        });
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
    const options = [];
    selectedOptions.forEach((valueIds, optionId) => {
        valueIds.forEach(valueId => {
            const input = document.querySelector(`[data-value-id="${valueId}"]`);
            if (input && input.checked) {
                options.push({
                    optionId: parseInt(optionId),
                    menuOptionValueId: parseInt(valueId)
                });
            }
        });
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
        // 전송된 JSON 데이터를 문자열로 변환
        const jsonString = JSON.stringify(cartData, null, 2);
        
        if (data.success) {
            alert(data.message + '\n\n전송된 데이터:\n' + jsonString);
            // 가게 상세 페이지로 이동
            location.href = '/api/store/' + storeId;
        } else {
            alert('장바구니 담기에 실패했습니다: ' + data.message + '\n\n전송된 데이터:\n' + jsonString);
            // 버튼 상태 복원
            cartButton.disabled = false;
            cartButton.innerHTML = '<span id="totalPrice">' + document.getElementById('totalPrice').textContent + '</span>';
        }
    })
    .catch(error => {
        console.error('Error:', error);
        // 전송된 JSON 데이터를 문자열로 변환
        const jsonString = JSON.stringify(cartData, null, 2);
        alert('장바구니 담기에 실패했습니다.\n\n전송된 데이터:\n' + jsonString);
        // 버튼 상태 복원
        cartButton.disabled = false;
        cartButton.innerHTML = '<span id="totalPrice">' + document.getElementById('totalPrice').textContent + '</span>';
    });
}
