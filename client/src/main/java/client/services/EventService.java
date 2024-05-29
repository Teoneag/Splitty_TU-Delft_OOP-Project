package client.services;

import com.google.inject.Inject;
import commons.Event;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

final public class EventService {

    private final String recentEventsFilePath = "src/main/java/client/config/recent_events.txt";

    private static final List<Event> recentEvents = new ArrayList<>();

    private final ServerUtils serverUtils;
    private final I18NService i18NService;

    @Inject
    public EventService(ServerUtils serverUtils, I18NService i18NService) {
        this.serverUtils = serverUtils;
        this.i18NService = i18NService;
        recentEvents.addAll(serverUtils.getEvents());
        recentEvents.removeIf(event -> !getRecentEventsIds().contains(event.getInviteCode()));
    }

    public List<Event> getRecentEvents() {
        return recentEvents;
    }

    public void hideEvent(String inviteCode) {
        try {
            recentEvents.removeIf(event -> event.getInviteCode().equals(inviteCode));
            Path path = Paths.get(recentEventsFilePath);
            List<String> lines = Files.readAllLines(path);
            lines.remove(inviteCode);
            Files.write(path, lines, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Adds an event to the recent events file
     *
     * @param inviteCode the invite code of the event
     */
    public void addToRecentEvents(String inviteCode) {
        addToRecentEvents(serverUtils.getEvent(inviteCode));
    }

    /**
     * Adds an event to the recent events file
     *
     * @param event event to add
     */
    public void addToRecentEvents(Event event) {
        try {
            if (recentEvents.contains(event)) return;

            recentEvents.add(event);
            Path path = Paths.get(recentEventsFilePath);
            List<String> lines = Files.readAllLines(path);
            if (lines.contains(event.getInviteCode())) return;

            lines.add(event.getInviteCode());
            Files.write(path, lines, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Clears the recent events file
     */
    public void clearRecentEvents() {
        try {
            recentEvents.clear();
            Path path = Paths.get(recentEventsFilePath);
            Files.write(path, "".getBytes(), StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Gets the recent events from the recent events file
     *
     * @return a list of recent events
     */
    private List<String> getRecentEventsIds() {
        try {
            return Files.readAllLines(Paths.get(recentEventsFilePath));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
