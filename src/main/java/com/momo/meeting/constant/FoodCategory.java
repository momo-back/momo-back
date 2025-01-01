package com.momo.meeting.constant;

import com.momo.meeting.exception.MeetingErrorCode;
import com.momo.meeting.exception.MeetingException;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum FoodCategory {
  KOREAN("한식"),
  CHINESE("중식"),
  JAPANESE("일식"),
  WESTERN("양식"),
  DESSERT("디저트"),
  OTHER("기타");

  private final String description;

  public static Set<String> convertToFoodCategories(String categoryStr) {
    if (categoryStr == null || categoryStr.isEmpty()) {
      return new HashSet<>();
    }

    Set<String> foodCategories = new HashSet<>();
    String[] categorise = categoryStr.split(",");

    for (String category : categorise) {
      FoodCategory.getFoodCategoryDescription(category).ifPresent(foodCategories::add);
    }
    return foodCategories;
  }

  private static Optional<String> getFoodCategoryDescription(String category) {
    try {
      return Optional.of(FoodCategory.valueOf(category).getDescription());
    } catch (IllegalArgumentException e) {
      throw new MeetingException(MeetingErrorCode.INVALID_FOOD_CATEGORY);
    }
  }
}
