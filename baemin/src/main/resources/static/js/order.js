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

  // 결제수단 매핑
  function resolveGateway() {
    const selected = getSelectedRadio();

    const dataPg = selected?.dataset.pg;
    const dataMethod = selected?.dataset.method;
    if (dataPg || dataMethod) {
      return {
        pg: dataPg || 'html5_inicis',
        pay_method: dataMethod || 'card',
      };
    }

    const payName = getSelectedPayName();

    if (/카카오|kakao/i.test(payName)) {
      return { pg: 'kakaopay.TC0ONETIME', pay_method: 'kakaopay' };
    }
    if (/토스|toss/i.test(payName)) {
      return { pg: 'tosspay.tosstest', pay_method: 'tosspay' };
    }
    return { pg: 'kicc.T5102001', pay_method: 'card' };
  }

  // 결제 요청
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

    if (!window.IMP) {
      alert('결제 모듈이 로드되지 않았습니다.');
      return;
    }

    // 버튼 비활성화
    if (btnEl) btnEl.disabled = true;

    // 가맹점 코드 초기화 (여기 직접 넣기)
    window.IMP.init('imp87380624');

    const { pg, pay_method } = resolveGateway();
    const merchantUid = 'mid_' + Date.now();

    const buyerName = container.dataset.buyerName || '주문자';
    const buyerTel = container.dataset.buyerTel || '';
    const buyerEmail = container.dataset.buyerEmail || '';
    const buyerAddr = container.dataset.buyerAddr || '';
    const buyerPostcode = container.dataset.buyerPostcode || '';

    window.IMP.request_pay(
      {
        pg,
        pay_method,
        merchant_uid: merchantUid,
        name: '배달의민족 주문 결제',
        amount: total,
        buyer_email: buyerEmail,
        buyer_name: buyerName,
        buyer_tel: buyerTel,
        buyer_addr: buyerAddr,
        buyer_postcode: buyerPostcode,
      },
      function (rsp) {
        if (rsp.success) {
          if (btnEl) btnEl.disabled = false;
          // 결제 완료 시 서버로 이동하여 결제정보 저장 및 완료 페이지 표시
          const qsParams = {
            imp_uid: rsp.imp_uid || '',
            merchant_uid: rsp.merchant_uid || merchantUid,
            amount: String(total),
            pg,
            pay_method,
          };
          const cartItemOptionId = container.dataset.cartItemOptionId;
          if (cartItemOptionId) qsParams.cartItemOptionId = cartItemOptionId;
          const qs = new URLSearchParams(qsParams).toString();
          window.location.href = `/api/orders/complete?${qs}`;
        } else {
          alert(`결제 실패\n사유: ${rsp.error_msg || '알 수 없는 오류'}`);
          if (btnEl) btnEl.disabled = false;
        }
      }
    );
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
