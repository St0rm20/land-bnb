package com.labndbnb.landbnb.integration;

import com.labndbnb.landbnb.model.User;
import com.labndbnb.landbnb.model.enums.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(scripts = "/dataset.sql")
public class UserControllerIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void givenUser_whenUpdateProfile_thenReturnUpdatedUser() {

        User updatedUser = User.builder()
                .name("Helen Updated")
                .lastName("Giraldo Updated")
                .phoneNumber("3007654321")
                .role(UserRole.USER)
                .build();

        HttpEntity<User> requestEntity = new HttpEntity<>(updatedUser);

        ResponseEntity<User> response = restTemplate.exchange(
                "/api/users/profile",
                HttpMethod.PUT,
                requestEntity,
                User.class
        );

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isNotNull();

        User responseUser = response.getBody();

        assertThat(responseUser.getName()).isEqualTo("Helen Updated");
        assertThat(responseUser.getLastName()).isEqualTo("Giraldo Updated");
        assertThat(responseUser.getPhoneNumber()).isEqualTo("3007654321");
        assertThat(responseUser.getRole()).isEqualTo(UserRole.USER);
    }
}
