package com.corp.bookiki.user;

import com.corp.bookiki.user.dto.UserSignUpRequest;
import com.corp.bookiki.user.entity.UserEntity;
import com.corp.bookiki.user.exception.DuplicateEmailException;
import com.corp.bookiki.user.exception.DuplicateCompanyIdException;
import com.corp.bookiki.user.repository.UserRepository;
import com.corp.bookiki.user.service.UserSignUpService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserSignUpServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserSignUpService userSignUpService;

    private UserSignUpRequest validSignUpRequest;

    @BeforeEach
    void setUp() {
        validSignUpRequest = new UserSignUpRequest();
        validSignUpRequest.setEmail("test@example.com");
        validSignUpRequest.setPassword("password123");
        validSignUpRequest.setUserName("Test User");
        validSignUpRequest.setCompanyId("C12345");
    }

    @Test
    @DisplayName("회원가입 성공 시나리오")
    void testSignUpSuccess() {
        when(userRepository.existsByEmail(validSignUpRequest.getEmail())).thenReturn(false);
        when(userRepository.existsByCompanyId(validSignUpRequest.getCompanyId())).thenReturn(false);

        userSignUpService.signUp(validSignUpRequest);

        ArgumentCaptor<UserEntity> userCaptor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userRepository).save(userCaptor.capture());

        UserEntity savedUser = userCaptor.getValue();
        assertEquals(validSignUpRequest.getEmail(), savedUser.getEmail());
        assertEquals(validSignUpRequest.getUserName(), savedUser.getUserName());
        assertEquals(validSignUpRequest.getCompanyId(), savedUser.getCompanyId());
    }

    @Test
    @DisplayName("중복 이메일로 회원가입 시 예외 발생")
    void testSignUpWithDuplicateEmail() {
        when(userRepository.existsByEmail(validSignUpRequest.getEmail())).thenReturn(true);

        DuplicateEmailException exception = assertThrows(DuplicateEmailException.class, () -> userSignUpService.signUp(validSignUpRequest));

        assertEquals("이미 사용 중인 이메일입니다.", exception.getMessage());

        verify(userRepository, never()).save(any(UserEntity.class));
    }

    @Test
    @DisplayName("중복 사번으로 회원가입 시 예외 발생")
    void testSignUpWithDuplicateCompanyId() {
        when(userRepository.existsByEmail(validSignUpRequest.getEmail())).thenReturn(false);
        when(userRepository.existsByCompanyId(validSignUpRequest.getCompanyId())).thenReturn(true);

        DuplicateCompanyIdException exception = assertThrows(DuplicateCompanyIdException.class, () -> userSignUpService.signUp(validSignUpRequest));

        assertEquals("이미 사용 중인 사번입니다.", exception.getMessage());

        verify(userRepository, never()).save(any(UserEntity.class));
    }

    @Test
    @DisplayName("이메일 중복 체크 - 중복된 이메일")
    void testCheckEmailDuplicate_DuplicateEmail() {
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        DuplicateEmailException exception = assertThrows(DuplicateEmailException.class, () -> userSignUpService.checkEmailDuplicate("duplicate@example.com"));

        assertEquals("이미 사용 중인 이메일입니다.", exception.getMessage());
    }

    @Test
    @DisplayName("이메일 중복 체크 - 사용 가능한 이메일")
    void testCheckEmailDuplicate_UniqueEmail() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);

        assertDoesNotThrow(() -> userSignUpService.checkEmailDuplicate("unique@example.com"));
    }

    @Test
    @DisplayName("사번 중복 체크 - 중복된 사번")
    void testCheckCompanyIdDuplicate_DuplicateCompanyId() {
        when(userRepository.existsByCompanyId(anyString())).thenReturn(true);

        DuplicateCompanyIdException exception = assertThrows(DuplicateCompanyIdException.class, () -> userSignUpService.checkEmployeeIdDuplicate("C12345"));

        assertEquals("이미 사용 중인 사번입니다.", exception.getMessage());
    }

    @Test
    @DisplayName("사번 중복 체크 - 사용 가능한 사번")
    void testCheckCompanyIdDuplicate_UniqueCompanyId() {
        when(userRepository.existsByCompanyId(anyString())).thenReturn(false);

        assertDoesNotThrow(() -> userSignUpService.checkEmployeeIdDuplicate("C12345"));
    }
}