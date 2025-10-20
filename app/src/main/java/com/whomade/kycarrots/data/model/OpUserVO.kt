package com.whomade.kycarrots.data.model

data class OpUserVO(
    var userNo: Long = 0L,                  // 사용자 고유 번호
    var userId: String = "",                // 사용자 ID
    var password: String = "",              // 비밀번호
    var userNm: String = "",                // 사용자 이름
    var cttpcSeCode: String = "",           // 통신사 구분 코드
    var cttpc: String = "",                 // 통신사 정보
    var email: String = "",                 // 이메일
    var areaCode: String = "",              // 지역 코드
    var areaCodeNm: String = "",              // 지역 코드
    var areaSeCodeS: String = "",           // 1차 지역 코드
    var areaSeCodeSNm: String = "",           // 1차 지역 코드
    var areaSeCodeD: String = "",           // 2차 지역 코드
    var userSttusCode: String = "",         // 사용자 상태 코드
    var loginDt: String = "",               // 마지막 로그인 일시
    var userAge: String = "",               // 사용자 나이
    var birthDate: String = "",             // 생년월일 (YYYY-MM-DD)
    var uniqueIdentifier: String = "",      // 고유 식별자 (CI)
    var deviceId: String = "",              // 디바이스 ID
    var duplicateIdentifier: String = "",   // 동일인 식별 정보 (DI)
    var gender: Int = 0,                    // 성별 (1: 남, 2: 여)
    var memberCode: String = "",            // 회원 구분 코드
    var citizenshipType: Int = 0,           // 내/외국인 구분
    var passwordHash: String = "",          // 해시된 비밀번호
    var referrerId: String = "",            // 추천인 ID
    var registerNo: Int = 0,                // 등록자 번호
    var registDt: String = "",              // 등록 일시
    var updusrNo: Int = 0,                  // 수정자 번호
    var updtDt: String = "",                 // 수정 일시
    var provider: String = "",               // SNS 유형
    var providerUserId: String = ""          // SNS ID
)
