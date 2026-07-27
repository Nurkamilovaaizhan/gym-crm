package com.gymcrm.util;

import com.gymcrm.entity.User;

import java.security.SecureRandom;
import java.util.Set;

public final class UserUtils {

    private static final String CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    private UserUtils() {
    }

    public static String generatePassword() {
        StringBuilder password = new StringBuilder(10);

        for (int i = 0; i < 10; i++) {
            password.append(CHARS.charAt(RANDOM.nextInt(CHARS.length())));
        }

        return password.toString();
    }

    public static String generateUsername(String firstName, String lastName, Set<String> existingUsernames) {
        String baseUsername = firstName.trim() + "." + lastName.trim();

        long sameBaseCount = existingUsernames.stream()
                .filter(username -> username.replaceAll("\\d+$", "").equals(baseUsername))
                .count();

        return sameBaseCount == 0 ? baseUsername : baseUsername + sameBaseCount;
    }

    public static void setupCredentials(User user, Set<String> existingUsernames) {
        user.setUsername(generateUsername(user.getFirstName(), user.getLastName(), existingUsernames));
        user.setPassword(generatePassword());
        user.setActive(true);
    }
}