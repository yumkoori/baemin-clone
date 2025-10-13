(() => {
  // 통화 포맷터
  function formatCurrency(value) {
    try {
      return new Intl.NumberFormat('ko-KR').format(Number(value));
    } catch {
      return String(value);
    }
  }

  // 금액 UI 동기화 + data-total 저장 + 버튼 상태
  function syncAmounts() {
    const container = document.querySelector('.container');
    if (!container) return;

    const price = Number(container.dataset.price || 0);
    const delivery = Number(container.dataset.delivery || 0);
    const discount = Number(container.dataset.discount || 0);
    const total = Math.max(0, price + delivery - discount);

    container.dataset.total = String(total);

    const priceEl = document.querySelector('[data-field="price"]');
    const deliveryEl = document.querySelector('[data-field="delivery"]');
    const discountEl = document.querySelector('[data-field="discount"]');
    const totalEl = document.querySelector('[data-field="total"]');
    const ctaBtn = document.querySelector('.cta');

    if (priceEl) priceEl.textContent = `${formatCurrency(price)}원`;
    if (deliveryEl) deliveryEl.textContent = `${formatCurrency(delivery)}원`;
    if (discountEl) discountEl.textContent = `-${formatCurrency(discount)}원`;
    if (totalEl) totalEl.textContent = `${formatCurrency(total)}원`;
    if (ctaBtn) {
      ctaBtn.textContent = `${formatCurrency(total)}원 결제하기`;
      ctaBtn.disabled = total <= 0;
    }
  }

  // 선택된 라디오
  function getSelectedRadio() {
    return document.querySelector('.payments input[type="radio"]:checked');
  }

  // 결제수단 표시명
  function getSelectedPayName() {
    const checked = getSelectedRadio();
    if (!checked) return 'CARD';
    const label = checked.closest('label');
    const nameEl = label && label.querySelector('.pay-name');
    return nameEl ? nameEl.textContent.trim() : (checked.value || 'CARD');
  }

  // 결제수단 매핑 (V2)
  function resolvePayMethod() {
    const payName = getSelectedPayName();

    if (/카카오|kakao/i.test(payName)) {
      return 'EASY_PAY';
    }
    if (/토스|toss/i.test(payName)) {
      return 'EASY_PAY';
    }
    if (/신용|체크|카드/i.test(payName)) {
      return 'CARD';
    }
    if (/기타/i.test(payName)) {
      return null;
    }
    return 'CARD';
  }

  // 간편결제 제공사 매핑
  function resolveEasyPayProvider() {
    const payName = getSelectedPayName();

    if (/카카오|kakao/i.test(payName)) {
      return 'KAKAOPAY';
    }
    if (/토스|toss/i.test(payName)) {
      return 'TOSSPAY';
    }
    return null;
  }

  // payMethod 문자열 추출 (리다이렉트용)
  function getPayMethodString() {
    const payName = getSelectedPayName();
    if (/카카오|kakao/i.test(payName)) return 'kakaopay';
    if (/토스|toss/i.test(payName)) return 'tosspay';
    if (/신용|체크|카드/i.test(payName)) return 'card';
    return 'card';
  }

  // 채널키 매핑 (V2)
  function resolveChannelKey() {
    const payName = getSelectedPayName();

    if (/카카오|kakao/i.test(payName)) {
      return 'channel-key-8ae05218-f347-412e-903f-a4b7c33703b6'; // 카카오페이
    }
    if (/토스|toss/i.test(payName)) {
      return 'channel-key-8ff2c522-b8b3-4622-b1df-2e0200f88b00'; // 토스페이
    }
    if (/신용|체크|카드/i.test(payName)) {
      return 'channel-key-76082135-f920-4755-aa2a-616fe62d244b'; // 나이스페이먼츠 (신용/체크카드)
    }
    return null;
  }

  // 결제 요청 (V2)
  async function requestPortOnePayment(btnEl) {
    const container = document.querySelector('.container');
    if (!container) {
      alert('결제 컨테이너를 찾을 수 없습니다.');
      return;
    }

    const total = Number(container.dataset.total || 0);
    if (!total || total <= 0) {
      alert('결제 금액이 올바르지 않습니다.');
      return;
    }

    if (!window.PortOne) {
      alert('결제 모듈이 로드되지 않았습니다.');
      return;
    }

    // 버튼 비활성화
    if (btnEl) btnEl.disabled = true;

    const payMethod = resolvePayMethod();
    const channelKey = resolveChannelKey();
    const easyPayProvider = resolveEasyPayProvider();

    if (!payMethod || !channelKey) {
      alert('지원하지 않는 결제 방식입니다. (만나서 결제 등)');
      if (btnEl) btnEl.disabled = false;
      return;
    }

    const paymentId = 'mid_' + Date.now();

    const buyerName = container.dataset.buyerName || '주문자';
    const buyerTel = container.dataset.buyerTel || '';
    const buyerEmail = container.dataset.buyerEmail || '';

    const mainAddrEl = document.querySelector('.addr:not(.sub)');
    const subAddrEl = document.querySelector('.addr.sub');

    const buyerAddr1 = mainAddrEl ? mainAddrEl.textContent.trim() : '';
    const buyerAddr2 = subAddrEl ? subAddrEl.textContent.trim() : '';
    const buyerPostcode = container.dataset.buyerPostcode || '';

    const customer = {
      fullName: buyerName,
    };

    // phoneNumber는 빈 문자열이면 안 되므로 값이 있을 때만 추가
    if (buyerTel && buyerTel.trim()) {
      customer.phoneNumber = buyerTel;
    }

    // email도 값이 있을 때만 추가
    if (buyerEmail && buyerEmail.trim()) {
      customer.email = buyerEmail;
    }

    // address는 최소한 addressLine1이 있을 때만 추가
    if (buyerAddr1 || buyerAddr2 || buyerPostcode) {
      customer.address = {};
      if (buyerAddr1) customer.address.addressLine1 = buyerAddr1;
      if (buyerAddr2) customer.address.addressLine2 = buyerAddr2;
      if (buyerPostcode) customer.address.postalCode = buyerPostcode;
    }

    try {
      const paymentRequest = {
        storeId: 'store-3108681a-92bf-432e-9847-79defad4d72a',
        channelKey: channelKey,
        paymentId: paymentId,
        orderName: '배달의민족 주문 결제',
        totalAmount: total,
        currency: 'KRW',
        payMethod: payMethod,
        customer: customer,
        // 웹훅을 수신할 서버의 주소입니다.
        noticeUrls: ['https://6b613a78f5d3.ngrok-free.app/api/payment/webhook'],
        // 최종 결제 승인을 처리할 서버의 주소입니다. (V2에서 추가)
        confirmUrl: 'https://6b613a78f5d3.ngrok-free.app/api/payment/confirm',
      };

      // 간편결제인 경우 easyPayProvider 추가
      if (easyPayProvider) {
        paymentRequest.easyPayProvider = easyPayProvider;
      }

      const response = await PortOne.requestPayment(paymentRequest);

      if (response.code) {
        // 결제 실패 또는 사용자에 의한 결제 취소
        alert(`결제 실패
사유: ${response.message || '알 수 없는 오류'}`);
        if (btnEl) btnEl.disabled = false;
      } else {
        // 서버 최종 승인 후 로직
        // 서버에서 웹훅을 수신하여 주문 처리를 완료하므로,
        // 클라이언트에서는 성공 페이지로 리디렉션합니다.
        // 실제 서비스에서는 주문 완료 페이지로 이동시키는 것이 좋습니다.
        console.log('서버 승인 완료. 결제 성공:', response);
        window.location.href = `/order/complete?paymentId=${response.paymentId}`;
      }
    } catch (error) {
      console.error('결제 요청 중 오류:', error);
      alert(`결제 요청 실패
${error.message || '알 수 없는 오류'}`);
      if (btnEl) btnEl.disabled = false;
    }
  }

  function setupPayment() {
    const ctaBtn = document.querySelector('.cta');
    if (!ctaBtn) return;
    ctaBtn.addEventListener('click', (e) => {
      e.preventDefault();
      requestPortOnePayment(ctaBtn);
    });
  }

  document.addEventListener('DOMContentLoaded', () => {
    syncAmounts();
    setupPayment();
  });
})();