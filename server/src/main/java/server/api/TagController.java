package server.api;


import commons.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import server.database.EventRepository;
import server.database.TagRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tags")
public class TagController {

    private final TagRepository repo;
    private final EventRepository eventRepo;

    /**
     * Constructs a new TagController with the specified TagRepository and EventRepository.
     *
     * @param repo      The repository for managing tags.
     * @param eventRepo The repository for managing events.
     */
    public TagController(TagRepository repo, EventRepository eventRepo) {
        this.repo = repo;
        this.eventRepo = eventRepo;
    }

    /**
     * Retrieves all tags from the repository.
     *
     * @return A list of tags containing all the tags found in the repository.
     */
    @GetMapping(path = {"", "/"})
    public List<Tag> getAllTags(){
        return repo.findAll();
    }

    /**
     * Get all tags for a specific event
     *
     * @param eventInviteCode the invite code of the event
     * @return a list of tags
     */
    @GetMapping(path = "/event/{eventInviteCode}")
    public ResponseEntity<List<Tag>> getTagByEventInviteCode(@PathVariable("eventInviteCode") String eventInviteCode) {
        if(!eventRepo.existsById(eventInviteCode)){
            return ResponseEntity.notFound().build();
        }
        List<Tag> tags = repo.findAllByEventInviteCode(eventInviteCode);
        tags.remove(repo.findByNameAndEvent_InviteCode("Payment", eventInviteCode));
        return ResponseEntity.ok(tags);
    }

    /**
     * Get all tags for a specific event
     *
     * @param eventInviteCode the invite code of the event
     * @return a list of tags
     */
    @GetMapping(path = "/event/{eventInviteCode}/raw")
    public ResponseEntity<List<Tag>> getTagByEventInviteCodeRaw(
            @PathVariable("eventInviteCode") String eventInviteCode) {
        if(!eventRepo.existsById(eventInviteCode)){
            return ResponseEntity.notFound().build();
        }
        List<Tag> tags = repo.findAllByEventInviteCode(eventInviteCode);
        return ResponseEntity.ok(tags);
    }

    /**
     * Get a tag by id
     *
     * @param id the id of the tag
     * @return the tag
     */
    @GetMapping(path = "/{id}")
    public ResponseEntity<Tag> getTagById(@PathVariable("id") long id){
        if(id < 0 || !repo.existsById(String.valueOf(id))){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(repo.findById(String.valueOf(id)).orElse(null));
    }

    /**
     * get the payment tag for an event
     *
     * @param inviteCode of the event
     * @return the payment tag of the event
     */
    @GetMapping("/event/{inviteCode}/payment")
    public ResponseEntity<Tag> getPaymentTag(@PathVariable("inviteCode") String inviteCode) {
        if(!eventRepo.existsById(inviteCode)){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(repo.findByNameAndEvent_InviteCode("Payment", inviteCode));
    }

    /**
     * Add a tag
     *
     * @param tag the tag to add
     * @return the added tag
     */
    @PostMapping(path = {"", "/"})
    public ResponseEntity<Tag> addTag(@RequestBody Tag tag) {
        Tag saved = repo.save(tag);
        return ResponseEntity.ok(saved);
    }

    /**
     * Add a list of tags
     *
     * @param tags the tags to add
     * @return the added tags
     */
    @PostMapping("list")
    public Map<Long,Long> addTags(@RequestBody List<Tag> tags) {
        Map<Long, Long> tagMap = new HashMap<>();
        for(Tag tag: tags){
            Long prev = tag.getId();
            tag.setEvent(eventRepo.findById(tag.getEvent().getInviteCode()).orElse(null));
            Tag saved = repo.save(tag);
            tagMap.put(prev, saved.getId());
        }

        return tagMap;
    }

    /**
     * Update a tag
     *
     * @param tag the tag to update
     * @return the updated tag
     */
    @PutMapping(path = "/{id}")
    public ResponseEntity<Tag> updateTag(@RequestBody Tag tag){
        if(tag == null || !repo.existsById(String.valueOf(tag.getId()))) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(repo.save(tag));
    }

    /**
     * Delete a tag by id
     *
     * @param id the id of the tag
     * @return the deleted tag
     */
    @DeleteMapping(path = "/{id}")
    public ResponseEntity<Tag> deleteTagById(@PathVariable("id") long id){
        Tag deletedTag = repo.findById(String.valueOf(id)).orElse(null);
        repo.deleteById(String.valueOf(id));
        return ResponseEntity.ok(deletedTag);

    }

}
