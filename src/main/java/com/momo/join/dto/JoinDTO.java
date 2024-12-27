package com.momo.join.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class JoinDTO {

  public String email;
  private String password;
  private String nickname;
  private String phone;

}
