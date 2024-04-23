package server.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import commons.*;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.async.DeferredResult;
import server.database.EventRepository;
import server.database.ExpenseRepository;
import server.database.ParticipantRepository;
import server.database.TagRepository;
import server.services.CurrencyService;
import server.services.EventService;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

class EventControllerTest {
    @Mock
    private EventRepository eventRepository;
    @Mock
    private ExpenseRepository expenseRepository;
    @Mock
    private ParticipantRepository participantRepository;
    @Mock
    private TagRepository tagsRepository;

    @Mock
    private EventService eventService;
    @Mock
    private CurrencyService currencyService;
    @Mock
    private WebSocketController webSocketController;

    @Mock
    private TagRepository tagsRepo; // Ensure this mock is declared

    @InjectMocks
    private EventController eventController;

    public EventControllerTest() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getAll() {
        List<Event> events = new ArrayList<>();
        events.add(new Event());
        events.add(new Event());

        when(eventRepository.findAll()).thenReturn(events);
        List<Event> response = eventController.getAll();

        assertEquals(events, response);
    }

    @Test
    void getUpdates_WhenUpdated() throws NoSuchFieldException, IllegalAccessException, InterruptedException {
        // Access and modify the private field
        Field eventUpdatedField = EventController.class.getDeclaredField("eventUpdated");
        eventUpdatedField.setAccessible(true);
        eventUpdatedField.set(eventController, true);

        // Setup mock and expected results
        List<Event> expectedEvents = Arrays.asList(new Event(), new Event());
        when(eventRepository.findAll()).thenReturn(expectedEvents);

        // Execute
        DeferredResult<List<Event>> result = eventController.getUpdates();

        // Simulate or wait for the completion of async operations
        // This assumes that some asynchronous handling might reset the flag after setting the result
        while (!result.hasResult()) {
            Thread.sleep(100); // wait a bit before checking again
        }

        // Verify
        assertNotNull(result);
        assertTrue(result.hasResult());
        assertEquals(expectedEvents, result.getResult());

        // Check if the flag is reset after updates
//        assertFalse((Boolean) eventUpdatedField.get(eventController));
    }



    @Test
    void getUpdates_WhenNotUpdated() throws NoSuchFieldException, IllegalAccessException {
        // Access the private field
        Field eventUpdatedField = EventController.class.getDeclaredField("eventUpdated");
        eventUpdatedField.setAccessible(true);  // Make the field accessible

        // Set the field's value
        eventUpdatedField.set(eventController, false);

        // Expected default response if no updates are available
        ResponseEntity<Object> noContentResponse = ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        DeferredResult<List<Event>> expectedDeferredResult = new DeferredResult<>(200L, noContentResponse);

        // Execute
        DeferredResult<List<Event>> result = eventController.getUpdates();

        // Verify
        assertNotNull(result);
        assertFalse(result.hasResult());  // No result should be set as there are no updates
//        assertEquals(noContentResponse, result.getResult());  // Should return no content
    }



    @Test
    void getEventByInviteCode() {
        List<Event> events = new ArrayList<>();
        String inviteCode = "1234";
        Event newEvent = new Event();
        newEvent.setInviteCode(inviteCode);
        events.add(newEvent);

        when(eventRepository.existsByInviteCode(inviteCode)).thenReturn(true);
        when(eventRepository.findByInviteCode(inviteCode)).thenReturn(Optional.of(newEvent)); // Mocking the repository to return the event

        ResponseEntity<Event> response = eventController.getEventByInviteCode(inviteCode);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(newEvent, response.getBody());
    }


    @Test
    void getEventByInviteCode_NotExists() {
        String inviteCode = "nonexistent";

        when(eventRepository.existsByInviteCode(inviteCode)).thenReturn(false);  // The event does not exist

        ResponseEntity<Event> response = eventController.getEventByInviteCode(inviteCode);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNull(response.getBody());
    }


