package com.softwaremagico.kt.core.providers;

import com.softwaremagico.kt.core.exceptions.UserNotFoundException;
import com.softwaremagico.kt.persistence.entities.User;
import com.softwaremagico.kt.persistence.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserProvider {

    private final UserRepository userRepository;

    @Autowired
    public UserProvider(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User get(int id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(getClass(), "User with id '" + id + "' not found"));
    }
}
