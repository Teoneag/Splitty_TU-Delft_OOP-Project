package server.database;

import commons.Event;
import commons.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    Optional<List<Expense>> findByParentEvent(Event parentEvent);
    @Query("SELECT ex FROM expense ex " +
            "JOIN event ev ON ex.parentEvent.inviteCode=ev.inviteCode " +
            "LEFT JOIN tag t ON (ex.tag.id=t.id AND t.name!='Payment') OR ex.tag IS NULL " +
            "WHERE ev.inviteCode=?1")
    Optional<List<Expense>> findExpenses(String parentEventIC);
    @Query("SELECT ex FROM expense ex " +
            "JOIN event ev ON ev=ex.parentEvent " +
            "JOIN tag t ON t=ex.tag AND t.name='Payment'" +
            "WHERE ev=?1")
    Optional<List<Expense>> findPayments(Event parentEvent);


}
