package com.momo.notification.sseemitter;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Component
public class SseEmitterManager {

  @Value("${sse.timeout}")
  public static long sseTimeout;

  // ConcurrentHashMap을 사용하여 thread-safe하게 구현
  private final ConcurrentMap<Long, SseEmitter> emitterMap = new ConcurrentHashMap<>();

  /**
   * 새로운 SseEmitter 추가
   */
  public void add(Long userId, SseEmitter emitter) {
    emitterMap.put(userId, emitter);
  }

  /**
   * SseEmitter 제거
   */
  public void remove(Long userId) {
    emitterMap.remove(userId);
  }

  /**
   * 특정 사용자의 SseEmitter 조회
   */
  public Optional<SseEmitter> get(Long userId) {
    return Optional.ofNullable(emitterMap.get(userId));
  }
}
