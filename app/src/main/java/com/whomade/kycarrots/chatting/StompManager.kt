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
        if (stompClient?.isConnected == true) {
            Log.d("STOMP", "이미 연결된 상태입니다. 재연결을 건너뜁니다.")
            return
        }

        val urlWithUserId = "${Constants.BASE_CHAT_URL}$userId"
        Log.d("STOMP", "서버 접속 시도 URL: $urlWithUserId")
        
        stompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, urlWithUserId)

        stompClient?.lifecycle()?.subscribe { event ->
            when (event.type) {
                LifecycleEvent.Type.OPENED -> {
                    Log.d("STOMP", "✅ WebSocket 연결 성공! (OPENED)")
                }
                LifecycleEvent.Type.ERROR -> {
                    Log.e("STOMP", "❌ WebSocket 연결 오류 발생! (호출 URL: $urlWithUserId)")
                    Log.e("STOMP", "상태: ${event.type}, 오류 상세: ", event.exception)
                }
                LifecycleEvent.Type.CLOSED -> {
                    Log.d("STOMP", "ℹ️ WebSocket 연결 종료 (CLOSED)")
                }
                LifecycleEvent.Type.FAILED_SERVER_HEARTBEAT -> {
                    Log.e("STOMP", "⚠️ 서버 하트비트 실패 (URL: $urlWithUserId)")
                }
            }
        }

        stompClient?.connect()
    }

    fun isConnected(): Boolean = stompClient?.isConnected == true

    fun sendMessage(message: ChatMessage) {
        val json = Gson().toJson(message)
        Log.d("STOMP", "메시지 전송 시도: $json")
        stompClient?.send("/app/chat.send", json)
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe({ Log.d("STOMP", "✅ 메시지 전송 성공 (/app/chat.send)") },
                { e -> Log.e("STOMP", "❌ 메시지 전송 실패", e) })
    }

    fun sendMessageRoomId(message: ChatMessage) {
        val json = Gson().toJson(message)
        val destination = "/app/chat.send.${message.roomId}"
        Log.d("STOMP", "방별 메시지 전송 시도 ($destination): $json")
        stompClient?.send(destination, json)
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe({ Log.d("STOMP", "✅ 메시지 전송 성공 to $destination") },
                { e -> Log.e("STOMP", "❌ 전송 실패 (destination: $destination)", e) })
    }

    /** 구독: Disposable을 저장해두고, 나중에 topicPath로 해제 가능 */
    fun subscribe(topicPath: String, onMessageReceived: (ChatMessage) -> Unit) {
        // 중복 구독 방지
        if (subscriptions.containsKey(topicPath)) {
            Log.d("STOMP", "이미 구독 중인 토픽입니다: $topicPath")
            return
        }

        Log.d("STOMP", "토픽 구독 시도: $topicPath")
        val d = stompClient?.topic(topicPath)
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe({ topicMessage ->
                Log.d("STOMP", "📩 메시지 수신 완료 (from $topicPath)")
                val received = Gson().fromJson(topicMessage.payload, ChatMessage::class.java)
                onMessageReceived(received)
            }, { e -> Log.e("STOMP", "❌ 토픽 구독 실패: $topicPath", e) })

        if (d != null) {
            subscriptions[topicPath] = d
            Log.d("STOMP", "✅ 토픽 구독 성공 및 리스트 등록: $topicPath")
        }
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
            if (stompClient != null) {
                // disconnect()를 호출하면 비동기로 소켓을 닫습니다.
                // 바로 null로 날려버리면 종료 프레임 전달 전 스레드가 죽을 수 있으므로
                // 참조를 살려두고 네트워크 단에서 종료되길 기다립니다.
                val client = stompClient
                stompClient = null 
                client?.disconnect()
            }
        } catch (e: Throwable) {
            Log.w("STOMP", "disconnect 중 오류", e)
        }
    }
}
