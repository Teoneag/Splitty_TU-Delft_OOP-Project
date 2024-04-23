package server.api;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import server.services.RandomGeneratorService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminControllerTest {

    @InjectMocks
    private AdminController adminController;
    @Mock
    private RandomGeneratorService randomGeneratorService;

    @Test
    void adminLogin() {
        String p = "password";
        when(randomGeneratorService.validatePassword(p)).thenReturn(true);
        ResponseEntity<String> response = adminController.adminLogin(p);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void adminLoginFail() {
        String p = "password";
        when(randomGeneratorService.validatePassword(p)).thenReturn(false);
        ResponseEntity<String> response = adminController.adminLogin(p);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
}