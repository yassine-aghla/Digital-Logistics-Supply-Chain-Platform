package org.example.digitallogisticssupplychainplatform.service;

import org.example.digitallogisticssupplychainplatform.dto.LoginRequestDto;
import org.example.digitallogisticssupplychainplatform.dto.LoginResponseDto;
import org.example.digitallogisticssupplychainplatform.dto.UserDto;
import org.example.digitallogisticssupplychainplatform.dto.UserResponseDto;
import org.example.digitallogisticssupplychainplatform.entity.Role;
import org.example.digitallogisticssupplychainplatform.entity.User;
import org.example.digitallogisticssupplychainplatform.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;


@ExtendWith(MockitoExtension.class)
@DisplayName("Tests - AuthService")
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserService userService;

    @Mock
    private SimpleSessionService sessionService;

    private AuthService authService;

    private User testUser;
    private UserDto testUserDto;
    private UserResponseDto testUserResponseDto;
    private LoginRequestDto testLoginRequest;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userRepository, userService, sessionService);

        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@test.com");
        testUser.setPassword("password123");
        testUser.setRole(Role.CLIENT);
        testUser.setActive(true);

        testUserDto = new UserDto();
        testUserDto.setUsername("testuser");
        testUserDto.setEmail("test@test.com");
        testUserDto.setPassword("password123");
        testUserDto.setRole(Role.CLIENT);
        testUserDto.setActive(true);

        testUserResponseDto = new UserResponseDto();
        testUserResponseDto.setId(1L);
        testUserResponseDto.setUsername("testuser");
        testUserResponseDto.setEmail("test@test.com");
        testUserResponseDto.setRole(Role.CLIENT);
        testUserResponseDto.setActive(true);

        testLoginRequest = new LoginRequestDto();
        testLoginRequest.setEmail("test@test.com");
        testLoginRequest.setPassword("password123");
    }

    // ============================================================
    // TEST: register
    // ============================================================

    @Test
    @DisplayName(" register - Enregistrer un nouvel utilisateur")
    void testRegisterSuccess() {
        when(userService.createUser(any(UserDto.class))).thenReturn(testUserResponseDto);

        UserResponseDto result = authService.registre(testUserDto);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("testuser", result.getUsername());
        verify(userService, times(1)).createUser(any(UserDto.class));
    }

    // ============================================================
    // TEST: authenticate - Success
    // ============================================================

    @Test
    @DisplayName("authenticate - Authentification réussie")
    void testAuthenticateSuccess() {
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(testUser));
        when(sessionService.createSession(testUser)).thenReturn("session-token-123");

        LoginResponseDto result = authService.authenticate(testLoginRequest);

        assertNotNull(result);
        assertEquals(1L, result.getUserId());
        assertEquals("testuser", result.getUsername());
        assertEquals("test@test.com", result.getEmail());
        assertEquals(Role.CLIENT, result.getRole());
        assertTrue(result.isActive());
        assertEquals("session-token-123", result.getToken());
        assertEquals("Authentification réussie", result.getMessage());
        verify(userRepository, times(1)).findByEmail("test@test.com");
        verify(sessionService, times(1)).createSession(testUser);
    }

    // ============================================================
    // TEST: authenticate - Errors
    // ============================================================

    @Test
    @DisplayName(" authenticate - Email non trouvé")
    void testAuthenticateEmailNotFound() {
        when(userRepository.findByEmail("unknown@test.com")).thenReturn(Optional.empty());

        LoginResponseDto result = authService.authenticate(
                new LoginRequestDto("unknown@test.com", "password123"));

        assertNotNull(result);
        assertEquals("Email ou mot de passe incorrect", result.getMessage());
        verify(userRepository, times(1)).findByEmail("unknown@test.com");
        verify(sessionService, never()).createSession(any());
    }

    @Test
    @DisplayName(" authenticate - Compte désactivé")
    void testAuthenticateAccountDisabled() {
        User disabledUser = new User();
        disabledUser.setId(1L);
        disabledUser.setEmail("test@test.com");
        disabledUser.setPassword("password123");
        disabledUser.setActive(false);

        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(disabledUser));

        LoginResponseDto result = authService.authenticate(testLoginRequest);

        assertNotNull(result);
        assertEquals("Votre compte est désactivé", result.getMessage());
        verify(userRepository, times(1)).findByEmail("test@test.com");
        verify(sessionService, never()).createSession(any());
    }

    @Test
    @DisplayName(" authenticate - Mot de passe incorrect")
    void testAuthenticateWrongPassword() {
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(testUser));

        LoginRequestDto wrongPasswordRequest = new LoginRequestDto();
        wrongPasswordRequest.setEmail("test@test.com");
        wrongPasswordRequest.setPassword("wrongpassword");

        LoginResponseDto result = authService.authenticate(wrongPasswordRequest);

        assertNotNull(result);
        assertEquals("Email ou mot de passe incorrect", result.getMessage());
        verify(userRepository, times(1)).findByEmail("test@test.com");
        verify(sessionService, never()).createSession(any());
    }

    // ============================================================
    // TEST: validateToken
    // ============================================================

    @Test
    @DisplayName(" validateToken - Token valide")
    void testValidateTokenValid() {
        when(sessionService.isValidToken("valid-token")).thenReturn(true);

        boolean result = authService.validateToken("valid-token");

        assertTrue(result);
        verify(sessionService, times(1)).isValidToken("valid-token");
    }

    @Test
    @DisplayName("validateToken - Token invalide")
    void testValidateTokenInvalid() {
        when(sessionService.isValidToken("invalid-token")).thenReturn(false);

        boolean result = authService.validateToken("invalid-token");

        assertFalse(result);
        verify(sessionService, times(1)).isValidToken("invalid-token");
    }

    // ============================================================
    // TEST: Multiple operations
    // ============================================================

    @Test
    @DisplayName("Opérations multiples - Enregistrer et authentifier")
    void testRegisterAndAuthenticate() {
        // Register
        when(userService.createUser(any(UserDto.class))).thenReturn(testUserResponseDto);
        UserResponseDto registered = authService.registre(testUserDto);
        assertNotNull(registered);

        // Authenticate
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(testUser));
        when(sessionService.createSession(testUser)).thenReturn("session-token-123");
        LoginResponseDto authenticated = authService.authenticate(testLoginRequest);
        assertNotNull(authenticated);
        assertEquals("session-token-123", authenticated.getToken());
    }

    @Test
    @DisplayName(" Opérations multiples - Authentifier et valider le token")
    void testAuthenticateAndValidateToken() {
        // Authenticate
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(testUser));
        when(sessionService.createSession(testUser)).thenReturn("session-token-123");
        LoginResponseDto authenticated = authService.authenticate(testLoginRequest);
        assertNotNull(authenticated);
        assertNotNull(authenticated.getToken());

        // Validate token
        when(sessionService.isValidToken("session-token-123")).thenReturn(true);
        boolean isValid = authService.validateToken("session-token-123");
        assertTrue(isValid);
    }

    @Test
    @DisplayName("Opérations multiples - Cas d'erreur complète")
    void testErrorFlow() {
        // Try to authenticate with wrong password
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(testUser));
        LoginResponseDto result1 = authService.authenticate(
                new LoginRequestDto("test@test.com", "wrongpassword"));
        assertEquals("Email ou mot de passe incorrect", result1.getMessage());

        // Try with disabled account
        User disabledUser = new User();
        disabledUser.setEmail("test@test.com");
        disabledUser.setActive(false);
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(disabledUser));
        LoginResponseDto result2 = authService.authenticate(testLoginRequest);
        assertEquals("Votre compte est désactivé", result2.getMessage());

        // Try with non-existent email
        when(userRepository.findByEmail("unknown@test.com")).thenReturn(Optional.empty());
        LoginResponseDto result3 = authService.authenticate(
                new LoginRequestDto("unknown@test.com", "password123"));
        assertEquals("Email ou mot de passe incorrect", result3.getMessage());
    }

    @Test
    @DisplayName("Opérations multiples - Admin vs Client roles")
    void testAuthenticateDifferentRoles() {
        // Client authentication
        when(userRepository.findByEmail("client@test.com")).thenReturn(Optional.of(testUser));
        when(sessionService.createSession(testUser)).thenReturn("client-token");
        LoginResponseDto clientResult = authService.authenticate(
                new LoginRequestDto("client@test.com", "password123"));
        assertEquals(Role.CLIENT, clientResult.getRole());

        // Admin authentication
        User adminUser = new User();
        adminUser.setId(2L);
        adminUser.setEmail("admin@test.com");
        adminUser.setPassword("adminpass");
        adminUser.setRole(Role.ADMIN);
        adminUser.setActive(true);

        when(userRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(adminUser));
        when(sessionService.createSession(adminUser)).thenReturn("admin-token");
        LoginResponseDto adminResult = authService.authenticate(
                new LoginRequestDto("admin@test.com", "adminpass"));
        assertEquals(Role.ADMIN, adminResult.getRole());
    }
}