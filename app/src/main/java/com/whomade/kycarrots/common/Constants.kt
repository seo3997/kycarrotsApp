package com.whomade.kycarrots.common

object Constants {

    enum class ServerType {
        LOCAL,DEV,PROD
    }

    //여기서 서버 선택만 바꾸면 전체가 따라감
    private val currentServer = ServerType.DEV

    val BASE_URL: String
        get() = when (currentServer) {
            ServerType.LOCAL  -> "http://10.69.122.25:9000/"
            ServerType.DEV -> "http://52.231.229.156:9000/"
            ServerType.PROD -> "http://52.231.229.156:9000/"
        }

    val BASE_CHAT_URL: String
        get() = when (currentServer) {
            ServerType.LOCAL  -> "ws://10.69.122.25:9000/chat-ws?userId="
            ServerType.DEV -> "ws://52.231.229.156:9000/chat-ws?userId="
            ServerType.PROD -> "ws://52.231.229.156:9000/chat-ws?userId="
        }


    const val SYSTEM_TYPE = 1  // 또는 2로 설정

    const val ROLE_PUB = "ROLE_PUB"
    const val ROLE_SELL = "ROLE_SELL"
    const val ROLE_PROJ = "ROLE_PROJ"


}
