package com.ecore.roles.service.impl;

import com.ecore.roles.client.TeamsClient;
import com.ecore.roles.client.model.Team;
import com.ecore.roles.exception.ResourceNotFoundException;
import com.ecore.roles.service.TeamsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TeamsServiceImpl implements TeamsService {

    private final TeamsClient teamsClient;

    public Team getTeam(UUID id) {
        Team team = teamsClient.getTeam(id).getBody();
        if (team == null) {
            throw new ResourceNotFoundException(Team.class, id);
        }
        return team;
    }

    public List<Team> getTeams() {
        return teamsClient.getTeams().getBody();
    }
}
