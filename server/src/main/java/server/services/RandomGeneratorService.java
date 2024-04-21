package server.services;

import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Random;

@Service
public class RandomGeneratorService {

    private String generatedPassword;

    /**
     * Generate a random password for the admin when the server starts, which can be used
     * to log in to the admin panel.
     *
     * @return random generated password
     */
    public synchronized String generatePassword() {
        if (generatedPassword == null) {
            // Generate a random password (or however you want to generate it)
            generatedPassword = createRandomCode(6);
        }
        return generatedPassword;
    }

    /**
     * Validate the password given by the user.
     *
     * @param password input password of user
     * @return true if the password is correct, false otherwise
     */
    public boolean validatePassword(String password) {
        return password.equals(generatedPassword);
    }

    /**
     * Create a random password for the admin when the server starts, which can be used
     * to log in to the admin panel.
     *
     * @param codeLength length of the password
     * @return random generated password
     */
    public String createRandomCode(int codeLength) {
        char[] chars = "abcdefghijklmnopqrstuvwxyz1234567890".toCharArray();
        StringBuilder sb = new StringBuilder();
        Random random = new SecureRandom();
        for (int i = 0; i < codeLength; i++) {
            char c = chars[random.nextInt(chars.length)];
            sb.append(c);
        }
        return sb.toString();
    }
}
