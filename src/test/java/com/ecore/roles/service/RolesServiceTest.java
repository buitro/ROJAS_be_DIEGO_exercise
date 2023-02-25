package com.ecore.roles.service;

import com.ecore.roles.client.model.Team;
import com.ecore.roles.client.model.User;
import com.ecore.roles.exception.ResourceExistsException;
import com.ecore.roles.exception.ResourceNotFoundException;
import com.ecore.roles.model.Membership;
import com.ecore.roles.model.Role;
import com.ecore.roles.repository.RoleRepository;
import com.ecore.roles.service.impl.RolesServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static com.ecore.roles.utils.TestData.*;
import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RolesServiceTest {

    @InjectMocks
    private RolesServiceImpl subject;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private MembershipsService membershipsService;

    @Test
    public void shouldCreateRole() {
        Role expectedRole = DEVELOPER_ROLE();
        when(roleRepository.save(expectedRole)).thenReturn(expectedRole);

        Role role = subject.createRole(expectedRole);

        assertNotNull(role);
        assertEquals(expectedRole, role);
        verify(roleRepository, times(1)).findByName(anyString());
        verify(roleRepository, times(1)).save(any());
        verifyNoMoreInteractions(roleRepository);
        verifyNoInteractions(membershipsService);
    }

    @Test
    public void shouldFailToCreateRoleWhenRoleIsNull() {
        assertThrows(NullPointerException.class,
                () -> subject.createRole(null));
        verifyNoInteractions(roleRepository, membershipsService);
    }

    @Test
    public void shouldFailToCreateRoleWhenItExists() {
        Role expectedRole = DEVELOPER_ROLE();
        when(roleRepository.findByName(expectedRole.getName())).thenReturn(Optional.of(expectedRole));

        ResourceExistsException exception = assertThrows(ResourceExistsException.class,
                () -> subject.createRole(expectedRole));

        assertEquals("Role already exists", exception.getMessage());
        verify(roleRepository, times(1)).findByName(anyString());
        verifyNoMoreInteractions(roleRepository);
        verifyNoInteractions(membershipsService);
    }

    @Test
    public void shouldReturnRoleWhenRoleIdExists() {
        Role developerRole = DEVELOPER_ROLE();
        when(roleRepository.findById(developerRole.getId())).thenReturn(Optional.of(developerRole));

        Role role = subject.getRole(developerRole.getId());

        assertNotNull(role);
        assertEquals(developerRole, role);
        verify(roleRepository, times(1)).findById(any());
        verifyNoMoreInteractions(roleRepository);
        verifyNoInteractions(membershipsService);
    }

    @Test
    public void shouldFailToGetRoleWhenRoleIdIsNull() {
        assertThrows(NullPointerException.class,
                () -> subject.getRole(null));
        verifyNoInteractions(roleRepository, membershipsService);
    }

    @Test
    public void shouldFailToGetRoleWhenRoleIdDoesNotExist() {
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> subject.getRole(UUID_1));

        assertEquals(format("Role %s not found", UUID_1), exception.getMessage());
        verify(roleRepository, times(1)).findById(any());
        verifyNoMoreInteractions(roleRepository);
        verifyNoInteractions(membershipsService);
    }

    @Test
    public void shouldReturnRoles() {
        List<Role> expectedRoles =
                List.of(DEVELOPER_ROLE(), PRODUCT_OWNER_ROLE(), TESTER_ROLE(), DEVOPS_ROLE());
        when(roleRepository.findAll()).thenReturn(expectedRoles);

        assertEquals(expectedRoles, subject.getRoles());
        verify(roleRepository, times(1)).findAll();
        verifyNoMoreInteractions(roleRepository);
        verifyNoInteractions(membershipsService);
    }

    @Test
    public void shouldReturnRoleByUserIdAndTeamIdWhenMembershipExists() {
        when(membershipsService.getMembership(GIANNI_USER_UUID, ORDINARY_CORAL_LYNX_TEAM_UUID))
                .thenReturn(DEFAULT_MEMBERSHIP());

        Role role = subject.getRole(GIANNI_USER_UUID, ORDINARY_CORAL_LYNX_TEAM_UUID);

        assertEquals(DEFAULT_MEMBERSHIP().getRole().getId(), role.getId());
        assertEquals(DEFAULT_MEMBERSHIP().getRole().getName(), role.getName());
        verify(membershipsService, times(1)).getMembership(any(), any());
        verifyNoMoreInteractions(membershipsService);
        verifyNoInteractions(roleRepository);
    }

    @Test
    public void shouldFailToGetRoleByUserIdAndTeamIdWhenTeamIdDoesNotExist() {
        when(membershipsService.getMembership(GIANNI_USER_UUID, UUID_1))
                .thenThrow(new ResourceNotFoundException(Team.class, UUID_1));

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> subject.getRole(GIANNI_USER_UUID, UUID_1));

        assertEquals(format("Team %s not found", UUID_1), exception.getMessage());
        verify(membershipsService, times(1)).getMembership(any(), any());
        verifyNoMoreInteractions(membershipsService);
        verifyNoInteractions(roleRepository);
    }

    @Test
    public void shouldFailToGetRoleByUserIdAndTeamIdWhenUserIdDoesNotExist() {
        when(membershipsService.getMembership(UUID_1, ORDINARY_CORAL_LYNX_TEAM_UUID))
                .thenThrow(new ResourceNotFoundException(User.class, UUID_1));

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> subject.getRole(UUID_1, ORDINARY_CORAL_LYNX_TEAM_UUID));

        assertEquals(format("User %s not found", UUID_1), exception.getMessage());
        verify(membershipsService, times(1)).getMembership(any(), any());
        verifyNoMoreInteractions(membershipsService);
        verifyNoInteractions(roleRepository);
    }

    @Test
    public void shouldFailToGetRoleByUserIdAndTeamIdWhenMembershipDoesNotExist() {
        when(membershipsService.getMembership(GIANNI_USER_UUID, ORDINARY_CORAL_LYNX_TEAM_UUID))
                .thenThrow(new ResourceNotFoundException(Membership.class, GIANNI_USER_UUID,
                        ORDINARY_CORAL_LYNX_TEAM_UUID));

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> subject.getRole(GIANNI_USER_UUID, ORDINARY_CORAL_LYNX_TEAM_UUID));

        assertEquals(format("Membership %s %s not found", GIANNI_USER_UUID, ORDINARY_CORAL_LYNX_TEAM_UUID),
                exception.getMessage());
        verify(membershipsService, times(1)).getMembership(any(), any());
        verifyNoMoreInteractions(membershipsService);
        verifyNoInteractions(roleRepository);
    }
}
