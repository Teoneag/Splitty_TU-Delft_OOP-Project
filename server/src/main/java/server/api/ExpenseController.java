package server.api;

import commons.Expense;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import server.database.EventRepository;
import server.database.ExpenseRepository;

@RestController
@RequestMapping("/api/expenses")
public class ExpenseController {
    private final ExpenseRepository expenseRepo;
    private final EventRepository eventRepo;
    private final WebSocketController webSocketController;

    /**
     * Constructor for the ExpenseController
     * @param expenseRepo the repository for expenses
     * @param eventRepo the repository for events
     * @param webSocketController the web socket controller
     */
    public ExpenseController(ExpenseRepository expenseRepo, EventRepository eventRepo,
                             WebSocketController webSocketController) {
        this.expenseRepo = expenseRepo;
        this.eventRepo = eventRepo;
        this.webSocketController = webSocketController;
    }

    private boolean isNullOrEmpty(String s) {
        return s == null || s.isEmpty();
    }

    /**
     * @param expenseId the id of the expense
     * @return The expense with the given id
     */
    @GetMapping("/{expenseId}")
    public Expense getById(@PathVariable Long expenseId) {
        if(!expenseRepo.existsById(expenseId) || expenseRepo.findById(expenseId).isEmpty()) {
            throw new IllegalArgumentException("Expense with id " + expenseId + " does not exist");
        }
        return expenseRepo.findById(expenseId).get();
    }


    /**
     * @param expense expense
     * @return response
     */
    @PostMapping({"", "/"})
    public ResponseEntity<Expense> add(@RequestBody Expense expense) {
        if (expense == null || isNullOrEmpty(expense.getTitle())) {
            return ResponseEntity.badRequest().build();
        }
        String inviteCode = expense.getParentEvent().getInviteCode();
        if (!eventRepo.existsByInviteCode(inviteCode) || eventRepo.findByInviteCode(inviteCode).isEmpty())
            throw new IllegalArgumentException("Event with id " + inviteCode + " does not exist");
        ResponseEntity<Expense>  result = ResponseEntity.ok(expenseRepo.save(expense));
        if(result.getStatusCode().is2xxSuccessful()) {
            webSocketController.sendUpdatedExpenses(expense.getParentEvent());
        }
        return result;
    }

    /**
     * @param expenseId expenseId
     * @param expense expense
     * @return response
     */
    @PutMapping("/{expenseId}")
    public ResponseEntity<Expense> edit(@PathVariable long expenseId, @RequestBody Expense expense) {
        if (expenseId != expense.getId() || !expenseRepo.existsById(expenseId)) {
            return ResponseEntity.badRequest().build();
        }
        ResponseEntity<Expense> result = ResponseEntity.ok(expenseRepo.save(expense));
        if(result.getStatusCode().is2xxSuccessful()) {
            webSocketController.sendUpdatedExpenses(expense.getParentEvent());
        }
        return result;
    }

    /**
     * Delete an expense by invite code and expense id
     * @param expenseId expense id
     * @return deleted expense
     */
    @DeleteMapping("/{expenseId}")
    public Expense delete(@PathVariable Long expenseId) {
        if (!expenseRepo.existsById(expenseId) || expenseRepo.findById(expenseId).isEmpty()) {
            throw new IllegalArgumentException("Expense with id " + expenseId + " does not exist");
        }
        Expense expense = expenseRepo.findById(expenseId).get();
        String parentId = expense.getParentEvent().getInviteCode();
        if (!eventRepo.existsByInviteCode(parentId) || eventRepo.findByInviteCode(parentId).isEmpty())
            throw new IllegalArgumentException("Event with id " + parentId + " does not exist");
        expenseRepo.deleteById(expenseId);
        webSocketController.sendUpdatedExpenses(expense.getParentEvent());
        return expense;
    }



}
