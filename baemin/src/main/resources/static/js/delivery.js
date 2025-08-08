// ======== 배달 페이지 JavaScript 기능 ========

/**
 * 카테고리별 음식 데이터
 * 각 카테고리별로 표시할 음식점 정보를 저장
 * 현재는 기본 데이터로 사용되며, 실제로는 서버에서 데이터를 받아와야 함
 */
const categoryFoodData = {
    'chicken': {
        title: '치킨 맛집',
        items: [
            {
                name: '에퇴사',
                image: '/images/restaurant1.jpg',
                rating: '4.9',
                minOrder: '10,000원',
                deliveryFee: '900원~1,900원',
                distance: '1.8km'
            },
            {
                name: '파이치킨',
                image: '/images/restaurant2.jpg',
                rating: '5.0',
                minOrder: '12,000원',
                deliveryFee: '1,000원~2,000원',
                distance: '2.8km'
            },
            {
                name: '빅맨피자',
                image: '/images/restaurant1.jpg',
                rating: '4.9',
                minOrder: '10,000원',
                deliveryFee: '900원~1,900원',
                distance: '1.8km'
            },
            {
                name: '살러디',
                image: '/images/restaurant2.jpg',
                rating: '4.9',
                minOrder: '7,000원',
                deliveryFee: '900원~1,900원',
                distance: '0.5km'
            }
        ]
    },
    'chinese': {
        title: '중식 맛집',
        items: [
            {
                name: '홍콩반점',
                image: '/images/restaurant1.jpg',
                rating: '4.7',
                minOrder: '15,000원',
                deliveryFee: '1,500원~2,500원',
                distance: '1.2km'
            },
            {
                name: '차이나타운',
                image: '/images/restaurant2.jpg',
                rating: '4.8',
                minOrder: '12,000원',
                deliveryFee: '1,200원~2,000원',
                distance: '2.1km'
            },
            {
                name: '마라향',
                image: '/images/restaurant1.jpg',
                rating: '4.6',
                minOrder: '13,000원',
                deliveryFee: '1,000원~1,800원',
                distance: '1.5km'
            },
            {
                name: '짜장면집',
                image: '/images/restaurant2.jpg',
                rating: '4.5',
                minOrder: '8,000원',
                deliveryFee: '800원~1,500원',
                distance: '0.8km'
            }
        ]
    },
    'pizza': {
        title: '피자 맛집',
        items: [
            {
                name: '도미노피자',
                image: '/images/restaurant1.jpg',
                rating: '4.6',
                minOrder: '20,000원',
                deliveryFee: '2,000원~3,000원',
                distance: '2.3km'
            },
            {
                name: '피자헛',
                image: '/images/restaurant2.jpg',
                rating: '4.4',
                minOrder: '18,000원',
                deliveryFee: '1,800원~2,800원',
                distance: '1.9km'
            },
            {
                name: '파파존스',
                image: '/images/restaurant1.jpg',
                rating: '4.7',
                minOrder: '22,000원',
                deliveryFee: '2,200원~3,200원',
                distance: '2.8km'
            },
            {
                name: '미스터피자',
                image: '/images/restaurant2.jpg',
                rating: '4.3',
                minOrder: '16,000원',
                deliveryFee: '1,600원~2,600원',
                distance: '1.4km'
            }
        ]
    },
    'western': {
        title: '양식 맛집',
        items: [
            {
                name: '빕스',
                image: '/images/restaurant1.jpg',
                rating: '4.2',
                minOrder: '25,000원',
                deliveryFee: '2,500원~3,500원',
                distance: '3.1km'
            },
            {
                name: '아웃백',
                image: '/images/restaurant2.jpg',
                rating: '4.5',
                minOrder: '30,000원',
                deliveryFee: '3,000원~4,000원',
                distance: '2.7km'
            },
            {
                name: '패밀리레스토랑',
                image: '/images/restaurant1.jpg',
                rating: '4.1',
                minOrder: '20,000원',
                deliveryFee: '2,000원~3,000원',
                distance: '1.8km'
            },
            {
                name: '스파게티팩토리',
                image: '/images/restaurant2.jpg',
                rating: '4.4',
                minOrder: '18,000원',
                deliveryFee: '1,800원~2,800원',
                distance: '2.2km'
            }
        ]
    }
};

/**
 * 현재 선택된 카테고리를 저장하는 변수
 * 기본값은 'chicken'으로 설정
 */
let currentCategory = 'chicken';

/**
 * DOM이 로드되면 이벤트 리스너를 등록하는 초기화 함수
 */
document.addEventListener('DOMContentLoaded', function() {
    // 카테고리 카드 클릭 이벤트는 제거됨 (각 카테고리별 페이지로 이동하도록 변경)
    // 이제 카테고리 클릭 시 별도의 페이지로 이동
    
    /**
     * 필터 버튼 클릭 이벤트 처리
     * 음식점 목록을 다양한 기준으로 정렬할 수 있도록 함
     */
    const filterBtns = document.querySelectorAll('.filter-btn');
    filterBtns.forEach(btn => {
        btn.addEventListener('click', function() {
            // 모든 버튼에서 active 클래스 제거
            filterBtns.forEach(b => b.classList.remove('active'));
            // 클릭된 버튼에 active 클래스 추가
            this.classList.add('active');
            
            // 필터에 따른 정렬 실행
            const filterType = this.getAttribute('data-filter');
            sortFoodItems(filterType);
        });
    });
});

