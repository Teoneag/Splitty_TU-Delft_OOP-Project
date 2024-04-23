package client.services;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class EventServiceTest {

    @InjectMocks
    private final EventService eventService = new EventService();


    @Test
    void titleCheck() {
    }

    @Test
    void descriptionCheck() {
    }

    @Test
    void applyValidation() {
    }

    @Test
    void validateTitleAndDesc() {
    }

    @Test
    void validateText() {
        String res = eventService.validateText("tt", 3, 6);
        assertEquals("Too short!", res);
    }

    @Test
    void validateText2() {
        String res = eventService.validateText("ttt", 1, 2);
        assertEquals("Too long!", res);
    }
}