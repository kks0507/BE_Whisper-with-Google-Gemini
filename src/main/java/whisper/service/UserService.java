package whisper.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import whisper.dto.UserRequest;
import whisper.entity.UserEntity;
import whisper.repository.UserRepository;

@Service
public class UserService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    @Autowired
    public UserService(PasswordEncoder passwordEncoder, UserRepository userRepository) {
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
    }

    public void registerUser(UserRequest userRequest) {
        // Check if user with the given email already exists
        if (userRepository.findByEmail(userRequest.getEmail()) != null) {
            throw new RuntimeException("User with email " + userRequest.getEmail() + " already exists");
        }

        // If user does not exist, create new user
        UserEntity user = new UserEntity();
        user.setPassword(passwordEncoder.encode(userRequest.getPassword()));
        user.setEmail(userRequest.getEmail());
        user.setName(userRequest.getName());
        userRepository.save(user);
    }
}







