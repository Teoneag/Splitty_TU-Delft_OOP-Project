package client.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import commons.*;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.glassfish.jersey.client.ClientConfig;
import org.springframework.lang.NonNull;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import javax.inject.Inject;
import javax.websocket.DeploymentException;
import java.lang.reflect.Type;
import java.net.ConnectException;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

public class ServerUtils {

    private final ConfigService configService;
    private String server;
    private StompSession session = null;

    @Inject
    public ServerUtils(ConfigService configService) {
        this.configService = configService;
        server = configService.getServer();
    }

    public void refreshServer() {
        server = configService.getServer();
    }

    public boolean isConnected() {
        return session != null && session.isConnected();
    }

    /**
     * gets the event with the given invite code
     *
     * @param inviteCode the invite code of the event used as uid
     * @return the event with the given invite code
     */
    public Event getEvent(String inviteCode) {
        return ClientBuilder.newClient(new ClientConfig()) //
                .target(server).path("api/events/" + inviteCode) //
                .request(APPLICATION_JSON) //
                .accept(APPLICATION_JSON) //
                .get(Event.class);
    }

    /**
     * sends a request to the server to add an event to the database
     *
     * @param title       the title of the event
     * @param description the description of the event
     * @return event
     */
    public Event addEvent(String title, String description) {
        EventRequest request = new EventRequest(title, description);
        return ClientBuilder.newClient(new ClientConfig()) //
                .target(server).path("api/events") //
                .request(APPLICATION_JSON) //
                .accept(APPLICATION_JSON) //
                .post(Entity.entity(request, APPLICATION_JSON), Event.class);
    }

    /**
     * sends a request to the server to add an event to the database
     *
     * @param event event to be added
     */
    public void updateEvent(Event event) {
        ClientBuilder.newClient(new ClientConfig()) //
                .target(server).path("api/events") //
                .request(APPLICATION_JSON) //
                .accept(APPLICATION_JSON) //
                .put(Entity.entity(event, APPLICATION_JSON), Event.class);
    }


    private final ExecutorService execServ = Executors.newSingleThreadExecutor();

    /**
     * sends a request to the server to get all events from the database
     *
     * @param consumer takes care of getting the events
     */
    public void longPollingRegisterEvent(Consumer<List<Event>> consumer) {
        execServ.submit(() -> {
            while (!Thread.interrupted()) {
                List<Event> res = ClientBuilder.newClient(new ClientConfig()) //
                        .target(server).path("api/events/updates") //
                        .request(APPLICATION_JSON) //
                        .accept(APPLICATION_JSON) //
                        .get(new GenericType<>() {
                        });

                if (res != null) {
                    consumer.accept(res);
                }
            }
        });
    }

    /**
     * Shuts down the long polling for the admin page
     */
    public void stop() {
        execServ.shutdownNow();
    }

    /**
     * sends a request to the server to get all events from the database
     *
     * @return list of events
     */
    public List<Event> getEvents() {
        return ClientBuilder.newClient(new ClientConfig()) //
                .target(server).path("api/events") //
                .request(APPLICATION_JSON) //
                .accept(APPLICATION_JSON) //
                .get(new GenericType<>() {
                });
    }

    /**
     * sends a request to the server to get an event from the database
     *
     * @param selected Event from which to get the id
     * @return Response from the server or null when deleting fails
     */
    public Response deleteEvent(Event selected) throws ConnectException {
        try {
            Response response1 = ClientBuilder.newClient(new ClientConfig()) //
                    .target(server).path("api/events") //
                    .path(selected.getInviteCode()) //
                    .path("expenses") //
                    .request(APPLICATION_JSON) //
                    .accept(APPLICATION_JSON) //
                    .delete();
            Response response2 = ClientBuilder.newClient(new ClientConfig()) //
                    .target(server).path("api/events") //
                    .path(selected.getInviteCode()) //
                    .path("participants") //
                    .request(APPLICATION_JSON) //
                    .accept(APPLICATION_JSON) //
                    .delete();
            Response response3 = ClientBuilder.newClient(new ClientConfig()) //
                    .target(server).path("api/events") //
                    .path(selected.getInviteCode()) //
                    .path("tags")
                    .request(APPLICATION_JSON) //
                    .accept(APPLICATION_JSON) //
                    .delete();
            Response response4 = ClientBuilder.newClient(new ClientConfig()) //
                    .target(server).path("api/events") //
                    .path(selected.getInviteCode()) //
                    .request(APPLICATION_JSON) //
                    .accept(APPLICATION_JSON) //
                    .delete();
            if (response1.getStatus() != 200 || response2.getStatus() != 200 || response3.getStatus() != 200
                    || response4.getStatus() != 200) {
                throw new Exception("Error deleting event: %d %d %d".formatted(response1.getStatus(),
                        response2.getStatus(), response3.getStatus()));
            } else {
                // ToDo show Events deleted
            }
            return response3;
        } catch (Exception e) {
            if (e.getCause().getClass() == ConnectException.class) throw new ConnectException();
            // ToDo show Error. No connection to server
            return null;
        }
    }

