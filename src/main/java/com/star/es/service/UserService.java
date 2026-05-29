package com.star.es.service;


import com.star.es.domain.dto.UserSearchDTO;
import com.star.es.domain.entity.User;
import java.util.Optional;
import org.springframework.data.domain.Page;

public interface UserService {
    User save(User user);
    Optional<User> findById(Long id);
    Page<User> findAll(int page, int size);
    void deleteById(Long id);
    Page<User> searchByConditions(UserSearchDTO searchDTO);
}
