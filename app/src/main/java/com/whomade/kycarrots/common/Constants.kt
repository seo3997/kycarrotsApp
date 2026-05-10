package com.whomade.kycarrots.common

object Constants {

    enum class ServerType {
        LOCAL,DEV,PROD
    }

    //여기서 서버 선택만 바꾸면 전체가 따라감
    private val currentServer = ServerType.PROD

    val BASE_URL: String
        get() = when (currentServer) {
            ServerType.LOCAL  -> "http://10.69.122.25:9000/"
            ServerType.DEV -> "http://www.kycarrots.com:9000/"
            ServerType.PROD -> "http://www.asagong.com/"
        }

    val BASE_CHAT_URL: String
        get() = when (currentServer) {
            ServerType.LOCAL  -> "ws://10.69.122.25:9000/chat-ws?userId="
            ServerType.DEV -> "ws://www.kycarrots.com:9000/chat-ws?userId="
            ServerType.PROD -> "ws://www.asagong.com/chat-ws?userId="
        }



    const val ROLE_ADMIN = "ROLE_ADMIN"
    const val ROLE_PUB = "ROLE_PUB"
    const val ROLE_SELL = "ROLE_SELL"
    const val ROLE_PROJ = "ROLE_PROJ"

    const val CENTER_BRANCH_ID = "2"
    const val APP_TEST_YN = "N"   //N 일때 패스워드 안나옴


}
