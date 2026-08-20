package com.gymcrm;

import com.gymcrm.entity.User;
import com.gymcrm.util.UserUtils;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class UserUtilsTest {

    @Test
    void generatePassword_shouldReturn10Characters() {
        String password = UserUtils.generatePassword();

        assertNotNull(password);
        assertEquals(10, password.length());
    }

    @Test
    void generateUsername_shouldReturnBaseUsernameWhenNoDuplicates() {
        String username = UserUtils.generateUsername("Alan", "Walker", Set.of());

        assertEquals("Alan.Walker", username);
    }

    @Test
    void generateUsername_shouldAppendSerialNumberWhenDuplicateExists() {
        String username = UserUtils.generateUsername(
                "Alan",
                "Walker",
                Set.of("Alan.Walker", "Alan.Walker1")
        );

        assertEquals("Alan.Walker2", username);
    }

    @Test
    void setupCredentials_shouldFillUsernamePasswordAndActive() {
        User user = new User() {};
        user.setFirstName("Max");
        user.setLastName("Verstappen");

        UserUtils.setupCredentials(user, Set.of());

        assertEquals("Max.Verstappen", user.getUsername());
        assertNotNull(user.getPassword());
        assertEquals(10, user.getPassword().length());
        assertTrue(user.isActive());
    }
}