package com.corp.bookiki.user.service;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.corp.bookiki.global.error.code.ErrorCode;
import com.corp.bookiki.global.error.exception.EmailAuthenticationException;
import com.corp.bookiki.user.repository.UserRepository;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailVerificationService {

	private static final String EMAIL_CODE_PREFIX = "EMAIL_CODE:";
	private static final String VERIFIED_EMAIL_PREFIX = "VERIFIED_EMAIL:";
	private static final Duration CODE_EXPIRATION = Duration.ofMinutes(5);

	private final StringRedisTemplate redisTemplate;
	private final JavaMailSender mailSender;
	private final UserRepository userRepository;

	@Value("${spring.mail.username}")
	private String fromEmail;

	public void sendVerificationCode(String email) {
		if (userRepository.existsByEmail(email)) {
			throw new EmailAuthenticationException(ErrorCode.DUPLICATE_EMAIL);
		}

		String code = generateVerificationCode();
		saveVerificationCode(email, code);

		try {
			sendEmail(email, code);
		} catch (MessagingException e) {
			throw new EmailAuthenticationException(ErrorCode.FAIL_EMAIL_SEND);
		}
	}

	public void verifyCode(String email, String code) {
		String savedCode = redisTemplate.opsForValue().get(EMAIL_CODE_PREFIX + email);

		if (savedCode == null || !savedCode.equals(code)) {
			throw new EmailAuthenticationException(ErrorCode.INVALID_EMAIL_VERIFICATION);
		}

		markEmailAsVerified(email);
		redisTemplate.delete(EMAIL_CODE_PREFIX + email);
	}

	public boolean isEmailVerified(String email) {
		String value = redisTemplate.opsForValue().get(VERIFIED_EMAIL_PREFIX + email);
		return Boolean.TRUE.toString().equals(value);
	}

	private String generateVerificationCode() {
		return String.format("%06d", (int)(Math.random() * 1000000));
	}

	private void saveVerificationCode(String email, String code) {
		redisTemplate.opsForValue().set(
			EMAIL_CODE_PREFIX + email,
			code,
			CODE_EXPIRATION
		);
	}

	private void markEmailAsVerified(String email) {
		redisTemplate.opsForValue().set(
			VERIFIED_EMAIL_PREFIX + email,
			Boolean.TRUE.toString(),
			CODE_EXPIRATION
		);
	}

	private void sendEmail(String to, String code) throws MessagingException {
		MimeMessage message = mailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

		helper.setFrom(fromEmail);
		helper.setTo(to);
		helper.setSubject("[Bookiki] 이메일 인증번호");
		helper.setText("인증번호: " + code + "\n\n해당 인증번호는 5분간 유효합니다.", false);

		mailSender.send(message);
	}
}