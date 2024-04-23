package server.api;

import commons.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import server.database.EventRepository;
import server.database.TagRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

class TagControllerTest {

    @Mock
    private TagRepository tagRepository;
    @Mock
    private EventRepository eventRepository;
    @InjectMocks
    private TagController tagController;

    public TagControllerTest(){
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getAllTagsTest() {
        List<Tag> tags = new ArrayList<>();
        tags.add(new Tag());
        tags.add(new Tag());

        when(tagRepository.findAll()).thenReturn(tags);
        List<Tag> response = tagController.getAllTags();

        assertEquals(tags, response);
    }

    @Test
    void getTagByEventInviteCodeTest() {
        List<Tag> tags = new ArrayList<>();
        String inviteCode = "1234";
        tags.add(new Tag());
        tags.add(new Tag());

        when(eventRepository.existsById(inviteCode)).thenReturn(true);
        when(tagRepository.findAllByEventInviteCode(inviteCode)).thenReturn(tags);

        ResponseEntity<List<Tag>> response = tagController.getTagByEventInviteCode(inviteCode);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(tags, response.getBody());
    }

    @Test
    void getTagByEventInviteCode_NotExistsTest() {
        String inviteCode = "1234";
        List<Tag> tags = new ArrayList<>();
        tags.add(new Tag());
        tags.add(new Tag());

        when(eventRepository.existsById(inviteCode)).thenReturn(true);
        when(tagRepository.findAllByEventInviteCode(inviteCode)).thenReturn(tags);

        ResponseEntity<List<Tag>> response = tagController.getTagByEventInviteCode("123");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());

    }

    @Test
    void getTagByIdTest() {
        long id = 1;
        List<Tag> tags = new ArrayList<>();
        Tag newTag = new Tag();
        newTag.setId(id);
        tags.add(newTag);

        when(tagRepository.existsById(String.valueOf(id))).thenReturn(true);
        when(tagRepository.findById(String.valueOf(id))).thenReturn(Optional.of(newTag));

        ResponseEntity<Tag> response = tagController.getTagById(id);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(tags.getFirst(), response.getBody());
    }

    @Test
    void getTagsById_SmallerZeroTest() {
        long id = -1;
        List<Tag> tags = new ArrayList<>();
        Tag newTag = new Tag();
        newTag.setId(id);
        tags.add(newTag);

        when(tagRepository.existsById(String.valueOf(id))).thenReturn(true);
        when(tagRepository.findById(String.valueOf(id))).thenReturn(Optional.of(newTag));

        ResponseEntity<Tag> response = tagController.getTagById(id);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void getTagById_NotExistsTest() {
        long id = 1;
        List<Tag> tags = new ArrayList<>();
        Tag newTag = new Tag();
        newTag.setId(id);
        tags.add(newTag);

        when(tagRepository.existsById(String.valueOf(id))).thenReturn(true);
        when(tagRepository.findById(String.valueOf(id))).thenReturn(Optional.of(newTag));

        ResponseEntity<Tag> response = tagController.getTagById(2);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void getPaymentTagTest() {
        List<Tag> tags = new ArrayList<>();
        String inviteCode = "1234";
        String name = "Payment";
        Tag tag = new Tag();
        tag.setName(name);
        tags.add(tag);

        when(eventRepository.existsById(inviteCode)).thenReturn(true);
        when(tagRepository.findByNameAndEvent_InviteCode(name, inviteCode)).thenReturn(tag);

        ResponseEntity<Tag> response = tagController.getPaymentTag(inviteCode);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(tag, response.getBody());
    }

    @Test
    void getPaymentTag_NotExistsTest() {
        List<Tag> tags = new ArrayList<>();
        String inviteCode = "1234";
        String name = "Payment";
        Tag tag = new Tag();
        tag.setName(name);
        tags.add(tag);

        when(eventRepository.existsById(inviteCode)).thenReturn(true);
        when(tagRepository.findByNameAndEvent_InviteCode(name, inviteCode)).thenReturn(tag);

        ResponseEntity<Tag> response = tagController.getPaymentTag("123");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void addTagTest() {
        Tag tag = new Tag();
        when(tagRepository.save(tag)).thenReturn(tag);

        ResponseEntity<Tag> response = tagController.addTag(tag);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(tag, response.getBody());
    }

    @Test
    void updateTagTest() {
        long id = 1;
        Tag tag = new Tag();
        tag.setId(id);

        when(tagRepository.existsById(String.valueOf(tag.getId()))).thenReturn(true);
        when(tagRepository.save(tag)).thenReturn(tag);

        ResponseEntity<Tag> response = tagController.updateTag(tag);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(tag, response.getBody());
    }

    @Test
    void updateTag_IsNullTest() {
        when(tagRepository.existsById(anyString())).thenReturn(false);

        ResponseEntity<Tag> response = tagController.updateTag(null);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void updateTag_NotExistsTest() {
        long id = 1;
        Tag tag = new Tag();
        tag.setId(id);

        when(tagRepository.existsById(String.valueOf(2))).thenReturn(true);
        when(tagRepository.save(tag)).thenReturn(tag);

        ResponseEntity<Tag> response = tagController.updateTag(tag);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());

    }

    @Test
    void deleteTagByIdTest() {
        long id = 1;
        Tag tag = new Tag();
        tag.setId(id);

        when(tagRepository.existsById(String.valueOf(id))).thenReturn(true);
        when(tagRepository.findById(String.valueOf(id))).thenReturn(Optional.of(tag));

        ResponseEntity<Tag> response = tagController.deleteTagById(id);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(tag, response.getBody());
    }
}