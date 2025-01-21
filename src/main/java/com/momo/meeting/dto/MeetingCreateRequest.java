package com.momo.meeting.dto;

import com.momo.meeting.constant.FoodCategory;
import com.momo.meeting.constant.MeetingStatus;
import com.momo.meeting.entity.Meeting;
import com.momo.user.entity.User;
import java.time.LocalDateTime;
import java.util.Set;
import javax.validation.constraints.Future;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import org.hibernate.validator.constraints.Range;

@Getter
@Builder
public class MeetingCreateRequest {

  @NotBlank(message = "제목을 입력해주세요.")
  @Size(min = 1, max = 60, message = "제목은 1-60자 사이로 입력해주세요.")
  private String title;

  @NotNull(message = "위치 아이디가 누락되었습니다.")
  private Long locationId;

  @NotNull(message = "위도가 누락되었습니다")
  @Range(min = -90, max = 90, message = "위도는 -90부터 90 사이로 입력해주세요.")
  private Double latitude;

  @NotNull(message = "경도가 누락되었습니다")
  @Range(min = -180, max = 180, message = "경도는 -180부터 180 사이로 입력해주세요.")
  private Double longitude;

  @NotNull(message = "주소가 누락되었습니다")
  private String address;

  @NotNull(message = "모임 날짜와 시간을 선택해주세요.")
  @Future(message = "모임 날짜는 현재 시간 이후로 선택해주세요.")
  private LocalDateTime meetingDateTime;

  @Min(value = 2, message = "모임 인원은 2명 이상이어야 합니다.")
  private Integer maxCount;

  @NotEmpty(message = "카테고리를 1개 이상 선택해주세요.")
  private Set<FoodCategory> category;

  @NotBlank(message = "모임 내용을 입력해주세요.")
  @Size(min = 1, max = 600, message = "내용은 600자 이하로 입력해주세요.")
  private String content;

  public static Meeting toEntity(MeetingCreateRequest request, User user, String thumbnail) {
    return Meeting.builder()
        .user(user)
        .title(request.getTitle())
        .locationId(request.getLocationId())
        .latitude(request.getLatitude())
        .longitude(request.getLongitude())
        .address(request.getAddress())
        .meetingDateTime(request.getMeetingDateTime())
        .approvedCount(1)
        .maxCount(request.getMaxCount())
        .category(request.getCategory())
        .content(request.getContent())
        .thumbnail(thumbnail)
        .meetingStatus(MeetingStatus.RECRUITING)
        .build();
  }
}
