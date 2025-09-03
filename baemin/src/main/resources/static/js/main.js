// 메인 배너 슬라이더
let currentSlideIndex = 0;
const slides = document.querySelectorAll('.slide');
const indicators = document.querySelectorAll('.indicator');
const totalSlides = slides.length;

function showSlide(index) {
    const sliderWrapper = document.querySelector('.slider-wrapper');
    sliderWrapper.style.transform = `translateX(-${index * 100}%)`;
    
    // 인디케이터 업데이트
    indicators.forEach(indicator => indicator.classList.remove('active'));
    indicators[index].classList.add('active');
    
    currentSlideIndex = index;
}

function changeSlide(direction) {
    currentSlideIndex += direction;
    
    if (currentSlideIndex >= totalSlides) {
        currentSlideIndex = 0;
    } else if (currentSlideIndex < 0) {
        currentSlideIndex = totalSlides - 1;
    }
    
    showSlide(currentSlideIndex);
}

function currentSlide(index) {
    showSlide(index - 1);
}

// 음식점 슬라이더 전역 변수
let currentRestaurantSlideIndex = 0;
let restaurantSlides = [];
let restaurantIndicators = [];
let totalRestaurantSlides = 0;

function updateRestaurantSliderElements() {
    restaurantSlides = document.querySelectorAll('.restaurant-slide');
    restaurantIndicators = document.querySelectorAll('.restaurant-indicator');
    totalRestaurantSlides = restaurantSlides.length;
}

function showRestaurantSlide(index) {
    if (totalRestaurantSlides === 0) return;
    
    const restaurantSliderWrapper = document.querySelector('.restaurant-slider-wrapper');
    if (!restaurantSliderWrapper) return;
    
    restaurantSliderWrapper.style.transform = `translateX(-${index * 100}%)`;
    
    // 인디케이터 업데이트
    restaurantIndicators.forEach(indicator => indicator.classList.remove('active'));
    if (restaurantIndicators[index]) {
        restaurantIndicators[index].classList.add('active');
    }
    
    currentRestaurantSlideIndex = index;
}

function changeRestaurantSlide(direction) {
    if (totalRestaurantSlides === 0) return;
    
    currentRestaurantSlideIndex += direction;
    
    if (currentRestaurantSlideIndex >= totalRestaurantSlides) {
        currentRestaurantSlideIndex = 0;
    } else if (currentRestaurantSlideIndex < 0) {
        currentRestaurantSlideIndex = totalRestaurantSlides - 1;
    }
    
    showRestaurantSlide(currentRestaurantSlideIndex);
}

function currentRestaurantSlide(index) {
    showRestaurantSlide(index - 1);
}

// 자동 슬라이드 (5초마다) - 메인 배너만
setInterval(() => {
    changeSlide(1);
}, 5000);

// 키보드 네비게이션
document.addEventListener('keydown', (e) => {
    if (e.key === 'ArrowLeft') {
        changeSlide(-1);
    } else if (e.key === 'ArrowRight') {
        changeSlide(1);
    }
}); 

// 할인 슬라이더
let currentDiscountSlideIndex = 0;
const discountSlides = document.querySelectorAll('.discount-slide');
const discountIndicators = document.querySelectorAll('.discount-indicator');
const totalDiscountSlides = discountSlides.length;

function showDiscountSlide(index) {
    const discountSliderWrapper = document.querySelector('.discount-slider-wrapper');
    if (discountSliderWrapper) {
        discountSliderWrapper.style.transform = `translateX(-${index * 100}%)`;
        
        // 인디케이터 업데이트
        discountIndicators.forEach(indicator => indicator.classList.remove('active'));
        if (discountIndicators[index]) {
            discountIndicators[index].classList.add('active');
        }
        
        currentDiscountSlideIndex = index;
    }
}

function changeDiscountSlide(direction) {
    currentDiscountSlideIndex += direction;
    
    if (currentDiscountSlideIndex >= totalDiscountSlides) {
        currentDiscountSlideIndex = 0;
    } else if (currentDiscountSlideIndex < 0) {
        currentDiscountSlideIndex = totalDiscountSlides - 1;
    }
    
    showDiscountSlide(currentDiscountSlideIndex);
}

function currentDiscountSlide(index) {
    showDiscountSlide(index - 1);
}

    function logoutWithService() {
        if (confirm('카카오계정과 함께 로그아웃 하시겠습니까?')) {
            const baseOrigin = window.location.origin;
            const logoutRedirect = baseOrigin + '/api/logout';
            const kakaoLogoutUrl = "https://kauth.kakao.com/oauth/logout" +
                "?client_id=9332367d804b05aa4921d0ddd1c788cb" +
                `&logout_redirect_uri=${encodeURIComponent(logoutRedirect)}`;
            window.location.href = kakaoLogoutUrl;
        }
    }

// ===== 최근 주문 데이터 Ajax 로딩 기능 =====

