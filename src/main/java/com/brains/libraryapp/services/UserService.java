package com.brains.libraryapp.services;


import java.util.List;

import com.brains.libraryapp.models.User;

public interface UserService {

    User save(User user);
    List<User> findAll();
    void delete(Long id);
    Long getUserId();
    User getLoggedInUser();
    boolean isUser();
    boolean isAdmin();
    User findOneUser(Long id);
}
