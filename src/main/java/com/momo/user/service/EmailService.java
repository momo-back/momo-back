package com.momo.user.service;

import com.momo.common.exception.CustomException;
import com.momo.common.exception.ErrorCode;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

  private final JavaMailSender mailSender;

  public EmailService(JavaMailSender mailSender) {
    this.mailSender = mailSender;
  }

  public void sendVerificationEmail(String recipientEmail, String token) throws MessagingException {
    String subject = "이메일 인증 요청";
    String verificationUrl = "http://localhost:8080/users/verify?token=" + token;
    String message = "<h1>이메일 인증</h1>" +
        "<p>아래 링크를 클릭하여 이메일 인증을 완료해주세요:</p>" +
        "<a href=\"" + verificationUrl + "\">이메일 인증하기</a>";

    MimeMessage mimeMessage = mailSender.createMimeMessage();
    MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");
    helper.setTo(recipientEmail);
    helper.setSubject(subject);
    helper.setText(message, true); // HTML 형식 허용

    mailSender.send(mimeMessage);
  }

  public void sendPasswordResetEmail(String recipientEmail, String token) {
    try {
      String subject = "비밀번호 재설정 요청";
      String resetUrl = "http://localhost:8080/api/v1/users/reset-password?token=" + token;
      String message = "<h1>비밀번호 재설정</h1>" +
          "<p>아래 링크를 클릭하여 비밀번호를 재설정해주세요:</p>" +
          "<a href=\"" + resetUrl + "\">비밀번호 재설정하기</a>";

      MimeMessage mimeMessage = mailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");
      helper.setTo(recipientEmail);
      helper.setSubject(subject);
      helper.setText(message, true);

      mailSender.send(mimeMessage);
    } catch (MessagingException e) {
      throw new CustomException(ErrorCode.EMAIL_SEND_FAILED);
    }
  }

}
