package com.momo.config.token;

import com.momo.config.token.service.TokenService;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/token")
public class ReissueController {

  private final TokenService tokenService;

  public ReissueController(TokenService tokenService) {
    this.tokenService = tokenService;
  }

  @PostMapping("/reissue")
  public ResponseEntity<?> reissue(HttpServletRequest request, HttpServletResponse response) {
    ResponseEntity<?> result = tokenService.reissueToken(request, response);

    // 토큰이 성공적으로 갱신되었는지 확인
    if (result.getStatusCode().is2xxSuccessful()) {
      String newAccessToken = (String) result.getBody();
      return ResponseEntity.ok().body(Map.of("access_token", newAccessToken));
    }

    // 실패 시 해당 응답 반환
    return result;
  }
}