    /**
     * sends a request to the server to get an event from the database
     *
     * @param inviteCode inviteCode of the event to get
     * @return event as a json string
     */
    public String getJsonEvent(String inviteCode) {
        String event = ClientBuilder.newClient(new ClientConfig()) //
                .target(server).path("api/events") //
                .path(inviteCode) //
                .request(APPLICATION_JSON) //
                .accept(APPLICATION_JSON) //
                .get(String.class);
        String participants = ClientBuilder.newClient(new ClientConfig()) //
                .target(server).path("api/participants/event") //
                .path(inviteCode) //
                .request(APPLICATION_JSON) //
                .accept(APPLICATION_JSON) //
                .get(String.class);
        String expenses = ClientBuilder.newClient(new ClientConfig()) //
                .target(server).path("api/events") //
                .path(inviteCode) //
                .path("transactions") //
                .request(APPLICATION_JSON) //
                .accept(APPLICATION_JSON) //
                .get(String.class);
        String tags = ClientBuilder.newClient(new ClientConfig()) //
                .target(server).path("api/tags/event") //
                .path(inviteCode) //
                .path("raw")
                .request(APPLICATION_JSON) //
                .accept(APPLICATION_JSON) //
                .get(String.class);
        return event + "participants" + participants + "expenses" + expenses + "tags" + tags;
    }

    /**
     * sends a request to the server to add an event to the database
     *
     * @param event event to be added
     */
    public void addJsonEvent(String event) {
        Scanner scanner = new Scanner(event);
        scanner.useDelimiter("participants|expenses|tags");
        String eventString = scanner.next();
        String participantsString = scanner.next();
        String expensesString = scanner.next();
        String tagsString = scanner.next();
        ClientBuilder.newClient() //
                .target(server).path("api/events/raw") //
                .request(APPLICATION_JSON) //
                .accept(APPLICATION_JSON) //
                .post(Entity.entity(eventString, APPLICATION_JSON), String.class);
        String participantMap = ClientBuilder.newClient() //
                .target(server).path("api/participants/list") //
                .request(APPLICATION_JSON) //
                .accept(APPLICATION_JSON) //
                .post(Entity.entity(participantsString, APPLICATION_JSON), String.class);
        String tagMap = ClientBuilder.newClient() //
                .target(server).path("api/tags/list") //
                .request(APPLICATION_JSON) //
                .accept(APPLICATION_JSON) //
                .post(Entity.entity(tagsString, APPLICATION_JSON), String.class);
        ClientBuilder.newClient() //
                .target(server).path("api/events/expenses") //
                .request(APPLICATION_JSON) //
                .accept(APPLICATION_JSON) //
                .header("participantMap", participantMap)
                .header("tagMap", tagMap)
                .post(Entity.entity(expensesString, APPLICATION_JSON), String.class);
    }

    /**
     * sends a post-request to the server to check the filled password for admin overview
     *
     * @param password input password of user
     * @return String according to the response status
     */
    public String checkAdminPassword(String password) {
        //send post-request to server with password and save the response
        var response = ClientBuilder.newClient(new ClientConfig()) //
                .target(server).path("api/admin") //
                .request(APPLICATION_JSON) //
                .accept(APPLICATION_JSON) //
                .post(Entity.entity(password, MediaType.TEXT_PLAIN), Response.class);
        if (response.getStatus() != 200) {
            //if password is not equal to the server-generated password
            return "Wrong password!";
        } else {
            return "Admin password correct!";
        }
    }

