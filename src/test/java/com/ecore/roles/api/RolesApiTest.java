package com.ecore.roles.api;

import com.ecore.roles.model.Membership;
import com.ecore.roles.model.Role;
import com.ecore.roles.repository.RoleRepository;
import com.ecore.roles.utils.RestAssuredHelper;
import com.ecore.roles.web.dto.RoleDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

import static com.ecore.roles.utils.MockUtils.mockGetTeamById;
import static com.ecore.roles.utils.MockUtils.mockGetUserById;
import static com.ecore.roles.utils.RestAssuredHelper.*;
import static com.ecore.roles.utils.TestData.*;
import static io.restassured.RestAssured.when;
import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class RolesApiTest {

    private final RestTemplate restTemplate;
    private final RoleRepository roleRepository;

    private MockRestServiceServer mockServer;
    private ModelMapper modelMapper;

    @LocalServerPort
    private int port;

    @Autowired
    public RolesApiTest(RestTemplate restTemplate, RoleRepository roleRepository) {
        this.restTemplate = restTemplate;
        this.roleRepository = roleRepository;
    }

    @BeforeEach
    public void setUp() {
        mockServer = MockRestServiceServer.createServer(restTemplate);
        RestAssuredHelper.setUp(port);
        Optional<Role> devOpsRole = roleRepository.findByName(DEVOPS_ROLE().getName());
        devOpsRole.ifPresent(roleRepository::delete);
        modelMapper = new ModelMapper();
    }

    @Test
    public void shouldFailWhenPathDoesNotExist() {
        sendRequest(when()
                .get("/v1/role")
                .then())
                        .validate(404, "Not Found");
    }

    @Test
    public void shouldCreateNewRole() {
        Role expectedRole = DEVOPS_ROLE();

        RoleDto actualRole = createRole(expectedRole)
                .statusCode(201)
                .extract().as(RoleDto.class);

        assertThat(actualRole.getName()).isEqualTo(expectedRole.getName());
    }

    @Test
    public void shouldFailToCreateNewRoleWhenNull() {
        createRole(null)
                .validate(400, "Bad Request");
    }

    @Test
    public void shouldFailToCreateNewRoleWhenMissingName() {
        createRole(Role.builder().build())
                .validate(400, "Bad Request");
    }

    @Test
    public void shouldFailToCreateNewRoleWhenBlankName() {
        createRole(Role.builder().name("").build())
                .validate(400, "Bad Request");
    }

    @Test
    public void shouldFailToCreateNewRoleWhenNameAlreadyExists() {
        createRole(DEVELOPER_ROLE())
                .validate(400, "Role already exists");
    }

    @Test
    public void shouldGetAllRoles() {
        RoleDto[] roles = getRoles()
                .extract().as(RoleDto[].class);

        assertThat(roles.length).isGreaterThanOrEqualTo(3);
        assertThat(roles).contains(modelMapper.map(DEVELOPER_ROLE(), RoleDto.class));
        assertThat(roles).contains(modelMapper.map(PRODUCT_OWNER_ROLE(), RoleDto.class));
        assertThat(roles).contains(modelMapper.map(TESTER_ROLE(), RoleDto.class));
    }

    @Test
    public void shouldGetRoleById() {
        Role expectedRole = DEVELOPER_ROLE();

        getRole(expectedRole.getId())
                .statusCode(200)
                .body("name", equalTo(expectedRole.getName()));
    }

    @Test
    public void shouldFailToGetRoleById() {
        getRole(UUID_1)
                .validate(404, format("Role %s not found", UUID_1));
    }

    @Test
    public void shouldGetRoleByUserIdAndTeamId() {
        Membership expectedMembership = DEFAULT_MEMBERSHIP();
        mockGetTeamById(mockServer, ORDINARY_CORAL_LYNX_TEAM_UUID, ORDINARY_CORAL_LYNX_TEAM());
        mockGetUserById(mockServer, GIANNI_USER_UUID, GIANNI_USER());
        createMembership(expectedMembership).statusCode(201);

        getRole(expectedMembership.getUserId(), expectedMembership.getTeamId())
                .statusCode(200)
                .body("name", equalTo(expectedMembership.getRole().getName()));
    }

    @Test
    public void shouldFailToGetRoleByUserIdAndTeamIdWhenMissingUserId() {
        getRole(null, ORDINARY_CORAL_LYNX_TEAM_UUID)
                .validate(400, "Bad Request");
    }

    @Test
    public void shouldFailToGetRoleByUserIdAndTeamIdWhenMissingTeamId() {
        getRole(GIANNI_USER_UUID, null)
                .validate(400, "Bad Request");
    }

    @Test
    public void shouldFailToGetRoleByUserIdAndTeamIdWhenTeamDoesNotExist() {
        mockGetUserById(mockServer, GIANNI_USER_UUID, GIANNI_USER());
        mockGetTeamById(mockServer, UUID_1, null);

        getRole(GIANNI_USER_UUID, UUID_1)
                .validate(404, format("Team %s not found", UUID_1));
    }

    @Test
    public void shouldFailToGetRoleByUserIdAndTeamIdWhenUserDoesNotExist() {
        mockGetUserById(mockServer, UUID_1, null);

        getRole(UUID_1, ORDINARY_CORAL_LYNX_TEAM_UUID)
                .validate(404, format("User %s not found", UUID_1));
    }

    @Test
    public void shouldFailToGetRoleByUserIdAndTeamIdWhenMembershipDoesNotExist() {
        mockGetUserById(mockServer, UUID_1, GIANNI_USER());
        mockGetTeamById(mockServer, UUID_2, ORDINARY_CORAL_LYNX_TEAM());

        getRole(UUID_1, UUID_2)
                .validate(404,
                        format("Membership %s %s not found", UUID_1, UUID_2));
    }
}
