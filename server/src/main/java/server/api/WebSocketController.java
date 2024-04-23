package server.api;


import commons.Event;

import commons.Expense;
import commons.Participant;
import org.springframework.beans.factory.annotation.Autowired;


import org.springframework.messaging.simp.SimpMessagingTemplate;

import org.springframework.stereotype.Controller;

import server.database.ExpenseRepository;
import server.database.ParticipantRepository;

import java.util.List;



@Controller
public class WebSocketController {
    private final SimpMessagingTemplate messagingTemplate;
    private final ParticipantRepository participantRepo;
    private final ExpenseRepository expenseRepo;

    /**
     * Constructor for WebSocketController
     * @param participantRepo participantRepo
     * @param expenseRepo expenseRepo
     * @param messagingTemplate messagingTemplate
     */
    @Autowired
    public WebSocketController(ParticipantRepository participantRepo, ExpenseRepository expenseRepo,
                               SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
        this.participantRepo = participantRepo;
        this.expenseRepo = expenseRepo;
    }

    /**
     * Sends an updated event to the clients
     * @param inviteCode the invite code of the event
     * @param event the event that was updated
     */
    public void sendUpdatedEvent(String inviteCode, Event event){
        messagingTemplate.convertAndSend("/topic/event/" + inviteCode, event);
    }

    /**
     * Sends an updated list of participants to the clients
     * @param inviteCode the invite code of the event
     */
    public void sendUpdatedParticipants(String inviteCode){
        List<Participant> participants = participantRepo.findByEventInviteCode(inviteCode).get();
        messagingTemplate.convertAndSend("/topic/participants/" + inviteCode, participants);
    }

    /**
     * Sends an updated list of expenses to the clients
     * @param event the event that was updated
     */
    public void sendUpdatedExpenses(Event event){
        List<Expense> expenses = expenseRepo.findByParentEvent(event).get();
        messagingTemplate.convertAndSend("/topic/expenses/" + event.getInviteCode(), expenses);
    }
}