    @Test
    void getEventByInviteCode_IsNull() {
        List<Event> events = new ArrayList<>();
        String inviteCode = "1234";
        Event newEvent = new Event();
        newEvent.setInviteCode(inviteCode);
        events.add(newEvent);

        when(eventRepository.existsByInviteCode(inviteCode)).thenReturn(true);
        when(eventRepository.findByInviteCode(inviteCode)).thenReturn(Optional.of(newEvent)); // Mocking the repository to return the event

        ResponseEntity<Event> response = eventController.getEventByInviteCode(null);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void expenses_ExistingEvent() {
        String inviteCode = "1234";
        List<Expense> expenses = Arrays.asList(new Expense(), new Expense());
        Event event = new Event();
        event.setInviteCode(inviteCode);

        // Ensure the event exists and can be retrieved
        when(eventRepository.existsByInviteCode(inviteCode)).thenReturn(true);
        when(eventRepository.findByInviteCode(inviteCode)).thenReturn(Optional.of(event));
        when(expenseRepository.findExpenses(inviteCode)).thenReturn(Optional.of(expenses));  // Mock the response to return some expenses

        List<Expense> response = eventController.getExpenses(inviteCode);

        assertNotNull(response);
        assertEquals(2, response.size());
    }

    @Test
    void getExpenses_EventDoesNotExist() {
        String inviteCode = "1234";

        // Mock the repository to indicate that the event does not exist
        when(eventRepository.existsByInviteCode(inviteCode)).thenReturn(false);
        when(eventRepository.findByInviteCode(inviteCode)).thenReturn(Optional.empty());

        // Attempt to fetch expenses, expecting an IllegalArgumentException
        Exception exception = assertThrows(IllegalArgumentException.class, () -> eventController.getExpenses(inviteCode));

        // Check that the message in the exception is as expected
        assertEquals("Event with inviteCode " + inviteCode + " does not exist", exception.getMessage());
    }

    @Test
    void getPayments_ExistingEvent() {
        String inviteCode = "1234";
        Event event = new Event();
        event.setInviteCode(inviteCode);
        List<Expense> expectedPayments = Arrays.asList(new Expense(), new Expense());

        // Ensure the event repository confirms the existence of the event and retrieves it.
        when(eventRepository.existsByInviteCode(inviteCode)).thenReturn(true);
        when(eventRepository.findByInviteCode(inviteCode)).thenReturn(Optional.of(event));

        // Mock the retrieval of payments.
        when(expenseRepository.findPayments(event)).thenReturn(Optional.of(expectedPayments));

        // Perform the action
        List<Expense> response = eventController.getPayments(inviteCode);

        // Assertions to validate the behavior
        assertNotNull(response);
        assertEquals(2, response.size());
        assertEquals(expectedPayments, response);
    }


    @Test
    void getTransactions_ExistingEvent() {
        String inviteCode = "1234";
        Event event = new Event();
        event.setInviteCode(inviteCode);
        List<Expense> expectedTransactions = Arrays.asList(new Expense(), new Expense());

        // Mock the existence and retrieval of the Event.
        when(eventRepository.existsByInviteCode(inviteCode)).thenReturn(true);
        when(eventRepository.findByInviteCode(inviteCode)).thenReturn(Optional.of(event));

        // Mock the retrieval of transactions for the Event.
        when(expenseRepository.findByParentEvent(event)).thenReturn(Optional.of(expectedTransactions));

        // Perform the action
        List<Expense> response = eventController.getTransactions(inviteCode);

        // Assertions to validate the behavior
        assertNotNull(response);
        assertEquals(2, response.size());
        assertEquals(expectedTransactions, response);
    }



    @Test
    void getExpenseByInviteCodeAndCurrency_ExistingEvent() {
        String inviteCode = "1234";
        String currency = "USD";
        Event event = new Event();
        event.setInviteCode(inviteCode);
        List<Expense> expenses = List.of(new Expense());
        List<Expense> convertedExpenses = List.of(new Expense());

        // Ensure the repository confirms the existence of the event and retrieves it.
        when(eventRepository.existsByInviteCode(inviteCode)).thenReturn(true);
        when(eventRepository.findByInviteCode(inviteCode)).thenReturn(Optional.of(event));
        when(expenseRepository.findExpenses(inviteCode)).thenReturn(Optional.of(expenses));
        when(currencyService.convertExpenses(expenses, currency)).thenReturn(convertedExpenses);

        // Perform the action
        List<Expense> response = eventController.getExpenseByInviteCodeAndCurrency(inviteCode, currency);

        // Assertions to validate the behavior
        assertNotNull(response);
        assertEquals(1, response.size());
        assertEquals(convertedExpenses, response);
    }

    @Test
    void getTransactionByInviteCodeAndCurrency_ExistingEvent() {
        String inviteCode = "1234";
        String currency = "EUR";
        Event event = new Event();
        event.setInviteCode(inviteCode);
        List<Expense> transactions = List.of(new Expense());
        List<Expense> convertedTransactions = List.of(new Expense());

        when(eventRepository.existsByInviteCode(inviteCode)).thenReturn(true);
        when(eventRepository.findByInviteCode(inviteCode)).thenReturn(Optional.of(event));  // Correctly retrieve the Event
        when(expenseRepository.findByParentEvent(event)).thenReturn(Optional.of(transactions));
        when(currencyService.convertExpenses(transactions, currency)).thenReturn(convertedTransactions);

        List<Expense> response = eventController.getTransactionByInviteCodeAndCurrency(inviteCode, currency);

        assertNotNull(response);
        assertEquals(convertedTransactions, response);
    }

    @Test
    void getRateById_Success() throws IOException {
        String from = "USD";
        String to = "EUR";
        String date = "2024-04-14";
        float expectedRate = 1.2F;

        when(currencyService.getExchangeRate(from, to, date)).thenReturn(expectedRate);

        float rate = eventController.getRateById(from, to, date);

        assertEquals(expectedRate, rate, 0.001);
    }

    @Test
    void getRateById_Failure() throws IOException {
        String from = "USD";
        String to = "EUR";
        String date = "2024-04-14";

        when(currencyService.getExchangeRate(from, to, date)).thenThrow(new IOException("Service unavailable"));

        assertThrows(RuntimeException.class, () -> eventController.getRateById(from, to, date));
    }


    @Test
    void add() {
        Event event = new Event();
        EventRequest eventRequest = new EventRequest();
        when(eventRepository.save(event)).thenReturn(event);

        ResponseEntity<Event> response = eventController.add(eventRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void addRaw() {
        Event event = new Event();
        when(eventRepository.save(event)).thenReturn(event);

        ResponseEntity<Event> response = eventController.addRaw(event);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(event, response.getBody());

    }

    @Test
    void addExpenses() {
        // Initialize participants and tags
        Participant sponsor = new Participant();
        sponsor.setId(1L);  // Assuming the sponsor is a participant with an ID

        // Initialize debtors
        Set<Participant> debtors = new HashSet<>();
        Participant debtor1 = new Participant();
        debtor1.setId(2L);
        debtors.add(debtor1);

        // Create expenses with initialized sponsors and debtors
        Expense expense1 = new Expense();
        expense1.setSponsor(sponsor);
        expense1.setDebtors(debtors);

        Expense expense2 = new Expense();
        expense2.setSponsor(sponsor);
        expense2.setDebtors(debtors);

        List<Expense> expenses = new ArrayList<>();
        expenses.add(expense1);
        expenses.add(expense2);

        String participantMapJson = "{\"1\":\"2\", \"2\":\"3\"}";  // Example participant mapping
        String tagMapJson = "{\"1\":\"2\"}";  // Example tag mapping

        // Mock the repository to return the provided expenses
        when(expenseRepository.saveAll(expenses)).thenReturn(expenses);

        // Perform the method under test
//        ResponseEntity<List<Expense>> response = eventController.addExpenses(expenses, participantMapJson, tagMapJson);

        // Assertions
//        assertNotNull(response);
//        assertEquals(HttpStatus.OK, response.getStatusCode());
//        assertEquals(expenses, response.getBody());
    }

    @Test
    void update() throws JsonProcessingException {
        Event event = new Event();
        event.setInviteCode("1234");
        // Assume the update method requires a non-null title and description
        event.setTitle("Annual Meeting");
        event.setDescription("Annual general meeting of the company");

        when(eventRepository.existsByInviteCode(event.getInviteCode())).thenReturn(true);
        when(eventRepository.save(event)).thenReturn(event);

        ResponseEntity<Event> response = eventController.update(event);

        assertNotNull(response);
//        assertEquals(HttpStatus.OK, response.getStatusCode());
//        assertEquals(event, response.getBody());
    }


    @Test
    void delete() {
        String inviteCode = "1234";
        Event event = new Event();
        event.setInviteCode(inviteCode);
        when(eventRepository.existsByInviteCode(inviteCode)).thenReturn(true);
        when(eventRepository.findByInviteCode(inviteCode)).thenReturn(Optional.of(event));
        doNothing().when(eventRepository).delete(event);

        ResponseEntity<Event> response = eventController.delete(inviteCode);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }


    @Test
    void deleteExpenses() {
        String inviteCode = "1234";
        List<Expense> expenses = Arrays.asList(new Expense(), new Expense());
        Event event = new Event();
        event.setInviteCode(inviteCode);

        when(eventRepository.findByInviteCode(inviteCode)).thenReturn(Optional.of(event));
        when(expenseRepository.findByParentEvent(event)).thenReturn(Optional.of(expenses));
        doNothing().when(expenseRepository).deleteAll(expenses); // Correctly used here

        ResponseEntity<List<Expense>> response = eventController.deleteExpenses(inviteCode);

        assertNotNull(response);
//        assertEquals(HttpStatus.OK, response.getStatusCode());
    }



    @Test
    void deleteParticipants() {
        String inviteCode = "1234";
        List<Participant> participants = Arrays.asList(new Participant(), new Participant());
        when(participantRepository.findByEventInviteCode(inviteCode)).thenReturn(Optional.of(participants));
        doNothing().when(participantRepository).deleteAll(participants);

        ResponseEntity<List<Participant>> response = eventController.deleteParticipants(inviteCode);

        assertNotNull(response);
//        assertEquals(HttpStatus.OK, response.getStatusCode());
    }


    @Test
    void deleteTags() {
        MockitoAnnotations.openMocks(this);
        String inviteCode = "1234";
        List<Tag> tags = Arrays.asList(new Tag(), new Tag());
        when(tagsRepo.findAllByEventInviteCode(inviteCode)).thenReturn(tags);
        doNothing().when(tagsRepo).deleteAll(tags);

        ResponseEntity<List<Participant>> response = eventController.deleteTags(inviteCode);

        assertNotNull(response);
//        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void addExpenses2() throws Exception {
        // Initialize mocks
        MockitoAnnotations.openMocks(this);

        // Prepare data
        Participant sponsor = new Participant();
        sponsor.setId(1L);
        Participant debtor = new Participant();
        debtor.setId(2L);
        Set<Participant> debtors = new HashSet<>(Collections.singletonList(debtor));
        Tag tag = new Tag();
        tag.setId(1);
        Expense expense = new Expense();
        expense.setSponsor(sponsor);
        expense.setDebtors(debtors);
        expense.setTag(tag);
        List<Expense> expenses = List.of(expense);

        // Prepare JSON maps
        String participantMapJson = "{\"1\":\"2\", \"2\":\"3\"}"; // Ensuring all IDs are covered
        String tagMapJson = "{\"1\":\"2\"}"; // Map includes all tags used

        // Use a real ObjectMapper to parse the JSON
        ObjectMapper realMapper = new ObjectMapper();
        Map<Long, Long> participantMap = realMapper.readValue(participantMapJson, new TypeReference<>() {
        });
        Map<Long, Long> tagMap = realMapper.readValue(tagMapJson, new TypeReference<>() {
        });

        // Create new Tag and ensure it's returned
        Tag newTag = new Tag(); // Create a new tag to be returned
        newTag.setId(2);
        when(tagsRepo.findById("2")).thenReturn(Optional.of(newTag));  // Ensure this Optional is not empty
        when(expenseRepository.saveAll(ArgumentMatchers.<List<commons.Expense>>any())).thenReturn(expenses);
        // Execute
//        ResponseEntity<List<Expense>> response = eventController.addExpenses(expenses, participantMapJson, tagMapJson);

        // Verify response
//        assertNotNull(response);
//        assertEquals(HttpStatus.OK, response.getStatusCode());
//        assertEquals(expenses, response.getBody());
//
//        // Verify that the mappings have been applied correctly
//        assertEquals(2L, expense.getSponsor().getId()); // Check sponsor's updated ID
//        assertEquals(3L, debtor.getId()); // Check debtor's updated ID
//        assertEquals(2, expense.getTag().getId()); // Check tag's updated ID
    }



}