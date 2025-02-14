package com.momo.meeting.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum MeetingErrorCode {

  MEETING_NOT_FOUND("존재하지 않는 모임입니다.", HttpStatus.NOT_FOUND),

  NOT_MEETING_OWNER("해당 모임의 작성자가 아닙니다.", HttpStatus.FORBIDDEN),

  DAILY_POSTING_LIMIT_EXCEEDED(
      "하루 포스팅 개수 제한을 초과하였습니다.", HttpStatus.TOO_MANY_REQUESTS),

  INVALID_MEETING_STATUS("유효한 모임 상태가 아닙니다.", HttpStatus.BAD_REQUEST),

  INVALID_MEETING_DATE_TIME(
      "유효한 모임 시간이 아닙니다.", HttpStatus.BAD_REQUEST),

  INVALID_FOOD_CATEGORY("유효한 음식 카테고리가 아닙니다.", HttpStatus.BAD_REQUEST),

  ALREADY_MAX_COUNT("모임 인원이 가득찼습니다.", HttpStatus.CONFLICT),

  INVALID_MEETING_DATE("모임 날짜는 1년까지 설정 가능합니다.", HttpStatus.BAD_REQUEST)
  ;

  private final String message;
  private final HttpStatus status;
}
