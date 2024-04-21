package server.api;

import commons.Event;
import commons.Participant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.Mockito.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

@ExtendWith(MockitoExtension.class)
public class ParticipantControllerTest {
    private ParticipantController sut;
    private TestParticipantRepository repo;
    private TestEventRepository eventRepo;
    @Mock
    private WebSocketController webSocketController;
    @BeforeEach
    public void setup() {
        repo = new TestParticipantRepository();
        eventRepo = new TestEventRepository();
        sut = new ParticipantController(repo, eventRepo, webSocketController);
    }
    
    @Test
    public void cannotAddParticipantWithoutName() {
        Participant participant = new Participant();
        var actual = sut.add(participant);
        assertEquals(BAD_REQUEST, actual.getStatusCode());
        assertEquals(ResponseEntity.badRequest().build(), sut.add(null));
    }
    
    @Test
    public void testAddParticipant() {
        Participant participant = new Participant("john", "smith", "email", "iban", "bic", "BOOB69");
        sut.add(participant);
        assertTrue(repo.calledMethods.contains("save"));
        assertTrue(repo.participants.contains(participant));
    }
    
    @Test
    public void testGetAll() {
        Participant participant = new Participant("john", "smith", "email", "iban", "bic", "BOOB69");
        Participant participant2 = new Participant("john2", "smith2", "email2", "iban2", "bic2", "BOOB69");
        sut.add(participant);
        sut.add(participant2);
        List<Participant> expected = new ArrayList<>();
        expected.add(participant);
        expected.add(participant2);
        List<Participant> actual = sut.getAll();
        assertTrue(repo.calledMethods.contains("findAll"));
        assertEquals(expected, actual);
    }
    
    @Test
    public void testGetById() {
        Participant participant = new Participant("john", "smith", "email", "iban", "bic", "BOOB69");
        Participant participant2 = new Participant("john2", "smith2", "email2", "iban2", "bic2", "BOOB69");
        Participant participant3 = new Participant("john3", "smith3", "email3", "iban3", "bic3", "BOOB69");
        sut.add(participant);
        sut.add(participant2);
        sut.add(participant3);
        Participant actual = sut.getById(2).getBody();
        assertTrue(repo.calledMethods.contains("findById"));
        assertEquals(participant3, actual);
        assertEquals(ResponseEntity.badRequest().build(), sut.getById(-1));
        assertEquals(ResponseEntity.badRequest().build(), sut.getById(60));
    }
    
    @Test
    public void testDeleteById() {
        Participant participant = new Participant("john", "smith", "email", "iban", "bic", "BOOB69");
        Participant participant2 = new Participant("john2", "smith2", "email2", "iban2", "bic2", "BOOB69");
        sut.add(participant);
        sut.add(participant2);
        
        var actual = sut.deleteById(1);
        assertTrue(repo.calledMethods.contains("deleteById"));
        assertEquals(participant2, actual.getBody());
        assertFalse(repo.participants.contains(participant2));
        
        assertEquals(ResponseEntity.badRequest().build(), sut.deleteById(-1));
        
    }
    
    @Test
    public void testPostAddParticipant() {
        List<Participant> participants = new ArrayList<>();
        Set<Long> expectedResult = new HashSet<>();
        expectedResult.add(1L);
        expectedResult.add(2L);
        Participant participant = new Participant(1, "name1", "lastName1",
                "email1", "iban1", "bic1", "eventInviteCode");
        Participant participant2 = new Participant(2, "name2", "lastName2",
                "email2", "iban2", "bic2", "eventInviteCode");
        participants.add(participant);
        participants.add(participant2);
        assertEquals(sut.addParticipant(participants).keySet(), expectedResult);
        
        assertTrue(repo.calledMethods.contains("save"));
        assertTrue(repo.participants.contains(participant));
        assertTrue(repo.participants.contains(participant2));
    }
    
    @Test
    public void testGetEventById() {
        Event event1 = new Event("EVENT1", "title1", "desc1");
        Event event2 = new Event("EVENT2", "title2", "desc2");
        eventRepo.save(event1);
        eventRepo.save(event2);
        
        
        Participant participant = new Participant(1, "john", "smith", "email", "iban", "bic", "EVENT2");
        Participant participant2 = new Participant(2, "john2", "smith2", "email2", "iban2", "bic2", "EVENT2");
        Participant participant3 = new Participant(3, "john3", "smith3", "email3", "iban3", "bic3", "EVENT1");
        
        List<Participant> event2Participants = new ArrayList<>();
        event2Participants.add(participant);
        event2Participants.add(participant2);
        
        List<Participant> event1Participants = new ArrayList<>();
        event1Participants.add(participant3);
        
        sut.add(participant);
        sut.add(participant2);
        sut.add(participant3);
        
        ResponseEntity<List<Participant>> expectedEvent2 = ResponseEntity.ok(event2Participants);
        ResponseEntity<List<Participant>> expectedEvent1 = ResponseEntity.ok(event1Participants);
        ResponseEntity<List<Participant>> expectedEvent3 = ResponseEntity.badRequest().build();
        
        
        assertEquals(expectedEvent1, sut.getByEventId("EVENT1"));
        assertEquals(expectedEvent2, sut.getByEventId("EVENT2"));
        assertEquals(expectedEvent3, sut.getByEventId("EVENT3"));
        
        assertTrue(repo.calledMethods.contains("findByEventInviteCode"));
        assertTrue(eventRepo.calledMethods.contains("existsById"));
    }
    
    @Test
    public void testDeleteParticipants() {
        Participant participant = new Participant(1, "john", "smith", "email", "iban", "bic", "EVENT2");
        Participant participant2 = new Participant(2, "john2", "smith2", "email2", "iban2", "bic2", "EVENT2");
        Participant participant3 = new Participant(3, "john3", "smith3", "email3", "iban3", "bic3", "EVENT2");
        
        sut.add(participant);
        sut.add(participant2);
        sut.add(participant3);
        
        List<Participant> expectedDeleted = new ArrayList<>();
        expectedDeleted.add(participant);
        expectedDeleted.add(participant2);
        
        ResponseEntity<List<Participant>> expectedResponse = ResponseEntity.ok(expectedDeleted);
        assertEquals(expectedResponse, sut.deleteParticipants(expectedDeleted));
        assertTrue(repo.calledMethods.contains("deleteAll"));
        
    }
    
    @Test
    public void testUpdateParticipantBadRequests() {
        assertEquals(ResponseEntity.badRequest().build(), sut.update(1, null));
        assertEquals(ResponseEntity.badRequest().build(), sut.update(1, new Participant()));
        assertEquals(ResponseEntity.badRequest().build(), sut.update(1, new Participant(1, "", "last", "mail", "iban", "bic", "event1")));
        assertEquals(ResponseEntity.badRequest().build(), sut.update(1, new Participant(1, "first", "last", "mail", "iban", "bic", "event1")));
    }
    
    @Test
    public void testUpdateParticipant() {
        Participant participant = new Participant(1, "john", "smith", "email", "iban", "bic", "EVENT2");
        participant = sut.add(participant).getBody();
        
        ResponseEntity<Participant> expectedResponse = ResponseEntity.ok(participant);
        assert participant != null;
        assertEquals(expectedResponse, sut.update(participant.getId(), participant));
        verify(webSocketController, atLeastOnce()).sendUpdatedParticipants("EVENT2");
        
    }
    
    
    
}
