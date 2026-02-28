1.주문서 페이지(Order Sheet) 기획 정의서 (MRD)
1-1 개요
   목적: 사용자가 구매하려는 상품 정보를 최종 확인하고, 배송지 및 결제 정보를 입력하여 주문을 생성함.
    진입 시점: 상품 상세 페이지에서 '결제하기' 또는 '바로 구매' 버튼 클릭 시 진입.

1-2 주요 기능 및 사용자 요구사항
   A. 배송지 정보 관리 (Shipping Address)
   최근 배송지 자동 로드: userNo를 기준으로 최근 주문 건의 주소 정보를 기본값으로 세팅.

   신규 주소 입력: 주소 검색 API(카카오 등)를 연동하여 우편번호와 기본 주소를 자동 입력.
   정보: 이름, 연락처(휴대폰 번호) 필수 입력 및 유효성 검사.
   배송 메모: '문 앞에 놓아주세요' 등 프리셋 선택 및 직접 입력 기능.

   B. 주문 상품 확인 (Order Items Summary)
    상품 정보 노출: productId, productName, optionName, quantity를 리스트 형태로 표시.
    가격 스냅샷: 주문 시점의 unitPrice를 고정하여 표시 (추후 가격 변동에 대비).
    
   C. 결제 금액 계산 (Price Calculation)
    실시간 합산: 상품 금액 합계 + 배송비 - 할인 금액 = 최종 결제 금액(totalPayAmount) 표시.
    항목별 상세 내역: 사용자가 최종 금액이 어떻게 산출되었는지 명확히 인지하도록 구분하여 표기.
    
   D. 결제 수단 선택 (Payment Method)
    토스페이먼츠 위젯 연동: 카드, 계좌이체, 가상계좌 등 결제 수단 선택 UI 노출.
    약관 동의: 결제 진행을 위한 필수 약관 동의 체크박스 구성.

1-3 데이터 입력 및 API 연동 정의 (Data Mapping)
   사용자가 '최종 결제하기' 버튼을 누를 때 서버로 전송할 데이터 구조(JSON)와 매핑되는 테이블 정보입니다.

2. 서버 프로그램 호출
    AppServiceProvider -> AppService -> RemoteRepository ->  AdApi -> RetrofitProvider
    2-1 endPoint
        2-1-1 주문생성 endPoint
                /api/payment/order/create
        2-1-2  주문생성 requstBody
               {
                   "userNo": 1,
                   "totalItemAmount": 50000,
                   "deliveryFee": 3000,
                   "discountAmount": 0,
                   "totalPayAmount": 53000,
                   "receiverName": "홍길동",
                   "receiverPhone": "010-1234-5678",
                   "zipCode": "12345",
                   "address1": "서울시 강남구...",
                   "address2": "101호",
                   "orderMemo": "배송 전 연락 바랍니다.",
                   "items": [
                   {
                   "productId": 1,
                   "quantity": 1,
                   "optionName": "XL"
                   }
                   ]
               }
        2-1-3 주문생성 responseBody
               {
                   "success": true,
                   "orderId": 123,
                   "orderNo": "ORDER-1708348400000"
               }

    2-2 결재승인 endPoint
       2-2-1 결재승인 endPoint
             /api/payment/confirm
       2-2-2 결재승인 requstBody
           {
               "paymentKey": "tgen_20240219...",
               "orderId": "ORDER-1708348400000",
               "amount": 53000
           }
       2-2-3 결재승인 responseBody
           {
               "success": true,
               "message": "Payment confirmed"
           }
   3. 화면개발
      3-1. 상품상세 결제 버튼 추가 (AdDetailActivity)
      UI 구성: activity_detail.xml 하단 플로팅 바 또는 하단 고정 영역에 [결제하기] 버튼 추가.
      이벤트: 버튼 클릭 시 productId, productName, unitPrice, selectedOption 등 결제에 필요한 최소 데이터를 Intent에 담아 OrderActivity로 전달합니다.
      3-2. 주문화면 생성 (OrderActivity)
      주문화면은 사용자가 최종 결제 정보를 입력하고 확인하는 신규 화면입니다.
      가. 화면 구성 요소 (UI Layout)
          섹션	기능 요약	관련 DB 컬럼 (tb_orders)
          1. 배송지 정보	수령인, 연락처, 주소 검색(Postcode API), 상세주소 입력	RECEIVER_NAME, RECEIVER_PHONE, ADDRESS1/2, ZIP_CODE
          2. 주문 상품	상세에서 넘어온 상품명, 옵션, 수량, 가격 표시	tb_order_items 스냅샷 데이터
          3. 배송 요청사항	배송 시 유의사항 입력 (직접 입력/선택형)	ORDER_MEMO
          4. 결제 금액	상품 금액 + 배송비 - 할인액 = 최종 결제 금액	TOTAL_PAY_AMOUNT
          5. 결제 수단	토스페이먼츠 결제위젯이 렌더링되는 영역	PAYMENT_METHOD
             나. 주요 비즈니스 로직 (Logic)
             데이터 초기화: Intent로 전달받은 상품 정보를 화면에 바인딩하고, userNo를 이용해 기존 배송지 정보가 있다면 서버에서 불러와 자동 기입합니다.
      나. 주요 비즈니스 로직 (Logic)
        결제 수단 렌더링: 토스페이먼츠 SDK를 초기화하고 paymentMethodWidget.renderPaymentMethods()를 호출하여 UI를 그려줍니다.
        주문 생성 및 결제 실행:
        사용자가 [결제하기] 클릭 시, 입력된 모든 데이터를 JSON으로 구성합니다.
        Step 1: 서버 API(POST /api/payment/order/create)를 호출하여 tb_orders 레코드를 생성하고 orderNo(MERCHANT_UID)를 받아옵니다.
        Step 2: 서버로부터 응답받은 orderNo를 사용하여 토스 SDK의 requestPayment()를 호출합니다.
      다. 예외 처리 사항
          입력 검증: 수령인 이름, 주소, 연락처가 비어있을 경우 결제 버튼을 비활성화하거나 경고 메시지를 노출합니다.
          재고 확인: 주문화면 진입 시 해당 옵션의 재고가 0인 경우 "품절된 상품입니다" 메시지와 함께 이전 화면으로 복귀시킵니다.
          결제 중단: 사용자가 결제창을 닫거나 취소했을 때 tb_orders의 상태를 적절히 처리할 수 있도록 리스너를 구현합니다.
       3-2 주문상세 에서 결체취소 
           주문후 7일 이내 상품은 결제최소 버튼 노출 결제 취소 버튼 클릭시 결제취소 처리 
           EndPoint: POST /api/payment/cancel   
           input:
           {
               "paymentKey": "tgen_20240219...",
               "orderId": "ORDER-1708348400000",
               "amount": 53000
           }
           output:
           {
               "success": true,
               "message": "Payment cancelled"
           }
   4. 요약된 결제 시퀀스 (MRD 기반)
     App: 주문 정보 입력 및 결제하기 클릭
     App → Server: POST /api/payment/order/create (주문서 가등록)
     App → PG: 토스 결제창 호출 및 사용자 인증
     App → Server: POST /api/payment/confirm (최종 결제 승인 및 DB 업데이트)
     Server → PG: 토스 서버로 실제 결제 승인 요청 (Secret Key 사용)
     App: 최종 결제 완료 화면 노출