package com.whomade.kycarrots.chatting

import android.util.Log
import com.google.gson.Gson
import com.whomade.kycarrots.common.Constants
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import ua.naiksoftware.stomp.Stomp
import ua.naiksoftware.stomp.StompClient
import ua.naiksoftware.stomp.dto.LifecycleEvent
import ua.naiksoftware.stomp.dto.StompHeader

object StompManager {

    private var stompClient: StompClient? = null

    fun connect(userId: String) {
        val urlWithUserId = "${Constants.BASE_CHAT_URL}$userId"
        stompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, urlWithUserId)

        stompClient?.lifecycle()?.subscribe { event ->
            when (event.type) {
                LifecycleEvent.Type.OPENED -> Log.d("STOMP", "연결됨")
                LifecycleEvent.Type.ERROR -> Log.e("STOMP", "연결 오류", event.exception)
                LifecycleEvent.Type.CLOSED -> Log.d("STOMP", "연결 종료")
                else -> {}
            }
        }

        stompClient?.connect()
    }

    fun sendMessage(message: ChatMessage) {
        val json = Gson().toJson(message)
        stompClient?.send("/app/chat.send", json)
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe({
                Log.d("STOMP", "메시지 전송 성공")
            }, { error ->
                Log.e("STOMP", "전송 실패", error)
            })
    }

    fun sendMessageRoomId(message: ChatMessage) {
        val json = Gson().toJson(message)
        val destination = "/app/chat.send.${message.roomId}"

        stompClient?.send(destination, json)
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe({
                Log.d("STOMP", "메시지 전송 성공 to $destination")
            }, { error ->
                Log.e("STOMP", "전송 실패", error)
            })
    }

    fun subscribe(topicPath: String, onMessageReceived: (ChatMessage) -> Unit) {
        stompClient?.topic(topicPath)
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe({ topicMessage ->
                val received = Gson().fromJson(topicMessage.payload, ChatMessage::class.java)
                onMessageReceived(received)
            }, { error ->
                Log.e("STOMP", "구독 실패", error)
            })
    }
}