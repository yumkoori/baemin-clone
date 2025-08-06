// ======== List 페이지 JavaScript 기능 ========

/**
 * 광고 슬라이더 관련 변수
 */
let currentSlide = 0;
const totalSlides = 3;
let slideInterval;

/**
 * DOM이 로드되면 이벤트 리스너를 등록하는 초기화 함수
 */
document.addEventListener('DOMContentLoaded', function() {
    console.log('List 페이지 JavaScript 로드됨');
    
    // 필터 버튼 이벤트 처리
    initFilterButtons();
    
    // 광고 슬라이더 초기화
    initAdSlider();
    
    // 카테고리 활성화 표시
    updateActiveCategory();
});

/**
 * 필터 버튼 초기화
 */
function initFilterButtons() {
    const filterBtns = document.querySelectorAll('.filter-btn');
    filterBtns.forEach(btn => {
        btn.addEventListener('click', function() {
            // 모든 버튼에서 active 클래스 제거
            filterBtns.forEach(b => b.classList.remove('active'));
            // 클릭된 버튼에 active 클래스 추가
            this.classList.add('active');
            
            // 필터 타입에 따른 처리
            const filterType = this.getAttribute('data-filter');
            console.log('필터 변경:', filterType);
            
            // 여기에 실제 필터링 로직 추가 가능
        });
    });
}

/**
 * 광고 슬라이더 초기화
 */
function initAdSlider() {
    console.log('광고 슬라이더 초기화 시작');
    
    // 모든 슬라이드를 숨기고 첫 번째만 표시
    const slides = document.querySelectorAll('.ad-slide');
    const dots = document.querySelectorAll('.pagination-dot');
    
    console.log('슬라이드 개수:', slides.length);
    console.log('페이지네이션 점 개수:', dots.length);
    
    // 모든 슬라이드 숨기기
    slides.forEach((slide, index) => {
        slide.style.display = 'none';
        slide.classList.remove('active');
        console.log(`슬라이드 ${index} 숨김`);
    });
    
    // 모든 페이지네이션 점 비활성화
    dots.forEach((dot, index) => {
        dot.classList.remove('active');
        console.log(`페이지네이션 점 ${index} 비활성화`);
    });
    
    // 첫 번째 슬라이드와 페이지네이션 점 활성화
    if (slides[0]) {
        slides[0].style.display = 'grid';
        slides[0].classList.add('active');
        console.log('첫 번째 슬라이드 활성화');
    }
    
    if (dots[0]) {
        dots[0].classList.add('active');
        console.log('첫 번째 페이지네이션 점 활성화');
    }
    
    // 페이지네이션 점 클릭 이벤트
    dots.forEach((dot, index) => {
        dot.addEventListener('click', () => {
            console.log(`페이지네이션 점 ${index} 클릭됨`);
            goToSlide(index);
        });
    });
    
    // 자동 슬라이드 시작
    startAutoSlide();
}

/**
 * 특정 슬라이드로 이동
 */
function goToSlide(slideIndex) {
    console.log(`슬라이드 ${slideIndex}로 이동`);
    
    // 현재 활성 슬라이드 숨기기
    const currentSlideElement = document.querySelector('.ad-slide.active');
    if (currentSlideElement) {
        currentSlideElement.style.display = 'none';
        currentSlideElement.classList.remove('active');
        console.log('현재 슬라이드 숨김');
    }
    
    // 현재 활성 페이지네이션 점 비활성화
    const currentDot = document.querySelector('.pagination-dot.active');
    if (currentDot) {
        currentDot.classList.remove('active');
        console.log('현재 페이지네이션 점 비활성화');
    }
    
    // 새 슬라이드 활성화
    const slides = document.querySelectorAll('.ad-slide');
    if (slides[slideIndex]) {
        slides[slideIndex].style.display = 'grid';
        slides[slideIndex].classList.add('active');
        console.log(`슬라이드 ${slideIndex} 활성화`);
    }
    
    // 새 페이지네이션 점 활성화
    const dots = document.querySelectorAll('.pagination-dot');
    if (dots[slideIndex]) {
        dots[slideIndex].classList.add('active');
        console.log(`페이지네이션 점 ${slideIndex} 활성화`);
    }
    
    currentSlide = slideIndex;
    
    // 자동 슬라이드 재시작
    restartAutoSlide();
}

/**
 * 다음 슬라이드로 이동
 */
function nextSlide() {
    console.log('다음 슬라이드로 이동');
    const nextIndex = (currentSlide + 1) % totalSlides;
    goToSlide(nextIndex);
    // 자동 슬라이드 재시작
    restartAutoSlide();
}

/**
 * 이전 슬라이드로 이동
 */
function prevSlide() {
    console.log('이전 슬라이드로 이동');
    const prevIndex = (currentSlide - 1 + totalSlides) % totalSlides;
    goToSlide(prevIndex);
    // 자동 슬라이드 재시작
    restartAutoSlide();
}

/**
 * 자동 슬라이드 시작
 */
function startAutoSlide() {
    console.log('자동 슬라이드 시작');
    slideInterval = setInterval(() => {
        nextSlide();
    }, 5000);
}

/**
 * 자동 슬라이드 재시작
 */
function restartAutoSlide() {
    console.log('자동 슬라이드 재시작');
    if (slideInterval) {
        clearInterval(slideInterval);
    }
    startAutoSlide();
}

/**
 * 카테고리 활성화 표시 업데이트
 */
function updateActiveCategory() {
    // URL에서 카테고리 파라미터 확인
    const urlParams = new URLSearchParams(window.location.search);
    const category = urlParams.get('category');
    
    if (category) {
        // 모든 카테고리 카드에서 active 클래스 제거
        const categoryCards = document.querySelectorAll('.category-card');
        categoryCards.forEach(card => {
            card.classList.remove('active');
        });
        
        // 해당 카테고리 카드에 active 클래스 추가
        const targetCard = document.querySelector(`[href="/list?category=${category}"]`);
        if (targetCard) {
            targetCard.classList.add('active');
        }
    }
}