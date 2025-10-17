package com.whomade.kycarrots.data.model

// 요청: 각 플랫폼에서 얻은 토큰을 담아서 보냄
data class SocialAuthRequest(
    val provider: String,           // "KAKAO" | "NAVER" | "GOOGLE"
    val providerUserId: String, // Kakao/Naver 보통 access_token
    val accessToken: String? = null, // Kakao/Naver 보통 access_token
    val idToken: String? = null,     // Google은 idToken(필요 시 Kakao도 OIDC면 사용)
    val deviceId: String? = null,    // 선택: 서버가 디바이스 관리/푸시토큰 매핑에 쓰려면
    val appVersion: String? = null   // 선택
)