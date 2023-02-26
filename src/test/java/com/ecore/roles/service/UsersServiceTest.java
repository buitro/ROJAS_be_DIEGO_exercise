package com.ecore.roles.service;

import com.ecore.roles.client.UsersClient;
import com.ecore.roles.client.model.User;
import com.ecore.roles.exception.ResourceNotFoundException;
import com.ecore.roles.service.impl.UsersServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static com.ecore.roles.utils.TestData.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsersServiceTest {

    @InjectMocks
    private UsersServiceImpl subject;
    @Mock
    private UsersClient usersClient;

    @Test
    void shouldGetUserWhenUserIdExists() {
        User expectedUser = GIANNI_USER();
        when(usersClient.getUser(GIANNI_USER_UUID))
                .thenReturn(ResponseEntity
                        .status(HttpStatus.OK)
                        .body(expectedUser));

        User user = subject.getUser(GIANNI_USER_UUID);
        assertNotNull(user);
        assertEquals(expectedUser, user);
        verify(usersClient, times(1)).getUser(any());
        verifyNoMoreInteractions(usersClient);
    }

    @Test
    public void shouldFailToGetUserWhenUserIdDoesNotExist() {
        when(usersClient.getUser(UUID_1)).thenReturn(ResponseEntity.status(HttpStatus.NOT_FOUND).body(null));

        ResourceNotFoundException exception =
                assertThrows(ResourceNotFoundException.class, () -> subject.getUser(UUID_1));

        assertEquals(String.format("User %s not found", UUID_1), exception.getMessage());
        verify(usersClient, times(1)).getUser(any());
        verifyNoMoreInteractions(usersClient);
    }

    @Test
    public void shouldGetUsers() {
        List<User> expectedUsers = List.of(GIANNI_USER());
        when(usersClient.getUsers()).thenReturn(ResponseEntity.status(HttpStatus.OK).body(expectedUsers));

        List<User> teams = subject.getUsers();

        assertNotNull(teams);
        assertEquals(expectedUsers, teams);
        verify(usersClient, times(1)).getUsers();
        verifyNoMoreInteractions(usersClient);
    }
}
