package server.database;

import org.springframework.data.jpa.repository.JpaRepository;

import commons.Participant;

import java.util.List;
import java.util.Optional;

public interface ParticipantRepository extends JpaRepository<Participant, Long>{
    Optional<List<Participant>> findByEventInviteCode(String eventInviteCode);
    
}
