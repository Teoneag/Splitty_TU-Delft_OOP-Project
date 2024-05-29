package server.services;

import commons.Event;
import commons.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import server.database.EventRepository;
import server.database.TagRepository;

import java.awt.*;
import java.util.List;
import java.util.Random;

@Service
public class EventService {

    private final EventRepository eventRepository;
    private final TagRepository tagRepository;
    private final Random random;
    private final char[] chars = ("ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890").toCharArray();

    @Autowired
    public EventService(EventRepository eventRepository, TagRepository tagRepository, Random random) {
        this.eventRepository = eventRepository;
        this.tagRepository = tagRepository;
        this.random = random;
    }

    /**
     * creates an event with the given title and description
     *
     * @param title       the title of the event
     * @param description the description of the event
     * @return the created event
     */
    public Event createEvent(String title, String description) {
        String inviteCode = generateUniqueInviteCode();
        Event event = new Event(inviteCode, title, description);

        Event res = eventRepository.save(event);

        tagRepository.saveAll(List.of(
                new Tag("Food", new Color(0, 255, 0).getRGB(), event),
                new Tag("Entrance Fees", new Color(0, 0, 255).getRGB(), event),
                new Tag("Travel", new Color(255, 0, 0).getRGB(), event),
                new Tag("Payment", Color.BLACK.getRGB(), event)
        ));

        return res;
    }

    /**
     * updates the event with the given id
     *
     * @return the updated event
     */
    private String generateUniqueInviteCode() {
        String inviteCode;
        do {
            inviteCode = generateRandomInviteCode();
        } while (eventRepository.existsByInviteCode(inviteCode));
        return inviteCode;
    }

    /**
     * generates a random invite code
     *
     * @return the generated invite code
     */
    public String generateRandomInviteCode() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            char c = chars[random.nextInt(chars.length)];
            sb.append(c);
        }
        return sb.toString();
    }
}
