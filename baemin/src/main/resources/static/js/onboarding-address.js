document.addEventListener('DOMContentLoaded', function() {
    // NAVER 지도 인증 실패 훅 (키/도메인 문제 시 호출됨)
    window.navermap_authFailure = function () {
        console.error('Naver Map auth failure - 키 또는 도메인(Referer) 설정을 확인하세요.');
        alert('네이버 지도 인증에 실패했습니다. 키 또는 도메인 설정을 확인해주세요.');
    };
    const $ = (id) => document.getElementById(id);
    const btnLocate = $('btnLocate');
    const btnPick = $('btnPick');
    const btnAddrSearch = $('btnAddrSearch');
    const addrQuery = $('addrQuery');
    const searchResult = $('searchResult');
    const form = $('onboardingAddressForm');
    const roadAddress = $('roadAddress');
    const detailAddress = $('detailAddress');
    const zipCode = $('zipCode');
    const alias = $('alias');
    const isDefault = $('isDefault');
    const mapContainer = document.getElementById('map');

    let map = null;
    let marker = null;
    let selectedLatLng = null; // 사용자가 선택한 좌표(마커 기준)

    // URL 파라미터 처리 (return 경로, 기본 별칭 등)
    const params = new URLSearchParams(window.location.search);
    const returnPath = params.get('return') || '/api/main';
    const presetAlias = params.get('alias');
    if (presetAlias && alias) alias.value = presetAlias;

    function showMap(lat, lng) {
        if (!mapContainer) return;
        const latlng = new naver.maps.LatLng(lat, lng);
        if (!map) {
            map = new naver.maps.Map('map', {
                center: latlng,
                zoom: 16
            });
            marker = new naver.maps.Marker({ position: latlng, map, draggable: true });
            selectedLatLng = latlng;
            naver.maps.Event.addListener(marker, 'dragend', function(e){
                selectedLatLng = marker.getPosition();
            });
            naver.maps.Event.addListener(map, 'click', function(e){
                if (!e.coord) return;
                marker.setPosition(e.coord);
                selectedLatLng = e.coord;
            });
        } else {
            map.setCenter(latlng);
            if (!marker) marker = new naver.maps.Marker({ position: latlng, map, draggable: true });
            else marker.setPosition(latlng);
            selectedLatLng = latlng;
        }
        mapContainer.style.display = 'block';
    }

    // 응답 파싱 헬퍼: reverseGeocode 응답에서 도로명/지번/우편번호 추출
    function parseReverseResponse(response) {
        let road = '';
        let zipcodeVal = '';
        if (response && response.v2) {
            if (Array.isArray(response.v2.addresses) && response.v2.addresses.length > 0) {
                const r = response.v2.addresses[0];
                road = r.roadAddress || r.jibunAddress || '';
                zipcodeVal = r.zipcode || '';
            } else if (Array.isArray(response.v2.results) && response.v2.results.length > 0) {
                const r = response.v2.results[0];
                const region = r.region || {};
                const land = r.land || {};
                const parts = [
                    region.area1 && region.area1.name,
                    region.area2 && region.area2.name,
                    region.area3 && region.area3.name,
                    region.area4 && region.area4.name,
                    land.number1,
                    land.number2 ? '-' + land.number2 : ''
                ].filter(Boolean);
                road = parts.join(' ');
                if (land.addition0 && land.addition0.type === 'zipcode') {
                    zipcodeVal = land.addition0.value || '';
                }
            }
        }
        return { road, zipcodeVal };
    }

    function metersToDegLat(m) { return m / 111320; }
    function metersToDegLng(m, lat) { return m / (111320 * Math.cos(lat * Math.PI / 180) || 1e-6); }

    let geocodeBusy = false;
    async function reverseGeocodeAt(lat, lng) {
        return new Promise((resolve) => {
            const latlng = new naver.maps.LatLng(lat, lng);
            if (geocodeBusy) return resolve(null);
            geocodeBusy = true;
            try {
                naver.maps.Service.reverseGeocode({
                    coords: latlng,
                    coordType: naver.maps.Service.CoordType.LATLNG,
                    orders: [
                        naver.maps.Service.OrderType.ROAD_ADDR,
                        naver.maps.Service.OrderType.ADDR
                    ].join(',')
                }, function(status, response) {
                    if (status === naver.maps.Service.Status.OK) {
                        const { road, zipcodeVal } = parseReverseResponse(response);
                        if (road) {
                            geocodeBusy = false;
                            return resolve({ road, zipcode: zipcodeVal, lat, lng });
                        }
                    }
                    naver.maps.Service.reverseGeocode({
                        coords: latlng,
                        coordType: naver.maps.Service.CoordType.LATLNG,
                        orders: naver.maps.Service.OrderType.ADDR
                    }, function(status2, response2) {
                        geocodeBusy = false;
                        if (status2 === naver.maps.Service.Status.OK) {
                            const { road, zipcodeVal } = parseReverseResponse(response2);
                            if (road) return resolve({ road, zipcode: zipcodeVal, lat, lng });
                        }
                        // 최후 폴백: 백엔드 REST 프록시 호출
                        fetch(`/api/maps/rev-geocode?lat=${lat}&lng=${lng}`, { credentials: 'include' })
                            .then(r => r.json()).then(json => {
                                const body = json?.data;
                                let road2 = '';
                                let zip2 = '';
                                try {
                                    const obj = (typeof body === 'string') ? JSON.parse(body) : body;
                                    const results = obj?.results || [];
                                    if (Array.isArray(results) && results.length > 0) {
                                        // distance가 가장 작은 결과 선택
                                        results.sort((a,b)=> (Number(a.distance||Infinity)) - (Number(b.distance||Infinity)));
                                        const r = results[0];
                                        const region = r.region || {};
                                        const land = r.land || {};
                                        const parts = [
                                            region.area1 && region.area1.name,
                                            region.area2 && region.area2.name,
                                            region.area3 && region.area3.name,
                                            region.area4 && region.area4.name,
                                            land.number1,
                                            land.number2 ? '-' + land.number2 : ''
                                        ].filter(Boolean);
                                        road2 = parts.join(' ');
                                        if (land.addition0 && land.addition0.type === 'zipcode') {
                                            zip2 = land.addition0.value || '';
                                        }
                                    }
                                } catch (e) {}
                                if (road2) return resolve({ road: road2, zipcode: zip2, lat, lng });
                                resolve(null);
                            }).catch(() => resolve(null));
                    });
                });
            } catch (e) {
                geocodeBusy = false;
                resolve(null);
            }
        });
    }

    function delay(ms){ return new Promise(r=>setTimeout(r, ms)); }
    async function findNearestAddress(center) {
        const lat0 = center.lat();
        const lng0 = center.lng();
        const radii = [20, 40, 80, 150]; // meters, 미세 탐색 위주
        const dirs = Array.from({ length: 8 }, (_, i) => {
            const a = i * Math.PI / 8; // 0..2π step 22.5°
            return [Math.cos(a), Math.sin(a)];
        });
        for (let r of radii) {
            const dLat = metersToDegLat(r);
            const dLng = metersToDegLng(r, lat0);
            for (let [dx, dy] of dirs) {
                const lat = lat0 + dLat * dy;
                const lng = lng0 + dLng * dx;
                const res = await reverseGeocodeAt(lat, lng);
                if (res) return res;
                await delay(300); // 호출 간 간격으로 API 안정화
            }
        }
        return null;
    }

    // Reverse geocoding for current center to fill address (근처 주소 탐색 폴백 포함)
    async function reverseGeocodeCenter() {
        if (!map) return;
        const center = selectedLatLng || map.getCenter();
        btnPick && (btnPick.disabled = true);
        try {
            let best = await reverseGeocodeAt(center.lat(), center.lng());
            if (!best) {
                best = await findNearestAddress(center);
            }
            if (best) {
                roadAddress.value = best.road;
                zipCode.value = best.zipcode || '';
                form.dataset.lat = String(best.lat);
                form.dataset.lng = String(best.lng);
                form.style.display = 'block';
                detailAddress.focus();
                // 지도도 스냅한 위치로 미세 이동
                showMap(best.lat, best.lng);
            } else {
                searchResult.style.display = 'block';
                searchResult.innerHTML = '<div class="warn">가까운 주소를 찾지 못했습니다. 지도를 조금 이동해 다시 시도하거나 도로명 주소를 직접 입력해주세요.</div>';
                roadAddress.value = '';
                zipCode.value = '';
                form.dataset.lat = String(center.lat());
                form.dataset.lng = String(center.lng());
                form.style.display = 'block';
                roadAddress.focus();
            }
        } finally {
            btnPick && (btnPick.disabled = false);
        }
    }

    // Dynamic map interactions
    if (mapContainer) {
        // Init map at Seoul City Hall
        showMap(37.5666102, 126.9783881);
    }

    if (btnLocate) {
        btnLocate.addEventListener('click', function() {
            if (navigator.geolocation) {
                navigator.geolocation.getCurrentPosition(pos => {
                    const lat = pos.coords.latitude;
                    const lng = pos.coords.longitude;
                    showMap(lat, lng);
                    // 현재 위치 선택 시 자동으로 주소 스냅
                    setTimeout(reverseGeocodeCenter, 100);
                }, () => alert('현재 위치를 가져오지 못했습니다. 위치 권한을 확인해주세요.'));
            }
        });
    }

    if (btnPick) {
        btnPick.addEventListener('click', function(e){
            e.preventDefault();
            console.log('[onboarding] btnPick clicked');
            reverseGeocodeCenter();
        });
    }
    // 안전망: 동적으로 바뀌는 경우에도 캐치
    document.addEventListener('click', function(e){
        if (e.target && e.target.id === 'btnPick') {
            e.preventDefault();
            console.log('[onboarding] btnPick (delegated) clicked');
            reverseGeocodeCenter();
        }
    });

    // 도로명/지번 텍스트 검색 → 지오코딩으로 지도의 중심 이동 및 자동 채움
    async function searchByText() {
        const q = (addrQuery && addrQuery.value ? addrQuery.value.trim() : '');
        if (!q) return;
        // 버튼 잠금
        if (btnAddrSearch) btnAddrSearch.disabled = true;
        try {
            await new Promise((resolve, reject) => {
                naver.maps.Service.geocode({ query: q }, function(status, response) {
                    if (status !== naver.maps.Service.Status.OK) return reject({ status, response });
                    const list = (response && response.v2 && Array.isArray(response.v2.addresses)) ? response.v2.addresses : [];
                    if (!list.length) return reject({ status: 'NO_RESULT', response });
                    const a = list[0];
                    const x = Number(a.x); // longitude
                    const y = Number(a.y); // latitude
                    if (!Number.isFinite(x) || !Number.isFinite(y)) return reject({ status: 'BAD_COORDS', response });
                    showMap(y, x);
                    // 자동 채움
                    roadAddress.value = a.roadAddress || a.jibunAddress || '';
                    zipCode.value = a.zipcode || '';
                    form.dataset.lat = String(y);
                    form.dataset.lng = String(x);
                    form.style.display = 'block';
                    detailAddress.focus();
                    resolve();
                });
            });
        } catch (err) {
            console.warn('geocode search failed', err);
            alert('검색 결과가 없습니다. 다른 키워드로 다시 시도해주세요.');
        } finally {
            if (btnAddrSearch) btnAddrSearch.disabled = false;
        }
    }

    if (btnAddrSearch) {
        btnAddrSearch.addEventListener('click', searchByText);
    }
    if (addrQuery) {
        addrQuery.addEventListener('keydown', function(e) {
            if (e.key === 'Enter') {
                e.preventDefault();
                searchByText();
            }
        });
    }

    form.addEventListener('submit', async function(e) {
        e.preventDefault();
        const payload = {
            alias: alias.value.trim() || '집',
            zipCode: zipCode.value.trim(),
            roadAddress: roadAddress.value.trim(),
            detailAddress: detailAddress.value.trim(),
            isDefault: isDefault && isDefault.checked === true,
            // 네이버 응답은 x=경도, y=위도 (문자열) → 숫자 변환 서버에서 BigDecimal로 파싱 가능
            latitude: form.dataset.lat ? Number(form.dataset.lat) : null,
            longitude: form.dataset.lng ? Number(form.dataset.lng) : null
        };
        try {
            const res = await fetch('/api/user/addresses', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                credentials: 'include',
                body: JSON.stringify(payload)
            });
            const result = await res.json();
            if (result.resultCode === 200) {
                window.location.href = returnPath;
            } else {
                alert('주소 저장 실패: ' + (result.message || '오류'));
            }
        } catch (err) {
            console.error(err);
            alert('네트워크 오류로 주소 저장에 실패했어요.');
        }
    });
});


