package com.ecore.roles.service;

import com.ecore.roles.client.TeamsClient;
import com.ecore.roles.client.model.Team;
import com.ecore.roles.exception.ResourceNotFoundException;
import com.ecore.roles.service.impl.TeamsServiceImpl;
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
class TeamsServiceTest {

    @InjectMocks
    private TeamsServiceImpl subject;
    @Mock
    private TeamsClient teamsClient;

    @Test
    public void shouldGetTeamWhenTeamIdExists() {
        Team expectedTeam = ORDINARY_CORAL_LYNX_TEAM();
        when(teamsClient.getTeam(ORDINARY_CORAL_LYNX_TEAM_UUID))
                .thenReturn(ResponseEntity
                        .status(HttpStatus.OK)
                        .body(expectedTeam));

        Team team = subject.getTeam(ORDINARY_CORAL_LYNX_TEAM_UUID);

        assertNotNull(team);
        assertEquals(expectedTeam, team);
        verify(teamsClient, times(1)).getTeam(any());
        verifyNoMoreInteractions(teamsClient);
    }

    @Test
    public void shouldFailToGetTeamWhenTeamIdDoesNotExist() {
        when(teamsClient.getTeam(UUID_1)).thenReturn(ResponseEntity.status(HttpStatus.NOT_FOUND).body(null));

        ResourceNotFoundException exception =
                assertThrows(ResourceNotFoundException.class, () -> subject.getTeam(UUID_1));

        assertEquals(String.format("Team %s not found", UUID_1), exception.getMessage());
        verify(teamsClient, times(1)).getTeam(any());
        verifyNoMoreInteractions(teamsClient);
    }

    @Test
    public void shouldGetTeams() {
        List<Team> expectedTeams = List.of(ORDINARY_CORAL_LYNX_TEAM());
        when(teamsClient.getTeams()).thenReturn(ResponseEntity.status(HttpStatus.OK).body(expectedTeams));

        List<Team> teams = subject.getTeams();

        assertNotNull(teams);
        assertEquals(expectedTeams, teams);
        verify(teamsClient, times(1)).getTeams();
        verifyNoMoreInteractions(teamsClient);
    }
}
