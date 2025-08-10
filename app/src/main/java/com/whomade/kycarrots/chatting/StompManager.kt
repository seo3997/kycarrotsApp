package com.whomade.kycarrots.chatting

import android.util.Log
import com.google.gson.Gson
import com.whomade.kycarrots.common.Constants
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import ua.naiksoftware.stomp.Stomp
import ua.naiksoftware.stomp.StompClient
import ua.naiksoftware.stomp.dto.LifecycleEvent

object StompManager {

    private var stompClient: StompClient? = null
    private val subscriptions = mutableMapOf<String, Disposable>()

    fun connect(userId: String) {
        // 이미 연결돼 있으면 재사용
        if (stompClient?.isConnected == true) return

        val urlWithUserId = "${Constants.BASE_CHAT_URL}$userId"
        stompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, urlWithUserId)

        stompClient?.lifecycle()?.subscribe { event ->
            when (event.type) {
                LifecycleEvent.Type.OPENED -> Log.d("STOMP", "연결됨")
                LifecycleEvent.Type.ERROR  -> Log.e("STOMP", "연결 오류", event.exception)
                LifecycleEvent.Type.CLOSED -> Log.d("STOMP", "연결 종료")
                else -> {}
            }
        }

        stompClient?.connect()
    }

    fun isConnected(): Boolean = stompClient?.isConnected == true

    fun sendMessage(message: ChatMessage) {
        val json = Gson().toJson(message)
        stompClient?.send("/app/chat.send", json)
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe({ Log.d("STOMP", "메시지 전송 성공") },
                { e -> Log.e("STOMP", "전송 실패", e) })
    }

    fun sendMessageRoomId(message: ChatMessage) {
        val json = Gson().toJson(message)
        val destination = "/app/chat.send.${message.roomId}"
        stompClient?.send(destination, json)
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe({ Log.d("STOMP", "메시지 전송 성공 to $destination") },
                { e -> Log.e("STOMP", "전송 실패", e) })
    }

    /** 구독: Disposable을 저장해두고, 나중에 topicPath로 해제 가능 */
    fun subscribe(topicPath: String, onMessageReceived: (ChatMessage) -> Unit) {
        // 중복 구독 방지
        if (subscriptions.containsKey(topicPath)) return

        val d = stompClient?.topic(topicPath)
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe({ topicMessage ->
                val received = Gson().fromJson(topicMessage.payload, ChatMessage::class.java)
                onMessageReceived(received)
            }, { e -> Log.e("STOMP", "구독 실패", e) })

        if (d != null) subscriptions[topicPath] = d
    }

    /** 특정 토픽 구독 해제 */
    fun unsubscribe(topicPath: String) {
        subscriptions.remove(topicPath)?.dispose()
    }

    /** 모든 구독 해제 */
    fun clearSubscriptions() {
        subscriptions.values.forEach { runCatching { it.dispose() } }
        subscriptions.clear()
    }

    /** 소켓 연결 종료 (서버가 disconnect 이벤트 감지) */
    fun disconnect() {
        clearSubscriptions()
        try {
            stompClient?.disconnect()
        } catch (e: Throwable) {
            Log.w("STOMP", "disconnect 중 오류", e)
        } finally {
            stompClient = null
        }
    }
}
