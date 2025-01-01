package com.momo.chat.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@EnableWebSocketMessageBroker
@Configuration
public class StompWebSocketConfig implements WebSocketMessageBrokerConfigurer {

  @Override
  public void registerStompEndpoints(StompEndpointRegistry registry) {
    registry.addEndpoint("/ws") // 여기로 웹소켓 생성
//        .addInterceptors()
        .setAllowedOriginPatterns("*");
    // sock.js를 통하여 낮은 버전의 브라우저에서도 websocket 이 동작할 수 있게 설정
    // api 통신 시, withSockJS() 설정을 빼야됨
    //.withSockJS();
  }

  @Override
  public void configureMessageBroker(MessageBrokerRegistry registry) {
    // 메시지를 발행하는 요청 url -> 메시지를 보낼 때
    registry.setApplicationDestinationPrefixes("/pub");
    // 메시지를 구독하는 요청 url -> 메시지를 받을 때
    registry.enableSimpleBroker("/sub");
  }

}
