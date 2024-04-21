package server.api;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import commons.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;
import server.database.EventRepository;
import server.database.ExpenseRepository;
import server.database.ParticipantRepository;
import server.database.TagRepository;
import server.services.CurrencyService;
import server.services.EventService;

import java.io.IOException;
import java.util.*;

@RestController
@RequestMapping("/api/events")
public class EventController {

    private final EventRepository repo;
    private final ExpenseRepository expenseRepo;
    private final EventService eventService;
    private final CurrencyService currencyService;
    private final ParticipantRepository participantRepo;
    private final WebSocketController webSocketController;
    private final TagRepository tagsRepo;

    /**
     * Autowired constructor?
     *
     * @param repo                event repo
     * @param expenseRepo         expense repo
     * @param eventService        event service
     * @param currencyService     currency service
     * @param participantRepo     participant repo
     * @param webSocketController web socket controller
     * @param tagsRepo            tag repo
     */
    @Autowired
    public EventController(
            EventRepository repo,
            ExpenseRepository expenseRepo,
            EventService eventService,
            CurrencyService currencyService,
            ParticipantRepository participantRepo,
            WebSocketController webSocketController,
            TagRepository tagsRepo
    ) {
        this.repo = repo;
        this.expenseRepo = expenseRepo;
        this.eventService = eventService;
        this.currencyService = currencyService;
        this.participantRepo = participantRepo;
        this.webSocketController = webSocketController;
        this.tagsRepo = tagsRepo;
    }

    /**
     * @return all events
     */
    @GetMapping(path = {"", "/"})
    public List<Event> getAll() {
        return repo.findAll();
    }
    
    
    private Boolean eventUpdated = false;
    /**
     * @return all events for long polling
     */
    @GetMapping(path = {"/updates"})
    public DeferredResult<List<Event>> getUpdates() {
        ResponseEntity<Object> noContent = ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        DeferredResult<List<Event>> res = new DeferredResult<>(200L, noContent);
        
        if (eventUpdated) {
            res.setResult(repo.findAll());
            res.onCompletion(() -> eventUpdated = false);
        }
        
        return res;
    }

