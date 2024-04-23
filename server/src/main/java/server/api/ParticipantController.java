package server.api;

import commons.Participant;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import server.database.EventRepository;
import server.database.ParticipantRepository;

import java.util.*;

@RestController
@RequestMapping("/api/participants")
public class ParticipantController {
    private final EventRepository eventRepo;
    private final ParticipantRepository participantRepo;
    private final WebSocketController websocketController;

    /**
     * Constructor for the ParticipantController
     * @param participantRepo - the repository for the participants
     * @param eventRepo - the repository for the events
     * @param websocketController - the web socket controller
     */
    public ParticipantController(ParticipantRepository participantRepo, EventRepository eventRepo,
                                 WebSocketController websocketController) {
        this.participantRepo = participantRepo;
        this.eventRepo = eventRepo;
        this.websocketController = websocketController;
    }
    
    /**
     * Gets all the Participant in the database
     * @return - All Participant that exist
     */
    @GetMapping(path= {"", "/"})
    public List<Participant> getAll() {
        return participantRepo.findAll();
    }
    
    /**
     * Gets a Participant by id
     * @param id - the id requested
     * @return ResponseEntity with the Participant
     */
    @GetMapping("/{id}")
    public ResponseEntity<Participant> getById(@PathVariable("id") long id) {
        if (id < 0 || !participantRepo.existsById(id)) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(participantRepo.findById(id).orElse(null));
    }
    
    /**
     * Gets a List of Participant based on the event they are part of
     * @param eventInviteCode - the invite code of the event
     * @return - the list of Participant part of that event
     */
    @GetMapping("/event/{eventInviteCode}")
    public ResponseEntity<List<Participant>> getByEventId(@PathVariable("eventInviteCode") String eventInviteCode) {
        if (!eventRepo.existsById(eventInviteCode)) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(participantRepo.findByEventInviteCode(eventInviteCode).orElse(new ArrayList<>()));
        
        
    }
    
    /**
     * Adds a participant to the repository
     * @param participant - the participant to add
     * @return - the participant that has been added with any variables that might have been updated
     */
    @PostMapping(path = {"", "/"})
    public ResponseEntity<Participant> add(@RequestBody Participant participant) {

        if (participant == null || isNullOrEmpty(participant.getFirstName())) {
            return ResponseEntity.badRequest().build();
        }

        Participant saved = participantRepo.save(participant);
        websocketController.sendUpdatedParticipants(saved.getEventInviteCode());
        return ResponseEntity.ok(saved);
    }
    /**
     * Adds a list of participants to the repository
     * @param participants - the list of participants to add
     * @return - the list of participants that have been added with any variables that might have been updated
     */
    @PostMapping("/list")
    public Map<Long, Long> addParticipant(@RequestBody List<Participant> participants) {
        Map<Long, Long> oldToNewParticipantIdMap = new HashMap<>();
        for (Participant participant : participants) {
            Long participantId = participant.getId();
            Participant saved = participantRepo.save(participant);
            oldToNewParticipantIdMap.put(participantId, saved.getId());
        }
        return oldToNewParticipantIdMap;
    }
    
    /**
     * Updates a participant with the new given details
     * @param participantId The id of the participant
     * @param participant The participant with the updates
     * @return The server response (updated participant or a badRequest)
     */
    @PutMapping(path = {"/{participantId}"})
    public ResponseEntity<Participant> update(@PathVariable("participantId") long participantId,
                                              @RequestBody Participant participant) {
        
        if (participant == null || participantId != participant.getId() || isNullOrEmpty(participant.getFirstName())
                || !participantRepo.existsById(participantId)) {
            return ResponseEntity.badRequest().build();
        }
        ResponseEntity<Participant> result = ResponseEntity.ok(participantRepo.save(participant));
        if(result.getStatusCode().is2xxSuccessful()) {
            websocketController.sendUpdatedParticipants(participant.getEventInviteCode());
        }
        return result;
    }

    private boolean isNullOrEmpty(String s) {
        return s == null || s.isEmpty();
    }
    
    /**
     * Deletes a Participant from the database given the id
     * @param id - the id of the participant to delete
     * @return - the participant that has been deleted
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Participant> deleteById(@PathVariable("id") long id) {
        if (id < 0 || !participantRepo.existsById(id)) {
            return ResponseEntity.badRequest().build();
        }
        Participant deleted = participantRepo.findById(id).orElse(null);
        participantRepo.deleteById(id);
        websocketController.sendUpdatedParticipants(Objects.requireNonNull(deleted).getEventInviteCode());
        return ResponseEntity.ok(deleted);
        
    }
    @DeleteMapping("/list")
    public ResponseEntity<List<Participant>> deleteParticipants(@RequestBody List<Participant> participants) {
        participantRepo.deleteAll(participants);
        return ResponseEntity.ok(participants);
    }
    
}
