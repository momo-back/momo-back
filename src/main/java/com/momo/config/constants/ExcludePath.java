package com.momo.config.constants;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;

@Getter
@RequiredArgsConstructor
public enum ExcludePath {
  MEETINGS_LIST("/api/v1/meetings", HttpMethod.GET);
  // 추후 제외할 경로가 생기면 여기에 추가

  private final String path;
  private final HttpMethod method;
}
