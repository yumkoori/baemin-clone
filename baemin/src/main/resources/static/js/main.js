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

// 음식점 슬라이더
let currentRestaurantSlideIndex = 0;
const restaurantSlides = document.querySelectorAll('.restaurant-slide');
const restaurantIndicators = document.querySelectorAll('.restaurant-indicator');
const totalRestaurantSlides = restaurantSlides.length;

function showRestaurantSlide(index) {
    const restaurantSliderWrapper = document.querySelector('.restaurant-slider-wrapper');
    restaurantSliderWrapper.style.transform = `translateX(-${index * 100}%)`;
    
    // 인디케이터 업데이트
    restaurantIndicators.forEach(indicator => indicator.classList.remove('active'));
    restaurantIndicators[index].classList.add('active');
    
    currentRestaurantSlideIndex = index;
}

function changeRestaurantSlide(direction) {
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