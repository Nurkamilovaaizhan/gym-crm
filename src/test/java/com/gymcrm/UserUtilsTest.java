package com.gymcrm.util;

import com.gymcrm.entity.Trainee;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class UserUtilsTest {

    @Test
    void generateUsernameShouldReturnBaseUsernameWhenNoDuplicates() {
        String username = UserUtils.generateUsername("Alan", "Walker", Set.of());

        assertEquals("Alan.Walker", username);
    }

    @Test
    void generateUsernameShouldAddSerialNumberWhenUsernameExists() {
        String username = UserUtils.generateUsername(
                "Alan",
                "Walker",
                Set.of("Alan.Walker", "Alan.Walker1", "Other.User")
        );

        assertEquals("Alan.Walker2", username);
    }

    @Test
    void generatePasswordShouldHaveLengthTen() {
        String password = UserUtils.generatePassword();

        assertEquals(10, password.length());
        assertFalse(password.isBlank());
    }

    @Test
    void setupCredentialsShouldFillUsernamePasswordAndActivateUser() {
        Trainee trainee = new Trainee();
        trainee.setFirstName("Alan");
        trainee.setLastName("Walker");

        UserUtils.setupCredentials(trainee, Set.of());

        assertEquals("Alan.Walker", trainee.getUsername());
        assertNotNull(trainee.getPassword());
        assertEquals(10, trainee.getPassword().length());
        assertTrue(trainee.isActive());
    }
}