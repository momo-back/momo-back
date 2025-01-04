package com.momo.notification.sseemitter;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@ExtendWith(MockitoExtension.class)
class SseEmitterManagerTest {

  private SseEmitterManager sseEmitterManager;

  @Test
  @DisplayName("이미터 추가 및 조회 - 성공")
  void addAndGet_Success() {
    // given
    Long userId = 1L;
    SseEmitter emitter = new SseEmitter();
    sseEmitterManager = new SseEmitterManager();

    // when
    sseEmitterManager.add(userId, emitter);
    Optional<SseEmitter> result = sseEmitterManager.get(userId);

    // then
    assertThat(result.isPresent()).isTrue();
    assertThat(result.get()).isEqualTo(emitter);
  }

  @Test
  @DisplayName("이미터 제거 - 성공")
  void remove_Success() {
    // given
    Long userId = 1L;
    SseEmitter emitter = new SseEmitter();
    sseEmitterManager = new SseEmitterManager();

    sseEmitterManager.add(userId, emitter);

    // when
    sseEmitterManager.remove(userId);

    // then
    assertThat(sseEmitterManager.get(userId)).isEmpty();
  }
}