/**
 * 선택된 카테고리에 따라 음식점 목록을 업데이트하는 함수
 * @param {string} categoryKey - 카테고리 키 (예: 'chicken', 'chinese' 등)
 * 
 * 현재는 사용되지 않음 (카테고리별 페이지로 분리됨)
 * 향후 각 카테고리 페이지에서 사용할 수 있음
 */
function updateFoodItems(categoryKey) {
    const category = categoryFoodData[categoryKey];
    if (!category) return;
    
    // 페이지 제목 업데이트
    const titleElement = document.getElementById('selected-category-title');
    if (titleElement) {
        titleElement.textContent = category.title;
    }
    
    // 음식점 목록 컨테이너 찾기
    const container = document.getElementById('food-items-container');
    if (!container) return;
    
    // 기존 내용 초기화
    container.innerHTML = '';
    
    // 각 음식점 정보를 HTML로 생성하여 추가
    category.items.forEach(item => {
        const itemHTML = `
            <div class="food-item-card" data-category="${categoryKey}">
                <div class="food-item-image">
                    <img src="${item.image}" alt="${item.name}" />
                    <div class="rating-badge">⭐ ${item.rating}</div>
                </div>
                <div class="food-item-info">
                    <h4>${item.name}</h4>
                    <div class="food-item-details">
                        <span class="min-order">최소주문 ${item.minOrder}</span>
                        <span class="delivery-fee">배달비 ${item.deliveryFee}</span>
                        <span class="distance">${item.distance}</span>
                    </div>
                    <div class="coupon-tag">쿠폰</div>
                </div>
            </div>
        `;
        container.innerHTML += itemHTML;
    });
}

/**
 * 현재 페이지의 음식점 목록을 지정된 기준으로 정렬하는 함수
 * @param {string} filterType - 정렬 기준 ('popular', 'delivery-fee', 'distance', 'rating', 'new')
 * 
 * 정렬 기준:
 * - popular: 찜한 음식 순 (현재는 별점 높은 순으로 대체)
 * - delivery-fee: 배달비 낮은 순
 * - distance: 가까운 순 
 * - rating: 별점 높은 순
 * - new: 신규 매장 순 (기본 순서 유지)
 */
function sortFoodItems(filterType) {
    const category = categoryFoodData[currentCategory];
    if (!category) return;
    
    // 원본 배열을 복사하여 정렬
    let sortedItems = [...category.items];
    
    switch(filterType) {
        case 'popular':
            // 찜한 음식 순 (별점 높은 순으로 대체)
            sortedItems.sort((a, b) => parseFloat(b.rating) - parseFloat(a.rating));
            break;
        case 'delivery-fee':
            // 배달비 낮은 순으로 정렬
            sortedItems.sort((a, b) => {
                const aFee = parseInt(a.deliveryFee.split('원')[0].replace(/,/g, ''));
                const bFee = parseInt(b.deliveryFee.split('원')[0].replace(/,/g, ''));
                return aFee - bFee;
            });
            break;
        case 'distance':
            // 거리 가까운 순으로 정렬
            sortedItems.sort((a, b) => {
                const aDistance = parseFloat(a.distance.replace('km', ''));
                const bDistance = parseFloat(b.distance.replace('km', ''));
                return aDistance - bDistance;
            });
            break;
        case 'rating':
            // 별점 높은 순으로 정렬
            sortedItems.sort((a, b) => parseFloat(b.rating) - parseFloat(a.rating));
            break;
        case 'new':
            // 신규 매장 순 (기본 순서 유지)
            break;
    }
    
    // 정렬된 결과로 화면 업데이트
    const container = document.getElementById('food-items-container');
    if (!container) return;
    
    // 기존 내용 초기화
    container.innerHTML = '';
    
    // 정렬된 음식점 목록을 HTML로 생성하여 추가
    sortedItems.forEach(item => {
        const itemHTML = `
            <div class="food-item-card" data-category="${currentCategory}">
                <div class="food-item-image">
                    <img src="${item.image}" alt="${item.name}" />
                    <div class="rating-badge">⭐ ${item.rating}</div>
                </div>
                <div class="food-item-info">
                    <h4>${item.name}</h4>
                    <div class="food-item-details">
                        <span class="min-order">최소주문 ${item.minOrder}</span>
                        <span class="delivery-fee">배달비 ${item.deliveryFee}</span>
                        <span class="distance">${item.distance}</span>
                    </div>
                    <div class="coupon-tag">쿠폰</div>
                </div>
            </div>
        `;
        container.innerHTML += itemHTML;
    });
}