/**
 * 최근 주문 데이터를 서버에서 가져와서 슬라이더를 동적으로 생성
 */
function loadRecentOrders() {
    const loadingElement = document.getElementById('recent-orders-loading');
    const errorElement = document.getElementById('recent-orders-error');
    const sliderWrapper = document.getElementById('restaurant-slider-wrapper');
    const indicatorsContainer = document.getElementById('restaurant-slider-indicators');
    
    // 로딩 상태 표시
    loadingElement.style.display = 'block';
    errorElement.style.display = 'none';
    sliderWrapper.innerHTML = '';
    indicatorsContainer.innerHTML = '';
    
    // 슬라이더 버튼 숨기기
    document.querySelector('.restaurant-prev-btn').style.display = 'none';
    document.querySelector('.restaurant-next-btn').style.display = 'none';
    
    // Ajax 요청
    fetch('/api/recent-orders', {
        method: 'GET',
        credentials: 'same-origin', // 세션 쿠키 포함
        headers: {
            'Content-Type': 'application/json'
        }
    })
    .then(response => {
        if (!response.ok) {
            throw new Error('Network response was not ok');
        }
        return response.json();
    })
    .then(data => {
        loadingElement.style.display = 'none';
        
        if (data.success && data.data && data.data.length > 0) {
            createRestaurantSlides(data.data);
        } else {
            showNoDataMessage();
        }
    })
    .catch(error => {
        console.error('최근 주문 데이터 로딩 실패:', error);
        loadingElement.style.display = 'none';
        errorElement.style.display = 'block';
    });
}

/**
 * 레스토랑 데이터로 슬라이드 생성
 */
function createRestaurantSlides(restaurants) {
    const sliderWrapper = document.getElementById('restaurant-slider-wrapper');
    const indicatorsContainer = document.getElementById('restaurant-slider-indicators');
    
    // 슬라이드당 2개의 카드를 표시
    const cardsPerSlide = 2;
    const totalSlides = Math.ceil(restaurants.length / cardsPerSlide);
    
    // 슬라이드 생성
    for (let slideIndex = 0; slideIndex < totalSlides; slideIndex++) {
        const slide = document.createElement('div');
        slide.className = 'restaurant-slide';
        
        // 각 슬라이드에 카드 추가
        const startIndex = slideIndex * cardsPerSlide;
        const endIndex = Math.min(startIndex + cardsPerSlide, restaurants.length);
        
        for (let cardIndex = startIndex; cardIndex < endIndex; cardIndex++) {
            const restaurant = restaurants[cardIndex];
            const card = createRestaurantCard(restaurant);
            slide.appendChild(card);
        }
        
        sliderWrapper.appendChild(slide);
    }
    
    // 인디케이터 생성
    for (let i = 0; i < totalSlides; i++) {
        const indicator = document.createElement('span');
        indicator.className = 'restaurant-indicator';
        if (i === 0) indicator.classList.add('active');
        indicator.onclick = () => currentRestaurantSlide(i + 1);
        indicatorsContainer.appendChild(indicator);
    }
    
    // 슬라이더 요소들 업데이트
    updateRestaurantSliderElements();
    currentRestaurantSlideIndex = 0;
    
    // 슬라이더 버튼 표시 (슬라이드가 2개 이상일 때만)
    if (totalSlides > 1) {
        document.querySelector('.restaurant-prev-btn').style.display = 'block';
        document.querySelector('.restaurant-next-btn').style.display = 'block';
    }
}

/**
 * 개별 레스토랑 카드 생성
 */
function createRestaurantCard(restaurant) {
    const card = document.createElement('div');
    card.className = 'restaurant-card';
    
    card.innerHTML = `
        <div class="restaurant-image">
            <img src="${restaurant.imageUrl}" alt="${restaurant.name}" />
        </div>
        <div class="restaurant-info">
            <h4>${restaurant.name}</h4>
            <div class="rating">
                <span class="stars">${restaurant.rating}</span>
            </div>
            <div class="delivery-info">
                <span class="price">${restaurant.deliveryFee}</span>
                <span class="distance">${restaurant.distance}</span>
            </div>
            ${restaurant.hasCoupon ? '<div class="coupon-badge"><span>배달 쿠폰</span></div>' : ''}
        </div>
    `;
    
    return card;
}

/**
 * 데이터가 없을 때 메시지 표시
 */
function showNoDataMessage() {
    const sliderWrapper = document.getElementById('restaurant-slider-wrapper');
    sliderWrapper.innerHTML = `
        <div style="text-align: center; padding: 50px;">
            <p>아직 주문 내역이 없습니다.</p>
            <p>맛있는 음식을 주문해보세요! 🍽️</p>
        </div>
    `;
}

// 페이지 로드 시 최근 주문 데이터 자동 로딩
document.addEventListener('DOMContentLoaded', function() {
    // 다른 초기화가 완료된 후 최근 주문 데이터 로드
    setTimeout(loadRecentOrders, 100);
});