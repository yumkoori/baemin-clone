// ===== List 페이지 JavaScript =====

document.addEventListener('DOMContentLoaded', () => {
    console.log('List 페이지 JavaScript 로드됨');

    initFilterButtons();
    initAdSlider();
    updateActiveCategory();
    initSearchBox(); // ★ 추가: 검색 Enter 처리
});

/**
 * 필터 버튼 초기화 및 클릭 이벤트 처리
 */
function initFilterButtons() {
    const filterBtns = document.querySelectorAll('.filter-btn');
    const restaurantList = document.querySelector('.restaurant-list');

    filterBtns.forEach(btn => {
        btn.addEventListener('click', () => {
            // 이미 선택된 버튼이면 동작 안 함
            if (btn.classList.contains('active')) return;

            // active 클래스 교체
            filterBtns.forEach(b => b.classList.remove('active'));
            btn.classList.add('active');

            // 선택된 필터 타입으로 데이터 요청
            fetchFilteredRestaurants(btn.dataset.filter);
        });
    });
}

/**
 * 백엔드에서 필터링된 음식점 리스트를 가져와서 업데이트
 */
function fetchFilteredRestaurants(filterType) {
    const restaurantList = document.querySelector('.restaurant-list');
    const category = new URLSearchParams(location.search).get('category') || '';
    let url = `/api/restaurants?filter=${filterType}`;
    if (category) url += `&category=${category}`;

    showLoading(restaurantList);

    fetch(url, {
        headers: {
            'Content-Type': 'application/json',
            'X-Requested-With': 'XMLHttpRequest'
        }
    })
    .then(res => {
        if (!res.ok) throw new Error(res.statusText);
        return res.json();
    })
    .then(data => updateRestaurantList(data, restaurantList))
    .catch(err => {
        console.error('Error fetching filtered restaurants:', err);
        restaurantList.innerHTML = `
            <div class="error-message">
                <p>필터링 중 오류가 발생했습니다.</p>
            </div>
        `;
    });
}

/**
 * 음식점 리스트 DOM 업데이트
 */
function updateRestaurantList(restaurants, container) {
    if (!restaurants.length) {
        container.innerHTML = `
            <div class="no-results">
                <p>조건에 맞는 음식점이 없습니다.</p>
            </div>
        `;
        return;
    }

    container.innerHTML = restaurants.map(item => `
        <div class="restaurant-item">
            <div class="restaurant-images">
                <img src="/images/restaurant2.jpg" alt="${item.storeName} 이미지1" />
                <img src="/images/restaurant2.jpg" alt="${item.storeName} 이미지2" />
                <img src="/images/restaurant2.jpg" alt="${item.storeName} 이미지3" />
            </div>
            <div class="restaurant-details">
                <h4>${item.storeName}</h4>
                <div class="rating">⭐ ${item.rating} (${item.reviewCount})</div>
                <div class="delivery-info">
                    <span>최소주문 ${item.minimumPrice}원</span>
                    <span>배달비 ${item.deliveryFee}원</span>
                    <span>1.2km</span>
                </div>
            </div>
        </div>
    `).join('');
}

/**
 * 로딩 상태 표시
 */
function showLoading(container) {
    container.innerHTML = `
        <div class="loading">
            <div class="loading-spinner"></div>
            <p>음식점 목록을 불러오는 중...</p>
        </div>
    `;
}

/**
 * 광고 슬라이더 초기화
 */
function initAdSlider() {
    console.log('광고 슬라이더 초기화 시작');

    const slides = document.querySelectorAll('.ad-slide');
    const dots = document.querySelectorAll('.pagination-dot');
    const prevBtn = document.querySelector('.prev-btn');
    const nextBtn = document.querySelector('.next-btn');
    let currentSlide = 0;
    const totalSlides = slides.length;
    let slideInterval;

    const showSlide = index => {
        slides.forEach((slide, i) => {
            slide.classList.toggle('active', i === index);
            slide.style.display = i === index ? 'grid' : 'none';
        });
        dots.forEach((dot, i) => dot.classList.toggle('active', i === index));
        currentSlide = index;
    };

    const nextSlide = () => showSlide((currentSlide + 1) % totalSlides);
    const prevSlide = () => showSlide((currentSlide - 1 + totalSlides) % totalSlides);
    const startAutoSlide = () => { slideInterval = setInterval(nextSlide, 5000); };
    const restartAutoSlide = () => {
        clearInterval(slideInterval);
        startAutoSlide();
    };

    if (prevBtn) prevBtn.addEventListener('click', () => { prevSlide(); restartAutoSlide(); });
    if (nextBtn) nextBtn.addEventListener('click', () => { nextSlide(); restartAutoSlide(); });
    dots.forEach((dot, i) => dot.addEventListener('click', () => { showSlide(i); restartAutoSlide(); }));

    showSlide(0);
    startAutoSlide();
}

/**
 * URL의 category 파라미터에 따라 활성화된 카테고리 표시
 */
function updateActiveCategory() {
    const category = new URLSearchParams(location.search).get('category');
    if (!category) return;
    document.querySelectorAll('.category-card').forEach(card => card.classList.remove('active'));
    const target = document.querySelector(`a[href="/list?category=${category}"]`);
    if (target) target.classList.add('active');
}

/**
 * ★ 검색 인풋: Enter로 검색 실행 (추가)
 */
function initSearchBox() {
    const input = document.querySelector('.search-box input');
    const restaurantList = document.querySelector('.restaurant-list');
    if (!input || !restaurantList) return;

    // 한글 조합 중 Enter 무시
    let composing = false;
    input.addEventListener('compositionstart', () => composing = true);
    input.addEventListener('compositionend',   () => composing = false);

    input.addEventListener('keydown', (e) => {
        if (e.key !== 'Enter' || composing) return;
        e.preventDefault();

        const keyword = input.value.trim();
        if (!keyword) return;

        fetchSearchRestaurants(keyword);
    });
}

/**
 * ★ 검색어 기반 음식점 조회 (추가)
 * - 활성 필터/카테고리 파라미터도 함께 전송
 */
function fetchSearchRestaurants(keyword) {
    const restaurantList = document.querySelector('.restaurant-list');
    const category = new URLSearchParams(location.search).get('category') || '';
    const activeFilterBtn = document.querySelector('.filter-btn.active');
    const filterType = activeFilterBtn ? activeFilterBtn.dataset.filter : '';

    // 백엔드 파라미터명에 맞게 조정(현재 keyword/filter/category)
    let url = `/api/restaurants?keyword=${encodeURIComponent(keyword)}`;
    if (filterType) url += `&filter=${encodeURIComponent(filterType)}`;
    if (category)   url += `&category=${encodeURIComponent(category)}`;

    showLoading(restaurantList);

    fetch(url, {
        headers: {
            'Content-Type': 'application/json',
            'X-Requested-With': 'XMLHttpRequest'
        }
    })
    .then(res => {
        if (!res.ok) throw new Error(res.statusText);
        return res.json();
    })
    .then(data => updateRestaurantList(data, restaurantList))
    .catch(err => {
        console.error('Error fetching search restaurants:', err);
        restaurantList.innerHTML = `
            <div class="error-message">
                <p>검색 중 오류가 발생했습니다.</p>
            </div>
        `;
    });
}
