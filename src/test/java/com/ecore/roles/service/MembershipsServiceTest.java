package com.ecore.roles.service;

import com.ecore.roles.client.model.Team;
import com.ecore.roles.client.model.User;
import com.ecore.roles.exception.InvalidArgumentException;
import com.ecore.roles.exception.ResourceExistsException;
import com.ecore.roles.exception.ResourceNotFoundException;
import com.ecore.roles.model.Membership;
import com.ecore.roles.repository.MembershipRepository;
import com.ecore.roles.repository.RoleRepository;
import com.ecore.roles.service.impl.MembershipsServiceImpl;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MembershipsServiceTest {

    @InjectMocks
    private MembershipsServiceImpl subject;
    @Mock
    private MembershipRepository membershipRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private UsersService usersService;
    @Mock
    private TeamsService teamsService;

    @Test
    public void shouldCreateMembership() {
        Membership expectedMembership = DEFAULT_MEMBERSHIP();
        when(roleRepository.findById(expectedMembership.getRole().getId()))
                .thenReturn(Optional.ofNullable(DEVELOPER_ROLE()));
        when(teamsService.getTeam(expectedMembership.getTeamId()))
                .thenReturn(ORDINARY_CORAL_LYNX_TEAM());
        when(membershipRepository.findByUserIdAndTeamId(expectedMembership.getUserId(),
                expectedMembership.getTeamId()))
                        .thenReturn(Optional.empty());
        when(membershipRepository
                .save(expectedMembership))
                        .thenReturn(expectedMembership);

        Membership actualMembership = subject.assignRoleToMembership(expectedMembership);

        assertNotNull(actualMembership);
        assertEquals(actualMembership, expectedMembership);
        verify(roleRepository, times(1)).findById(any());
        verify(teamsService, times(1)).getTeam(any());
        verify(membershipRepository, times(1)).findByUserIdAndTeamId(any(), any());
        verify(membershipRepository, times(1)).save(any());
        verifyNoMoreInteractions(roleRepository, teamsService, membershipRepository);
        verifyNoInteractions(usersService);
    }

    @Test
    public void shouldFailToCreateMembershipWhenMembershipsIsNull() {
        assertThrows(NullPointerException.class,
                () -> subject.assignRoleToMembership(null));
        verifyNoInteractions(roleRepository, teamsService, membershipRepository, usersService);
    }

    @Test
    public void shouldFailToCreateMembershipWhenItHasInvalidRole() {
        Membership expectedMembership = DEFAULT_MEMBERSHIP();
        expectedMembership.setRole(null);

        InvalidArgumentException exception = assertThrows(InvalidArgumentException.class,
                () -> subject.assignRoleToMembership(expectedMembership));

        assertEquals("Invalid 'Role' object", exception.getMessage());
        verifyNoInteractions(roleRepository, teamsService, membershipRepository, usersService);
    }

    @Test
    public void shouldFailToCreateMembershipWhenRoleIdDoesNotExist() {
        Membership invalidMembership = INVALID_MEMBERSHIP();
        when(roleRepository.findById(invalidMembership.getRole().getId())).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> subject.assignRoleToMembership(invalidMembership));

        assertEquals(String.format("Role %s not found", invalidMembership.getRole().getId()),
                exception.getMessage());
        verify(roleRepository, times(1)).findById(any());
        verifyNoMoreInteractions(roleRepository);
        verifyNoInteractions(teamsService, membershipRepository, usersService);
    }

    @Test
    public void shouldFailToCreateMembershipWhenTeamIdDoesNotExists() {
        Membership membership = DEFAULT_MEMBERSHIP();
        when(roleRepository.findById(membership.getRole().getId()))
                .thenReturn(Optional.of(membership.getRole()));
        when(teamsService.getTeam(membership.getTeamId()))
                .thenThrow(new ResourceNotFoundException(Team.class, membership.getTeamId()));

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> subject.assignRoleToMembership(membership));

        assertEquals(String.format("Team %s not found", membership.getTeamId()), exception.getMessage());
        verify(roleRepository, times(1)).findById(any());
        verify(teamsService, times(1)).getTeam(any());
        verifyNoMoreInteractions(roleRepository, teamsService);
        verifyNoInteractions(membershipRepository, usersService);
    }

    @Test
    public void shouldFailToAssignRoleWhenMembershipIsInvalid() {
        Membership invalidMembership = INVALID_MEMBERSHIP();
        when(roleRepository.findById(invalidMembership.getRole().getId()))
                .thenReturn(Optional.of(invalidMembership.getRole()));
        when(teamsService.getTeam(invalidMembership.getTeamId())).thenReturn(ORDINARY_CORAL_LYNX_TEAM());

        InvalidArgumentException exception = assertThrows(InvalidArgumentException.class,
                () -> subject.assignRoleToMembership(invalidMembership));

        assertEquals("Invalid 'Membership' object. The provided user doesn't belong to the provided team.",
                exception.getMessage());
        verify(roleRepository, times(1)).findById(any());
        verify(teamsService, times(1)).getTeam(any());
        verifyNoMoreInteractions(roleRepository, teamsService);
        verifyNoInteractions(membershipRepository, usersService);
    }

    @Test
    public void shouldFailToCreateMembershipWhenItExists() {
        Membership expectedMembership = DEFAULT_MEMBERSHIP();
        when(roleRepository.findById(expectedMembership.getRole().getId()))
                .thenReturn(Optional.of(expectedMembership.getRole()));
        when(teamsService.getTeam(expectedMembership.getTeamId()))
                .thenReturn(ORDINARY_CORAL_LYNX_TEAM());
        when(membershipRepository.findByUserIdAndTeamId(expectedMembership.getUserId(),
                expectedMembership.getTeamId()))
                        .thenReturn(Optional.of(expectedMembership));

        ResourceExistsException exception = assertThrows(ResourceExistsException.class,
                () -> subject.assignRoleToMembership(expectedMembership));

        assertEquals("Membership already exists", exception.getMessage());
        verify(roleRepository, times(1)).findById(any());
        verify(teamsService, times(1)).getTeam(any());
        verify(membershipRepository, times(1)).findByUserIdAndTeamId(any(), any());
        verifyNoMoreInteractions(roleRepository, teamsService, membershipRepository);
        verifyNoInteractions(usersService);
    }

    @Test
    public void shouldFailToGetMembershipsWhenRoleIdIsNull() {
        assertThrows(NullPointerException.class,
                () -> subject.getMemberships(null));
        verifyNoInteractions(roleRepository, teamsService, membershipRepository, usersService);
    }

    @Test
    public void shouldGetMemberships() {
        Membership expectedMembership = DEFAULT_MEMBERSHIP();
        when(membershipRepository.findByRoleId(GIANNI_USER_UUID)).thenReturn(List.of(expectedMembership));

        List<Membership> memberships = subject.getMemberships(GIANNI_USER_UUID);

        assertEquals(List.of(expectedMembership), memberships);
        verify(membershipRepository, times(1)).findByRoleId(any());
        verifyNoMoreInteractions(membershipRepository);
        verifyNoInteractions(roleRepository, teamsService, usersService);
    }

    @Test
    public void shouldGetMembershipByUserIdAndTeamId() {
        when(usersService.getUser(GIANNI_USER_UUID)).thenReturn(GIANNI_USER());
        when(teamsService.getTeam(ORDINARY_CORAL_LYNX_TEAM_UUID)).thenReturn(ORDINARY_CORAL_LYNX_TEAM());
        when(membershipRepository.findByUserIdAndTeamId(GIANNI_USER_UUID, ORDINARY_CORAL_LYNX_TEAM_UUID))
                .thenReturn(Optional.of(DEFAULT_MEMBERSHIP()));

        Membership membership =
                subject.getMembership(GIANNI_USER_UUID, ORDINARY_CORAL_LYNX_TEAM_UUID);

        assertEquals(DEFAULT_MEMBERSHIP().getId(), membership.getId());
        assertEquals(DEFAULT_MEMBERSHIP().getUserId(), membership.getUserId());
        assertEquals(DEFAULT_MEMBERSHIP().getTeamId(), membership.getTeamId());
        verify(usersService, times(1)).getUser(any());
        verify(teamsService, times(1)).getTeam(any());
        verify(membershipRepository, times(1)).findByUserIdAndTeamId(any(), any());
        verifyNoMoreInteractions(usersService, teamsService, membershipRepository);
        verifyNoInteractions(roleRepository);
    }

    @Test
    public void shouldFailToGetRoleByUserIdAndTeamIdWhenTeamIdDoesNotExist() {
        when(usersService.getUser(GIANNI_USER_UUID)).thenReturn(GIANNI_USER());
        when(teamsService.getTeam(UUID_1))
                .thenThrow(new ResourceNotFoundException(Team.class, UUID_1));

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> subject.getMembership(GIANNI_USER_UUID, UUID_1));

        assertEquals(format("Team %s not found", UUID_1), exception.getMessage());
        verify(usersService, times(1)).getUser(any());
        verify(teamsService, times(1)).getTeam(any());
        verifyNoMoreInteractions(usersService, teamsService);
        verifyNoInteractions(roleRepository, membershipRepository);
    }

    @Test
    public void shouldFailToGetRoleByUserIdAndTeamIdWhenUserIdDoesNotExist() {
        when(usersService.getUser(UUID_1))
                .thenThrow(new ResourceNotFoundException(User.class, UUID_1));

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> subject.getMembership(UUID_1, ORDINARY_CORAL_LYNX_TEAM_UUID));

        assertEquals(format("User %s not found", UUID_1), exception.getMessage());
        verify(usersService, times(1)).getUser(any());
        verifyNoMoreInteractions(usersService);
        verifyNoInteractions(roleRepository, teamsService, membershipRepository);
    }

    @Test
    public void shouldFailToGetRoleByUserIdAndTeamIdWhenMembershipDoesNotExist() {
        when(usersService.getUser(GIANNI_USER_UUID)).thenReturn(GIANNI_USER());
        when(teamsService.getTeam(ORDINARY_CORAL_LYNX_TEAM_UUID)).thenReturn(ORDINARY_CORAL_LYNX_TEAM());
        when(membershipRepository.findByUserIdAndTeamId(GIANNI_USER_UUID, ORDINARY_CORAL_LYNX_TEAM_UUID))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> subject.getMembership(GIANNI_USER_UUID, ORDINARY_CORAL_LYNX_TEAM_UUID));

        assertEquals(format("Membership %s %s not found", GIANNI_USER_UUID, ORDINARY_CORAL_LYNX_TEAM_UUID),
                exception.getMessage());
        verify(usersService, times(1)).getUser(any());
        verify(teamsService, times(1)).getTeam(any());
        verify(membershipRepository, times(1)).findByUserIdAndTeamId(any(), any());
        verifyNoMoreInteractions(usersService, teamsService, membershipRepository);
        verifyNoInteractions(roleRepository);
    }
}
