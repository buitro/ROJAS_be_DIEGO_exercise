package com.ecore.roles.web.rest;

import com.ecore.roles.model.Role;
import com.ecore.roles.service.RolesService;
import com.ecore.roles.web.RolesApi;
import com.ecore.roles.web.dto.RoleDto;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/v1/roles")
public class RolesRestController implements RolesApi {

    private final RolesService rolesService;
    private final ModelMapper modelMapper;

    @Override
    @PostMapping(
            consumes = {"application/json"},
            produces = {"application/json"})
    public ResponseEntity<RoleDto> createRole(
            @Valid @RequestBody RoleDto role) {
        Role fromDto = modelMapper.map(role, Role.class);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(modelMapper.map(rolesService.createRole(fromDto), RoleDto.class));
    }

    @Override
    @GetMapping(
            produces = {"application/json"})
    public ResponseEntity<List<RoleDto>> getRoles() {

        List<Role> getRoles = rolesService.getRoles();

        List<RoleDto> roleDtoList = new ArrayList<>();

        for (Role role : getRoles) {
            RoleDto roleDto = modelMapper.map(role, RoleDto.class);
            roleDtoList.add(roleDto);
        }

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(roleDtoList);
    }

    @Override
    @GetMapping(
            path = "/{roleId}",
            produces = {"application/json"})
    public ResponseEntity<RoleDto> getRole(
            @PathVariable UUID roleId) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(modelMapper.map(rolesService.getRole(roleId), RoleDto.class));
    }

    @GetMapping(
            path = "/search",
            produces = {"application/json"})
    public ResponseEntity<RoleDto> getRole(@RequestParam UUID teamMemberId, @RequestParam UUID teamId) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(modelMapper.map(rolesService.getRole(teamMemberId, teamId), RoleDto.class));
    }
}
