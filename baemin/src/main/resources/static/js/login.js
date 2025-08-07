// DOM이 로드된 후 실행
document.addEventListener('DOMContentLoaded', function() {
    
    // 타임리프에서 전달된 설정 정보 확인
    const config = window.LOGIN_CONFIG || {};
    const messages = config.messages || {};
    
    // 요소들 선택
    const kakaoBtn = document.querySelector('.kakao-btn');
    const naverBtn = document.querySelector('.naver-btn');
    const appleBtn = document.querySelector('.apple-btn');
    const phoneBtn = document.querySelector('.phone-btn');
    const emailBtn = document.querySelector('.email-btn');
    const footerLinks = document.querySelectorAll('.footer-link');

    // 카카오 로그인 버튼 클릭 이벤트
    kakaoBtn.addEventListener('click', function() {
        showLoginProcess(messages.kakaoLogin || '카카오 로그인 중...');
        
        // 타임리프에서 전달된 카카오 OAuth URL 사용 또는 기본값
        const kakaoAuthUrl = config.kakao?.authUrl || 
            `https://kauth.kakao.com/oauth/authorize?client_id=${config.kakao?.clientId || '9332367d804b05aa4921d0ddd1c788cb'}&redirect_uri=${encodeURIComponent(config.kakao?.redirectUri || 'http://localhost:8080/api/oauth')}&response_type=code`;
        
        // CORS 문제로 인해 fetch 대신 브라우저 직접 리다이렉트 사용
        // OAuth 표준 플로우: 브라우저 → 카카오 → 백엔드(callback) → JWT 발급
        window.location.href = kakaoAuthUrl;
    });

    // 네이버 로그인 버튼 클릭 이벤트
    naverBtn.addEventListener('click', function() {
        showLoginProcess(messages.naverLogin || '네이버 로그인 중...');
        
        // 타임리프에서 전달된 네이버 OAuth URL 사용
        if (config.naver?.authUrl) {
            window.location.href = config.naver.authUrl;
        } else {
            showNotification('네이버 로그인 설정이 필요합니다.', 'warning');
            setTimeout(() => hideLoadingOverlay(), 1000);
        }
    });

    // Apple 로그인 버튼 클릭 이벤트
    appleBtn.addEventListener('click', function() {
        showLoginProcess(messages.appleLogin || 'Apple 로그인 중...');
        
        // 타임리프에서 전달된 Apple OAuth URL 사용
        if (config.apple?.authUrl) {
            window.location.href = config.apple.authUrl;
        } else {
            showNotification('Apple 로그인 설정이 필요합니다.', 'warning');
            setTimeout(() => hideLoadingOverlay(), 1000);
        }
    });

    // 휴대폰 로그인 버튼 클릭 이벤트 (타임리프 onclick으로 처리되지만 추가 로직)
    phoneBtn.addEventListener('click', function(e) {
        // 타임리프 onclick이 먼저 실행되므로 여기서는 로딩만 표시
        showLoginProcess(messages.phoneLogin || '휴대폰 로그인 페이지로 이동합니다.');
        
        setTimeout(() => {
            showNotification(messages.phoneLogin || '휴대폰 로그인 페이지로 이동합니다.', 'success');
            hideLoadingOverlay();
        }, 500);
    });

    // 이메일 로그인 버튼 클릭 이벤트 (타임리프 onclick으로 처리되지만 추가 로직)
    emailBtn.addEventListener('click', function(e) {
        // 타임리프 onclick이 먼저 실행되므로 여기서는 로딩만 표시
        showLoginProcess(messages.emailLogin || '이메일 로그인 페이지로 이동합니다.');
        
        setTimeout(() => {
            showNotification(messages.emailLogin || '이메일 로그인 페이지로 이동합니다.', 'success');
            hideLoadingOverlay();
        }, 500);
    });

    // 하단 링크들 이벤트 처리 (타임리프로 href가 설정되어 있지만 추가 기능)
    footerLinks.forEach(link => {
        link.addEventListener('click', function(e) {
            const linkText = this.textContent.trim();
            
            // 로딩 효과와 알림 표시
            if (linkText.includes('회원가입')) {
                showNotification(messages.signupRedirect || '회원가입 페이지로 이동합니다.', 'info');
            } else if (linkText.includes('아이디')) {
                showNotification(messages.findIdRedirect || '아이디 찾기 페이지로 이동합니다.', 'info');
            } else if (linkText.includes('비밀번호')) {
                showNotification(messages.findPasswordRedirect || '비밀번호 찾기 페이지로 이동합니다.', 'info');
            }
        });
    });

    // 로그인 프로세스 표시 함수
    function showLoginProcess(message) {
        // 버튼 비활성화 및 로딩 표시
        const allButtons = document.querySelectorAll('.social-btn, .other-btn');
        allButtons.forEach(btn => {
            btn.disabled = true;
            btn.style.opacity = '0.6';
            btn.style.cursor = 'not-allowed';
        });

        // 로딩 메시지 표시
        showLoadingOverlay(message);
    }

    // 로딩 오버레이 표시
    function showLoadingOverlay(message) {
        // 기존 로딩 오버레이가 있다면 제거
        const existingOverlay = document.querySelector('.loading-overlay');
        if (existingOverlay) {
            existingOverlay.remove();
        }

        // 로딩 오버레이 생성
        const overlay = document.createElement('div');
        overlay.className = 'loading-overlay';
        overlay.innerHTML = `
            <div class="loading-content">
                <div class="loading-spinner"></div>
                <p class="loading-text">${message}</p>
                <p class="loading-subtext">${messages.waitMessage || '잠시만 기다려주세요'}</p>
            </div>
        `;
        
        document.body.appendChild(overlay);
        
        // 스타일 적용
        const style = document.createElement('style');
        style.textContent = `
            .loading-overlay {
                position: fixed;
                top: 0;
                left: 0;
                width: 100%;
                height: 100%;
                background: rgba(0, 0, 0, 0.8);
                display: flex;
                justify-content: center;
                align-items: center;
                z-index: 10000;
                backdrop-filter: blur(5px);
            }
            .loading-content {
                text-align: center;
                color: white;
                padding: 40px;
                border-radius: 16px;
                background: rgba(255, 255, 255, 0.1);
                border: 1px solid rgba(255, 255, 255, 0.2);
            }
            .loading-spinner {
                width: 50px;
                height: 50px;
                border: 4px solid rgba(255, 255, 255, 0.3);
                border-top: 4px solid #00d4aa;
                border-radius: 50%;
                animation: spin 1s linear infinite;
                margin: 0 auto 20px;
            }
            .loading-text {
                font-size: 18px;
                font-weight: 600;
                margin-bottom: 8px;
            }
            .loading-subtext {
                font-size: 14px;
                opacity: 0.8;
            }
            @keyframes spin {
                0% { transform: rotate(0deg); }
                100% { transform: rotate(360deg); }
            }
        `;
        document.head.appendChild(style);
    }

    // 로딩 오버레이 숨기기
    function hideLoadingOverlay() {
        const overlay = document.querySelector('.loading-overlay');
        if (overlay) {
            overlay.style.opacity = '0';
            overlay.style.transition = 'opacity 0.3s ease';
            setTimeout(() => {
                overlay.remove();
                // 버튼 재활성화
                const allButtons = document.querySelectorAll('.social-btn, .other-btn');
                allButtons.forEach(btn => {
                    btn.disabled = false;
                    btn.style.opacity = '1';
                    btn.style.cursor = 'pointer';
                });
            }, 300);
        }
    }

    // 알림 메시지 표시
    function showNotification(message, type = 'info') {
        // 기존 알림이 있다면 제거
        const existingNotification = document.querySelector('.notification');
        if (existingNotification) {
            existingNotification.remove();
        }

        const notification = document.createElement('div');
        notification.className = `notification notification-${type}`;
        notification.textContent = message;
        
        // 스타일 적용
        const colors = {
            info: { bg: '#3498db', border: '#2980b9' },
            success: { bg: '#2ecc71', border: '#27ae60' },
            warning: { bg: '#f39c12', border: '#e67e22' },
            error: { bg: '#e74c3c', border: '#c0392b' }
        };
        
        notification.style.cssText = `
            position: fixed;
            top: 20px;
            right: 20px;
            background: ${colors[type].bg};
            color: white;
            padding: 16px 24px;
            border-radius: 8px;
            border-left: 4px solid ${colors[type].border};
            box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
            z-index: 10001;
            font-size: 14px;
            font-weight: 500;
            max-width: 300px;
            transform: translateX(100%);
            transition: transform 0.3s ease;
        `;
        
        document.body.appendChild(notification);
        
        // 애니메이션으로 표시
        setTimeout(() => {
            notification.style.transform = 'translateX(0)';
        }, 100);
        
        // 3초 후 제거
        setTimeout(() => {
            notification.style.transform = 'translateX(100%)';
            setTimeout(() => {
                notification.remove();
            }, 300);
        }, 3000);
    }

    // 에러/성공 메시지 자동 숨김 처리
    const errorMessage = document.querySelector('.error-message');
    const successMessage = document.querySelector('.success-message');
    
    if (errorMessage) {
        setTimeout(() => {
            errorMessage.style.opacity = '0';
            setTimeout(() => errorMessage.remove(), 300);
        }, 5000);
    }
    
    if (successMessage) {
        setTimeout(() => {
            successMessage.style.opacity = '0';
            setTimeout(() => successMessage.remove(), 300);
        }, 3000);
    }

    // 버튼 호버 효과 개선
    const interactiveButtons = document.querySelectorAll('.social-btn, .other-btn');
    interactiveButtons.forEach(button => {
        button.addEventListener('mouseenter', function() {
            if (!this.disabled) {
                this.style.transform = 'translateY(-2px)';
            }
        });
        
        button.addEventListener('mouseleave', function() {
            if (!this.disabled) {
                this.style.transform = 'translateY(0)';
            }
        });
    });

    // 키보드 접근성 개선
    document.addEventListener('keydown', function(e) {
        // Enter 키로 포커스된 버튼 클릭
        if (e.key === 'Enter' && 
            (document.activeElement.classList.contains('social-btn') || 
             document.activeElement.classList.contains('other-btn'))) {
            document.activeElement.click();
        }
        
        // ESC 키로 로딩 상태 취소
        if (e.key === 'Escape') {
            hideLoadingOverlay();
        }
    });

    // 페이지 로드 애니메이션
    function addPageLoadAnimation() {
        const brandSection = document.querySelector('.brand-section');
        const loginSection = document.querySelector('.login-section');
        
        brandSection.style.opacity = '0';
        brandSection.style.transform = 'translateX(-50px)';
        loginSection.style.opacity = '0';
        loginSection.style.transform = 'translateX(50px)';
        
        setTimeout(() => {
            brandSection.style.transition = 'all 0.8s ease';
            loginSection.style.transition = 'all 0.8s ease';
            brandSection.style.opacity = '1';
            brandSection.style.transform = 'translateX(0)';
            loginSection.style.opacity = '1';
            loginSection.style.transform = 'translateX(0)';
        }, 200);
    }

    // 페이지 로드 애니메이션 실행
    addPageLoadAnimation();

    // JWT 토큰을 헤더에 담아서 main 페이지로 이동하는 함수
    function redirectToMainWithToken(jwtToken) {
        console.log('redirectToMainWithToken 호출됨, 토큰:', jwtToken ? '있음' : '없음');
        
        // 방법 1: 컨트롤러를 통해 main 페이지 접근
        fetch('/api/main', {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${jwtToken}`,
                'Content-Type': 'text/html',
                'Accept': 'text/html'
            }
        })
        .then(response => {
            console.log('메인 페이지 응답:', response.status, response.statusText);
            if (response.ok) {
                return response.text();
            }
            throw new Error(`메인 페이지 로드 실패: ${response.status}`);
        })
        .then(html => {
            console.log('HTML 받음, 페이지 교체 시작');
            // HTML을 받아서 현재 페이지를 교체
            document.open();
            document.write(html);
            document.close();
            
            // URL 변경
            window.history.pushState({}, '배달의민족 - 메인', '/api/main');
            
            hideLoadingOverlay();
        })
        .catch(error => {
            console.error('메인 페이지 이동 오류:', error);
            
            // 방법 2: 직접 리다이렉트 (fallback)
            console.log('fallback: 직접 리다이렉트 시도');
            window.location.href = '/api/main';
        });
    }

    // 토큰 관리 유틸리티 함수들
    function setAuthToken(token) {
        localStorage.setItem('authToken', token);
        sessionStorage.setItem('authToken', token);
        
        // 토큰을 모든 후속 요청의 기본 헤더에 설정
        if (window.fetch) {
            const originalFetch = window.fetch;
            window.fetch = function(...args) {
                if (args[1]) {
                    args[1].headers = {
                        ...args[1].headers,
                        'Authorization': `Bearer ${token}`
                    };
                } else {
                    args[1] = {
                        headers: {
                            'Authorization': `Bearer ${token}`
                        }
                    };
                }
                return originalFetch.apply(this, args);
            };
        }
    }

    function getAuthToken() {
        return localStorage.getItem('authToken') || sessionStorage.getItem('authToken');
    }

    function removeAuthToken() {
        localStorage.removeItem('authToken');
        sessionStorage.removeItem('authToken');
    }

    // 페이지 로드 시 기존 토큰 확인
    function checkExistingAuth() {
        const existingToken = getAuthToken();
        if (existingToken) {
            // 토큰이 있으면 유효성 검사 후 메인으로 리다이렉트
            validateTokenAndRedirect(existingToken);
        }
    }

    function validateTokenAndRedirect(token) {
        fetch('/api/validate-token', {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            }
        })
        .then(response => response.json())
        .then(result => {
            if (result.resultCode === 200) {
                showNotification('이미 로그인되어 있습니다. 메인 페이지로 이동합니다.', 'info');
                setTimeout(() => {
                    redirectToMainWithToken(token);
                }, 1000);
            } else {
                // 토큰이 유효하지 않으면 제거
                removeAuthToken();
            }
        })
        .catch(error => {
            console.error('토큰 검증 오류:', error);
            removeAuthToken();
        });
    }

    // URL 파라미터에서 JWT 토큰 확인 (OAuth callback 처리)
    function checkTokenFromUrl() {
        const urlParams = new URLSearchParams(window.location.search);
        const token = urlParams.get('token');
        const error = urlParams.get('error');
        
        console.log('URL 파라미터 확인:', { token: token ? 'exists' : 'null', error });
        
        if (error) {
            // OAuth 에러 처리
            console.error('OAuth 에러:', error);
            showNotification(`로그인 실패: ${decodeURIComponent(error)}`, 'error');
            // URL에서 에러 파라미터 제거
            window.history.replaceState({}, document.title, window.location.pathname);
            return;
        }
        
        if (token) {
            console.log('토큰 발견, 저장 및 메인 페이지 이동 준비');
            // OAuth 성공 - JWT 토큰 저장
            setAuthToken(token);
            showNotification('로그인 성공! 메인 페이지로 이동합니다.', 'success');
            
            // URL에서 토큰 파라미터 제거
            window.history.replaceState({}, document.title, window.location.pathname);
            
            // 메인 페이지로 이동
            setTimeout(() => {
                console.log('메인 페이지로 이동 시작');
                redirectToMainWithToken(token);
            }, 1500);
        }
    }

    // 페이지 로드 시 URL 파라미터 및 기존 인증 상태 확인
    checkTokenFromUrl();
    checkExistingAuth();

    // 페이지 언로드 시 정리
    window.addEventListener('beforeunload', function() {
        hideLoadingOverlay();
        // 알림 제거
        const notifications = document.querySelectorAll('.notification');
        notifications.forEach(notification => notification.remove());
    });

    // 창 크기 변경 시 레이아웃 조정
    window.addEventListener('resize', function() {
        // 모바일에서 데스크톱으로 전환 시 레이아웃 재조정
        const container = document.querySelector('.login-container');
        if (window.innerWidth > 768) {
            container.style.flexDirection = 'row';
        } else {
            container.style.flexDirection = 'column';
        }
    });
});

// 전역 함수들 (JWT 토큰 기반 시스템)
window.BaeminLogin = {
    // 외부에서 로그인 상태 확인
    checkLoginStatus: function() {
        const token = localStorage.getItem('authToken') || sessionStorage.getItem('authToken');
        return !!token;
    },
    
    // 현재 JWT 토큰 반환
    getToken: function() {
        return localStorage.getItem('authToken') || sessionStorage.getItem('authToken');
    },
    
    // 로그아웃 함수
    logout: function() {
        if (confirm('로그아웃 하시겠습니까?')) {
            // 토큰 제거
            localStorage.removeItem('authToken');
            sessionStorage.removeItem('authToken');
            
            // 서버에 로그아웃 요청
            fetch('/api/logout', {
                method: 'POST',
                headers: {
                    'Authorization': `Bearer ${this.getToken()}`,
                    'Content-Type': 'application/json'
                }
            })
            .then(() => {
                // 로그인 페이지로 리다이렉트
                window.location.href = '/html/login.html';
            })
            .catch(() => {
                // 에러가 발생해도 로그인 페이지로 이동
                window.location.href = '/html/login.html';
            });
        }
    },
    
    // 소셜 로그인 함수들 (리다이렉트 기반)
    loginWithKakao: function() {
        const config = window.LOGIN_CONFIG || {};
        const kakaoAuthUrl = config.kakao?.authUrl || 
            `https://kauth.kakao.com/oauth/authorize?client_id=${config.kakao?.clientId || '9332367d804b05aa4921d0ddd1c788cb'}&redirect_uri=${encodeURIComponent(config.kakao?.redirectUri || 'http://localhost:8080/api/oauth')}&response_type=code`;
        
        // 브라우저 리다이렉트 (CORS 문제 해결)
        window.location.href = kakaoAuthUrl;
    },
    
    // API 요청 헬퍼 함수
    apiRequest: function(url, options = {}) {
        const token = this.getToken();
        const headers = {
            'Content-Type': 'application/json',
            ...options.headers
        };
        
        if (token) {
            headers['Authorization'] = `Bearer ${token}`;
        }
        
        return fetch(url, {
            ...options,
            headers
        });
    },
    
    // 토큰 유효성 검사
    validateToken: async function() {
        const token = this.getToken();
        if (!token) return false;
        
        try {
            const response = await this.apiRequest('/api/validate-token', {
                method: 'POST'
            });
            const result = await response.json();
            return result.resultCode === 200;
        } catch (error) {
            console.error('토큰 검증 오류:', error);
            return false;
        }
    }
};
