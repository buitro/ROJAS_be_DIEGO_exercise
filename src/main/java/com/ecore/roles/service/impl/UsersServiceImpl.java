package com.ecore.roles.service.impl;

import com.ecore.roles.client.UsersClient;
import com.ecore.roles.client.model.User;
import com.ecore.roles.exception.ResourceNotFoundException;
import com.ecore.roles.service.UsersService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UsersServiceImpl implements UsersService {

    private final UsersClient usersClient;

    public User getUser(UUID id) {
        User user = usersClient.getUser(id).getBody();
        if (user == null) {
            throw new ResourceNotFoundException(User.class, id);
        }
        return user;
    }

    public List<User> getUsers() {
        return usersClient.getUsers().getBody();
    }
}
