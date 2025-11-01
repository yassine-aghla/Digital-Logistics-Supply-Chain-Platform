package org.example.digitallogisticssupplychainplatform.service;

import lombok.RequiredArgsConstructor;
import org.example.digitallogisticssupplychainplatform.dto.LoginRequestDto;
import org.example.digitallogisticssupplychainplatform.dto.LoginResponseDto;
import org.example.digitallogisticssupplychainplatform.dto.UserDto;
import org.example.digitallogisticssupplychainplatform.dto.UserResponseDto;
import org.example.digitallogisticssupplychainplatform.entity.Role;
import org.example.digitallogisticssupplychainplatform.entity.User;
import org.example.digitallogisticssupplychainplatform.mapper.UserMapper;
import org.example.digitallogisticssupplychainplatform.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final UserService userService;
    private final UserMapper userMapper;

     public UserResponseDto registre(UserDto userDto){
         userDto.setRole(Role.CLIENT);
         UserResponseDto userRegistred=userService.createUser(userDto);
         return userRegistred;
     }
    public LoginResponseDto authenticate(LoginRequestDto loginRequest) {
        Optional<User> userOptional = userRepository.findByEmail(loginRequest.getEmail());

        if (userOptional.isEmpty()) {
            return LoginResponseDto.builder()
                    .message("Email ou mot de passe incorrect")
                    .build();
        }

        User user = userOptional.get();


        if (!user.isActive()) {
            return LoginResponseDto.builder()
                    .message("Votre compte est désactivé")
                    .build();
        }


        if (!user.getPassword().equals(loginRequest.getPassword())) {
            return LoginResponseDto.builder()
                    .message("Email ou mot de passe incorrect")
                    .build();
        }


        String sessionToken = generateSimpleToken();

        return LoginResponseDto.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .active(user.isActive())
                .message("Authentification réussie")
                .token(sessionToken)
                .build();
    }

    private String generateSimpleToken() {
        return "SESSION_" + UUID.randomUUID().toString() + "_" + System.currentTimeMillis();
    }

    public boolean validateToken(String token) {
        return token != null && token.startsWith("SESSION_");
    }


}