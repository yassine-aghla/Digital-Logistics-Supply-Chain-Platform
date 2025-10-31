package org.example.digitallogisticssupplychainplatform.service;


import lombok.RequiredArgsConstructor;
import org.example.digitallogisticssupplychainplatform.dto.UserDto;
import org.example.digitallogisticssupplychainplatform.dto.UserResponseDto;
import org.example.digitallogisticssupplychainplatform.entity.User;
import org.example.digitallogisticssupplychainplatform.mapper.UserMapper;
import org.example.digitallogisticssupplychainplatform.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository repo;
    private final UserMapper userMapper;

    public UserResponseDto createUser(UserDto userDto){

        if(repo.existsByEmail(userDto.getEmail())){
            throw new RuntimeException("Un utilisateur avec ce email est deja existe");
        }

        User user= userMapper.toEntity(userDto);
        User savedUser=repo.save(user);

        return userMapper.toResponseDto(savedUser);
    }

    public List<UserResponseDto>getAllUsers(){
        return repo.findAll().stream().map(userMapper::toResponseDto).toList();
    }

    public void deleteUser(Long id){
        repo.deleteById(id);
    }

    public UserResponseDto getById(Long id){
        return repo.findById(id).map(userMapper::toResponseDto).orElseThrow(()->new RuntimeException("utilisateur non trouve"));
    }
    public UserResponseDto updateUser(Long id,UserDto user){
        User existingUser=repo.findById(id).orElseThrow(()->new RuntimeException("utilisateur non trouvee"));

        if(repo.existsByEmailAndIdNot(user.getEmail(),id)){
            throw new RuntimeException("utilisteur avec cette email existe deja ");
        }
        existingUser.setUsername(user.getUsername());
        existingUser.setEmail(user.getEmail());
        existingUser.setPassword(user.getPassword());
        existingUser.setRole(user.getRole());
        existingUser.setActive(user.getActive());

        User updatedUser=repo.save(existingUser);
        return userMapper.toResponseDto(updatedUser);
    }
}
