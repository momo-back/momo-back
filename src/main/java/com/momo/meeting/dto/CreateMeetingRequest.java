package com.momo.meeting.dto;

import com.momo.meeting.constant.FoodCategory;
import com.momo.meeting.persist.entity.Meeting;
import java.time.LocalDateTime;
import java.util.Set;
import javax.validation.constraints.Future;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.Getter;

@Getter
public class CreateMeetingRequest {

  @NotBlank(message = "제목을 입력해주세요.")
  @Size(min = 1, max = 60, message = "제목은 1-60자 사이로 입력해주세요.")
  private String title;

  @NotNull(message = "모임 날짜와 시간을 선택해주세요.")
  @Future(message = "모임 날짜는 현재 시간 이후로 선택해주세요.")
  private LocalDateTime meetingDateTime;

  @NotBlank(message = "모임 장소를 입력해주세요.")
  private Long locationId;

  @Min(value = 2, message = "모임 인원은 2명 이상이어야 합니다.")
  private Integer maxParticipants;

  @NotEmpty(message = "카테고리를 1개 이상 선택해주세요.")
  private Set<FoodCategory> categories;

  @NotBlank(message = "모임 내용을 입력해주세요.")
  @Size(min = 1, max = 600, message = "내용은 600자 이하로 입력해주세요.")
  private String content;

  private String thumbnailUrl;

  public Meeting toEntity(CreateMeetingRequest request) {
    return Meeting.builder()
        .title(request.getTitle())
        .meetingDateTime(request.getMeetingDateTime())
        .locationId(request.getLocationId())
        .maxParticipants(request.getMaxParticipants())
        .categories(request.getCategories())
        .content(request.getContent())
        .thumbnailUrl(request.getThumbnailUrl())
        .build();
  }
}
