package server.api;

import commons.Event;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import server.database.ExpenseRepository;
import server.database.ParticipantRepository;

import java.util.Collections;

import static org.mockito.Mockito.*;

public class WebSocketControllerTest {

    @Mock
    private ParticipantRepository participantRepo;

    @Mock
    private ExpenseRepository expenseRepo;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    private WebSocketController webSocketController;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);
        webSocketController = new WebSocketController(participantRepo, expenseRepo, messagingTemplate);
    }

    @Test
    public void testSendUpdatedEvent() {
        Event event = new Event();
        String inviteCode = "TESTIN";
        webSocketController.sendUpdatedEvent(inviteCode, event);
        verify(messagingTemplate).convertAndSend(eq("/topic/event/" + inviteCode), eq(event));
    }

    @Test
    public void testSendUpdatedParticipants() {
        String inviteCode = "TESTIN";
        when(participantRepo.findByEventInviteCode(inviteCode)).thenReturn(java.util.Optional.of(Collections.emptyList()));
        webSocketController.sendUpdatedParticipants(inviteCode);
        verify(messagingTemplate).convertAndSend(eq("/topic/participants/" + inviteCode), anyList());
    }

    @Test
    public void testSendUpdatedExpenses() {
        Event event = new Event();
        when(expenseRepo.findByParentEvent(event)).thenReturn(java.util.Optional.of(Collections.emptyList()));
        webSocketController.sendUpdatedExpenses(event);
        verify(messagingTemplate).convertAndSend(eq("/topic/expenses/" + event.getInviteCode()), anyList());
    }
}