    /**
     * @param inviteCode of event
     * @return response entity
     */
    @GetMapping("/{inviteCode}")
    public ResponseEntity<Event> getEventByInviteCode(@PathVariable String inviteCode) {
        if (!repo.existsByInviteCode(inviteCode) || repo.findByInviteCode(inviteCode).isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(repo.findByInviteCode(inviteCode).get());
    }

    /**
     * @param inviteCode the id of the parent event
     * @return All expenses which reference the event
     */
    @GetMapping("{inviteCode}/expenses")
    public List<Expense> getExpenses(@PathVariable String inviteCode) {
        if(!repo.existsByInviteCode(inviteCode) || repo.findByInviteCode(inviteCode).isEmpty()) {
            throw new IllegalArgumentException("Event with inviteCode " + inviteCode + " does not exist");
        }
        Event parent = repo.findByInviteCode(inviteCode).get();
        return expenseRepo.findExpenses(parent.getInviteCode()).orElseThrow();
    }

    /**
     * @param inviteCode the id of the parent event
     * @return All expenses which reference the event
     */
    @GetMapping("{inviteCode}/payments")
    public List<Expense> getPayments(@PathVariable String inviteCode) {
        if(!repo.existsByInviteCode(inviteCode) || repo.findByInviteCode(inviteCode).isEmpty()) {
            throw new IllegalArgumentException("Event with inviteCode " + inviteCode + " does not exist");
        }
        Event parent = repo.findByInviteCode(inviteCode).get();
        return expenseRepo.findPayments(parent).orElseThrow();
    }

    /**
     * @param inviteCode the id of the parent event
     * @return All expenses which reference the event
     */
    @GetMapping("{inviteCode}/transactions")
    public List<Expense> getTransactions(@PathVariable String inviteCode) {
        if (!repo.existsByInviteCode(inviteCode) || repo.findByInviteCode(inviteCode).isEmpty()) {
            throw new IllegalArgumentException("Event with inviteCode " + inviteCode + " does not exist");
        }
        Event parent = repo.findByInviteCode(inviteCode).get();
        return expenseRepo.findByParentEvent(parent).orElseThrow();
    }

    /**
     * @param inviteCode the ID of the parent event
     * @param currency   the desired currency to convert the expenses into
     * @return All expenses which reference the event, converted into the specified currency
     */
    @GetMapping("{inviteCode}/expenses/{currency}")
    public List<Expense> getExpenseByInviteCodeAndCurrency(@PathVariable String inviteCode,
                                                           @PathVariable String currency) {
        if (!repo.existsByInviteCode(inviteCode) || repo.findByInviteCode(inviteCode).isEmpty()) {
            throw new IllegalArgumentException("Event with inviteCode " + inviteCode + " does not exist");
        }
        Event parent = repo.findByInviteCode(inviteCode).get();
        List<Expense> expenses = expenseRepo.findExpenses(parent.getInviteCode())
                .orElseThrow(() -> new IllegalArgumentException("No expenses found"));

        return currencyService.convertExpenses(expenses, currency);
    }

    /**
     * @param inviteCode the ID of the parent event
     * @param currency   the desired currency to convert the expenses into
     * @return All expenses which reference the event, converted into the specified currency
     */
    @GetMapping("{inviteCode}/transactions/{currency}")
    public List<Expense> getTransactionByInviteCodeAndCurrency(@PathVariable String inviteCode,
                                                           @PathVariable String currency) {
        if (!repo.existsByInviteCode(inviteCode) || repo.findByInviteCode(inviteCode).isEmpty()) {
            throw new IllegalArgumentException("Event with inviteCode " + inviteCode + " does not exist");
        }
        Event parent = repo.findByInviteCode(inviteCode).get();
        List<Expense> expenses = expenseRepo.findByParentEvent(parent)
                .orElseThrow(() -> new IllegalArgumentException("No expenses found"));

        return currencyService.convertExpenses(expenses, currency);
    }

    /**
     * Method to get the exchange rate between two currencies
     * @param from currency
     * @param to currency
     * @param date date of exchange rate
     * @return exchange rate
     */
    @GetMapping("/rate/{from}/{to}/{date}")
    public float getRateById(@PathVariable String from, @PathVariable String to, @PathVariable String date) {
        try {
            return currencyService.getExchangeRate(from, to, date);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * @param request the request with the title and description
     * @return response entity
     */
    @PostMapping(path = {"", "/"})
    public ResponseEntity<Event> add(@RequestBody EventRequest request) {
        Event event = eventService.createEvent(request.getTitle(), request.getDescription());
        
        eventUpdated = true;
        
        return ResponseEntity.ok(event);
    }

    /**
     * @param event to add
     * @return response entity
     */
    @PostMapping(path = "/raw")
    public ResponseEntity<Event> addRaw(@RequestBody Event event) {
        eventUpdated = true;
        return ResponseEntity.ok(repo.save(event));
    }

    /**
     * @param expenses to add
     * @param participantMap the map of new to old ids
     * @param tagMap the map of new to old ids
     * @return response entity
     */
    @PostMapping("/expenses")
    public ResponseEntity<List<Expense>> addExpenses(@RequestBody List<Expense> expenses,
                                                     @RequestHeader("participantMap") String participantMap,
                                                     @RequestHeader("tagMap") String tagMap) {
        ObjectMapper mapper = new ObjectMapper();
        Map<Long, Long> map = null;
        Map<Long, Long> map2 = null;
        try {
            map = mapper.readValue(participantMap, new TypeReference<Map<Long, Long>>() {
            });
            map2 = mapper.readValue(tagMap, new TypeReference<Map<Long, Long>>() {
            });
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        for (Expense expense : expenses) {
            expense.getSponsor().setId(map.get(expense.getSponsor().getId()));
            for (Participant participant : expense.getDebtors()) {
                participant.setId(map.get(participant.getId()));
            }
            expense.setTag(tagsRepo.findById(String.valueOf(map2.get(expense.getTag().getId()))).get());
        }
        return ResponseEntity.ok(expenseRepo.saveAll(expenses));
    }


    /**
     * @param event to update
     * @return response entity
     */
    @PutMapping(path = {"", "/"})
    public ResponseEntity<Event> update(@RequestBody Event event) throws JsonProcessingException {
        if (!repo.existsByInviteCode(event.getInviteCode()) || repo.findByInviteCode(event.getInviteCode()).isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        ResponseEntity<Event> result = ResponseEntity.ok(repo.save(event));
        webSocketController.sendUpdatedEvent(event.getInviteCode(), event);
        
        eventUpdated = true;
        
        return result;
    }

    /**
     * @param inviteCode of event to delete
     * @return response entity
     */
    @DeleteMapping(path = "/{inviteCode}")
    public ResponseEntity<Event> delete(@PathVariable String inviteCode) {
        if (!repo.existsByInviteCode(inviteCode) || repo.findByInviteCode(inviteCode).isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        repo.delete(repo.findByInviteCode(inviteCode).get());
        eventUpdated = true;
        return ResponseEntity.ok().build();
    }

    /**
     * @param inviteCode of event
     * @return response entity
     */
    @DeleteMapping("/{inviteCode}/expenses")
    public ResponseEntity<List<Expense>> deleteExpenses(@PathVariable String inviteCode) {
        if (!repo.existsByInviteCode(inviteCode) || repo.findByInviteCode(inviteCode).isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        List<Expense> expenses = expenseRepo.findByParentEvent(repo.findByInviteCode(inviteCode).get()).get();
        expenseRepo.deleteAll(expenses);
        return ResponseEntity.ok().build();
    }

    /**
     * @param inviteCode of event from which to delete participants
     * @return response entity with the list of participants
     */
    @DeleteMapping("/{inviteCode}/participants")
    public ResponseEntity<List<Participant>> deleteParticipants(@PathVariable String inviteCode) {
        if (!repo.existsByInviteCode(inviteCode) || repo.findByInviteCode(inviteCode).isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        List<Participant> participants = participantRepo.findByEventInviteCode(inviteCode).get();
        participantRepo.deleteAll(participants);
        return ResponseEntity.ok().build();
    }

    /**
     * @param inviteCode of event from which to delete tags
     * @return response entity with the list of participants
     */
    @DeleteMapping("/{inviteCode}/tags")
    public ResponseEntity<List<Participant>> deleteTags(@PathVariable String inviteCode) {
        if (!repo.existsByInviteCode(inviteCode) || repo.findByInviteCode(inviteCode).isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        List<Tag> tags = tagsRepo.findAllByEventInviteCode(inviteCode);
        tagsRepo.deleteAll(tags);
        return ResponseEntity.ok().build();
    }

}
