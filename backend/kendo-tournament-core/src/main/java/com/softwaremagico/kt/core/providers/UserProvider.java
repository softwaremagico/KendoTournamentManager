package com.softwaremagico.kt.core.providers;

import com.softwaremagico.kt.core.exceptions.UserNotFoundException;
import com.softwaremagico.kt.persistence.entities.User;
import com.softwaremagico.kt.persistence.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

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


    public List<User> getAll() {
        return userRepository.findAll();
    }

    public User add(User user) {
        return userRepository.save(user);
    }

    public User update(User user) {
        if (user.getId() == null) {
            throw new UserNotFoundException(getClass(), "User with null id does not exists.");
        }
        return userRepository.save(user);
    }

    public void delete(User user) {
        userRepository.delete(user);
    }

    public void delete(Integer id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
        } else {
            throw new UserNotFoundException(getClass(), "User with id '" + id + "' not found");
        }
    }
}
