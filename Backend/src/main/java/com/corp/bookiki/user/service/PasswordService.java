package com.corp.bookiki.user.service;

import java.time.Duration;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.corp.bookiki.global.error.code.ErrorCode;
import com.corp.bookiki.global.error.exception.PasswordUpdateException;
import com.corp.bookiki.user.entity.UserEntity;
import com.corp.bookiki.user.repository.UserRepository;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PasswordService {

	private static final String PASSWORD_RESET_PREFIX = "PASSWORD_RESET:";
	private static final Duration RESET_TOKEN_TTL = Duration.ofMinutes(10);

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final JavaMailSender mailSender;
	private final StringRedisTemplate redisTemplate;

	@Value("${MAIL_USERNAME}")
	private String fromEmail;

	@Value("${FRONTEND_URL}")
	private String frontendUrl;

	@Transactional
	public void sendPasswordResetEmail(String email, String userName) {
		UserEntity user = userRepository.findByEmail(email)
			.orElseThrow(() -> new PasswordUpdateException(ErrorCode.USER_NOT_FOUND));

		if (!userName.equals(user.getUserName())) {
			throw new PasswordUpdateException(ErrorCode.USERNAME_EMAIL_MISMATCH);
		}

		String resetToken = UUID.randomUUID().toString();
		redisTemplate.opsForValue().set(
			PASSWORD_RESET_PREFIX + resetToken,
			email,
			RESET_TOKEN_TTL
		);

		String resetUrl = frontendUrl + "/reset-password/" + resetToken;

		try {
			sendPasswordResetMail(email, resetUrl);
			log.debug("비밀번호 재설정 이메일 발송 완료 - email: {}, userName: {}", email, userName);
		} catch (MessagingException e) {
			log.error("비밀번호 재설정 이메일 발송 실패 - email: {}", email, e);
			throw new PasswordUpdateException(ErrorCode.FAIL_EMAIL_SEND);
		}
	}

	@Transactional
	public void resetPassword(String resetToken, String newPassword, String newPasswordConfirm) {
		String email = redisTemplate.opsForValue().get(PASSWORD_RESET_PREFIX + resetToken);
		if (email == null) {
			throw new PasswordUpdateException(ErrorCode.INVALID_TOKEN);
		}

		if (!newPassword.equals(newPasswordConfirm)) {
			throw new PasswordUpdateException(ErrorCode.PASSWORD_MISMATCH);
		}

		UserEntity user = userRepository.findByEmail(email)
			.orElseThrow(() -> new PasswordUpdateException(ErrorCode.USER_NOT_FOUND));

		user.changePassword(passwordEncoder.encode(newPassword));
		userRepository.save(user);

		redisTemplate.delete(PASSWORD_RESET_PREFIX + resetToken);
		log.debug("비밀번호 재설정 완료 - email: {}", email);
	}

	@Transactional
	public void updatePassword(Integer userId, String currentPassword, String newPassword, String newPasswordConfirm) {
		UserEntity user = userRepository.findById(userId)
			.orElseThrow(() -> new PasswordUpdateException(ErrorCode.USER_NOT_FOUND));

		if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
			throw new PasswordUpdateException(ErrorCode.INVALID_PASSWORD);
		}

		if (!newPassword.equals(newPasswordConfirm)) {
			throw new PasswordUpdateException(ErrorCode.PASSWORD_MISMATCH);
		}

		user.changePassword(passwordEncoder.encode(newPassword));
		userRepository.save(user);
	}

	private void sendPasswordResetMail(String to, String resetUrl) throws MessagingException {
		MimeMessage message = mailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

		helper.setFrom(fromEmail);
		helper.setTo(to);
		helper.setSubject("[Bookiki] 비밀번호 재설정");
		helper.setText(
			"비밀번호 재설정을 위해 아래 링크를 클릭해주세요:\n\n" + resetUrl + "\n\n" +
				"본 링크는 10분간 유효하며, 1회만 사용 가능합니다.",
			false
		);

		mailSender.send(message);
	}
}