    /**
     * Adds a specified participant to the database
     *
     * @param participant - the participant to be added
     */
    public void addParticipant(Participant participant) {
        ClientBuilder.newClient(new ClientConfig())
                .target(server).path("api/participants")
                .request(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .post(Entity.entity(participant, APPLICATION_JSON), Participant.class);
    }

    /**
     * Sends a delete request for a given participant
     *
     * @param participant the participant to be deleted
     */
    public void deleteParticipant(Participant participant) {
        ClientBuilder.newClient(new ClientConfig())
                .target(server).path("api/participants/" + participant.getId())
                .request(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .delete();
    }

    /**
     * Updates a participant in the database
     *
     * @param participant The participant with the new details
     */
    public void updateParticipant(Participant participant) {
        ClientBuilder.newClient(new ClientConfig())
                .target(server).path("api/participants/" + participant.getId())
                .request(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .put(Entity.entity(participant, APPLICATION_JSON), Participant.class);
    }

    /**
     * Gets the list of participants from the server
     *
     * @return all participants on the server
     */
    public List<Participant> getParticipants() {
        return ClientBuilder.newClient(new ClientConfig())
                .target(server).path("api/participants")
                .request(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .get(new GenericType<>() {
                });

    }

    /**
     * Gets the participant by their id
     *
     * @param participantId - the id of the participant wanted
     * @return the Participant
     */
    public Participant getParticipant(long participantId) {
        return ClientBuilder.newClient(new ClientConfig())
                .target(server).path("api/participants/" + participantId)
                .request(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .get(Participant.class);
    }


    /**
     * Send a post request for an expense to the server
     *
     * @param expense expense to add
     */
    public void addExpense(Expense expense) {
        ClientBuilder.newClient(new ClientConfig()) //
                .target(server).path("api/expenses/") //
                .request(APPLICATION_JSON) //
                .accept(APPLICATION_JSON) //
                .post(Entity.entity(expense, APPLICATION_JSON), Expense.class);
    }

    /**
     * Send a delete request for an expense to the server
     *
     * @param expense expense to be deleted
     */
    public void deleteExpense(Expense expense) {
        Client client = ClientBuilder.newClient(new ClientConfig());
        try {

            try (Response resp = client.target(server) // Connect to the server
                    .path("api/expenses/" + expense.getId()) // Specify the path
                    .request(APPLICATION_JSON) // Set request type
                    .accept(APPLICATION_JSON) // Set accepted response type
                    .delete()) { // Automatically close the response
                if (resp.getStatus() != 204) { // Check for the expected status code
                    // Handle unexpected response
                    throw new RuntimeException("Failed to delete expense: " + resp.getStatus());
                }
            }
        } finally {
            client.close(); // Ensure the client is also closed
        }
    }

    /**
     * Send a put request to update an expense
     *
     * @param expense expense to edit
     */
    public void updateExpense(Expense expense) {
        ClientBuilder.newClient(new ClientConfig()) //
                .target(server).path("api/expenses/" + expense.getId()) //
                .request(APPLICATION_JSON) //
                .accept(APPLICATION_JSON) //
                .put(Entity.entity(expense, APPLICATION_JSON), Expense.class);
    }

    /**
     * Send a get request for an expense to the server
     *
     * @param expenseId long id for the expense
     * @return the requested Expense object
     */
    public Expense getExpense(long expenseId) {
        return ClientBuilder.newClient(new ClientConfig()) //
                .target(server).path("api/expenses/" + expenseId) //
                .request(APPLICATION_JSON) //
                .accept(APPLICATION_JSON) //
                .get(Expense.class);
    }

    /**
     * Send a get request for an expense to the server
     *
     * @param inviteCode the invite code of the event
     * @return the requested Expense object
     */
    public List<Expense> getTransactionsByCurrency(String inviteCode) {
        String currency = configService.getConfigCurrency();
        return ClientBuilder.newClient(new ClientConfig()) //
                .target(server).path("api/events/" + inviteCode + "/transactions/" + currency) //
                .request(APPLICATION_JSON) //
                .accept(APPLICATION_JSON) //
                .get(new GenericType<>() {
                });

    }

    /**
     * Send a get request for an expense to the server
     *
     * @param inviteCode the invite code of the event
     * @return the requested Expense object
     */
    public List<Expense> getExpensesByCurrency(String inviteCode) {
        String currency = configService.getConfigCurrency();
        return ClientBuilder.newClient(new ClientConfig()) //
                .target(server).path("api/events/" + inviteCode + "/expenses/" + currency) //
                .request(APPLICATION_JSON) //
                .accept(APPLICATION_JSON) //
                .get(new GenericType<>() {
                });

    }

    /**
     * Send a get request for an expense to the server
     *
     * @param from the currency to convert from
     * @param to   the currency to convert to
     * @param date the date of the conversion
     * @return the requested rate
     */
    public float getRate(String from, String to, String date) {
        return ClientBuilder.newClient(new ClientConfig()) //
                .target(server).path("api/events/rate/" + from + "/" + to + "/" + date) //
                .request(APPLICATION_JSON) //
                .accept(APPLICATION_JSON) //
                .get(Float.class);
    }

    /**
     * Send a get request for an expense to the server
     *
     * @param inviteCode the invite code of the event
     * @return the requested Expense object
     */
    public List<Expense> getExpenses(String inviteCode) {
        return ClientBuilder.newClient(new ClientConfig()) //
                .target(server).path("api/events/" + inviteCode + "/expenses") //
                .request(APPLICATION_JSON) //
                .accept(APPLICATION_JSON) //
                .get(new GenericType<>() {
                });

    }

    /**
     * Send a get request for an expense to the server
     *
     * @param inviteCode the invite code of the event
     * @return the requested Expense object
     */
    public List<Expense> getPayments(String inviteCode) {
        return ClientBuilder.newClient(new ClientConfig()) //
                .target(server).path("api/events/" + inviteCode + "/payments") //
                .request(APPLICATION_JSON) //
                .accept(APPLICATION_JSON) //
                .get(new GenericType<>() {
                });

    }

    /**
     * Send a get request for an expense to the server
     *
     * @param inviteCode the invite code of the event
     * @return the requested Expense object
     */
    public List<Expense> getTransactions(String inviteCode) {
        return ClientBuilder.newClient(new ClientConfig()) //
                .target(server).path("api/events/" + inviteCode + "/transactions") //
                .request(APPLICATION_JSON) //
                .accept(APPLICATION_JSON) //
                .get(new GenericType<>() {
                });

    }

    /**
     * Gets the participants for an event
     *
     * @param eventInviteCode the invite code of the event
     * @return the participants
     */
    public List<Participant> getParticipantsByEventInviteCode(String eventInviteCode) {
        return ClientBuilder.newClient(new ClientConfig())
                .target(server).path("api/participants/event/" + eventInviteCode)
                .request(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .get(new GenericType<>() {
                });
    }

    /**
     * Post a new tag to the server
     *
     * @param tag to get added
     * @return the added tag
     */
    public Tag addTag(Tag tag) {
        return ClientBuilder.newClient(new ClientConfig())
                .target(server).path("api/tags")
                .request(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .post(Entity.entity(tag, APPLICATION_JSON), Tag.class);
    }

    /**
     * Get all tags from the server
     *
     * @return a list of tags in the entire server
     */
    public List<Tag> getAllTags() {
        return ClientBuilder.newClient(new ClientConfig())
                .target(server).path("api/tags")
                .request(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .get(new GenericType<>() {
                });
    }

    /**
     * Get all tags per event
     *
     * @param inviteCode the invite code of the event you want the tags from
     * @return a list of tags
     */
    public List<Tag> getTagsByEvent(String inviteCode) {
        return ClientBuilder.newClient(new ClientConfig())
                .target(server).path("api/tags/event/" + inviteCode)
                .request(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .get(new GenericType<>() {
                });
    }

    /**
     * Get a tag by its id
     *
     * @param id the id of the tag
     * @return the tag
     */
    public Tag getTagById(long id) {
        return ClientBuilder.newClient(new ClientConfig())
                .target(server).path("api/tags/" + id)
                .request(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .get(Tag.class);
    }

    /**
     * Update a tag
     *
     * @param tag the tag to update
     */
    public void updateTag(Tag tag) {
        ClientBuilder.newClient(new ClientConfig())
                .target(server)
                .path("api/tags/" + tag.getId())
                .request(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .put(Entity.entity(tag, APPLICATION_JSON), Tag.class);
    }

    /**
     * Delete a tag by its id
     *
     * @param id the id of the tag
     */
    public void deleteTagById(long id) {
        ClientBuilder.newClient(new ClientConfig())
                .target(server)
                .path("api/tags/" + id)
                .request(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .delete(Tag.class);
    }

    /**
     * Get an event's payment tag
     *
     * @param event event
     * @return the payment tag of the event
     */
    public Tag getPaymentTag(Event event) {
        return ClientBuilder.newClient(new ClientConfig())
                .target(server)
                .path("api/tags/event/" + event.getInviteCode() + "/payment")
                .request(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .get(Tag.class);
    }

    /**
     * Subscribes for event updates
     *
     * @param inviteCode the invite code of the event
     * @param consumer   the consumer to accept the event
     */
    public void registerForEvents(String inviteCode, Consumer<Event> consumer) {
        session.subscribe("/topic/event/" + inviteCode, new StompFrameHandler() {
            @NonNull
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return Event.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                consumer.accept((Event) payload);

            }

        });
    }

    /**
     * Connects to the websocket
     *
     * @param url      the url to connect to
     * @param consumer the consumer to accept the connection status
     */
    public void connect(String url, Consumer<Boolean> consumer) {
        var client = new StandardWebSocketClient();
        var stomp = new WebSocketStompClient(client);
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.registerSubtypes(Expense.class);
        objectMapper.registerSubtypes(Participant.class);
        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        converter.setObjectMapper(objectMapper);
        stomp.setMessageConverter(converter);
        try {
            session = stomp.connect(url, new StompSessionHandlerAdapter() {
                @Override
                public void handleTransportError(StompSession session, Throwable exception) {
                    consumer.accept(false);
                    super.handleTransportError(session, exception);
                }

                @Override
                public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
                    super.afterConnected(session, connectedHeaders);
                    consumer.accept(true);
                }
            }).get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (ExecutionException e) {
            if (e.getCause().getClass() == DeploymentException.class) {
                session = null;
            }
        }
    }

    /**
     * Subscribes for participants
     *
     * @param inviteCode the invite code of the event
     * @param consumer   the consumer to accept the participants
     */
    public void registerForParticipants(String inviteCode, Consumer<List<Participant>> consumer) {
        session.subscribe("/topic/participants/" + inviteCode, new StompFrameHandler() {
            @NonNull
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return new GenericType<List<Participant>>() {
                }.getType();
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                List<Participant> participants = null;
                try {
                    ObjectMapper objectMapper = new ObjectMapper();
                    participants = objectMapper.readValue(
                            objectMapper.writeValueAsString(payload),
                            objectMapper.getTypeFactory().constructCollectionType(List.class, Participant.class));
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
                consumer.accept(participants);
            }

        });
    }

    /**
     * Subscribes for expenses
     *
     * @param inviteCode the invite code of the event
     * @param consumer   the consumer to accept the expenses
     */
    public void registerForExpenses(String inviteCode, Consumer<List<Expense>> consumer) {
        session.subscribe("/topic/expenses/" + inviteCode, new StompFrameHandler() {
            @NonNull
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return new GenericType<List<Expense>>() {
                }.getType();
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                List<Expense> expenses = null;
                try {
                    ObjectMapper objectMapper = new ObjectMapper();
                    objectMapper.registerModule(new JavaTimeModule());
                    expenses = objectMapper.readValue(
                            objectMapper.writeValueAsString(payload),
                            objectMapper.getTypeFactory().constructCollectionType(List.class, Expense.class));
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
                consumer.accept(expenses);
            }
        });
    }

    public void disconnectWs() {
        session.disconnect();
    }
}
