package server.api;

import commons.Participant;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.FluentQuery.FetchableFluentQuery;
import server.database.ParticipantRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class TestParticipantRepository implements ParticipantRepository {
    public final List<Participant> participants = new ArrayList<>();
    public final List<String> calledMethods = new ArrayList<>();
    
    private void call(String method) {
        calledMethods.add(method);
    }
    
    @Override
    public void flush() {
        // TODO Auto-generated method stub
        
    }
    @Override
    public <S extends Participant> S saveAndFlush(S entity) {
        // TODO Auto-generated method stub
        return null;
    }
    @Override
    public <S extends Participant> List<S> saveAllAndFlush(Iterable<S> entities) {
        // TODO Auto-generated method stub
        return null;
    }
    @Override
    public void deleteAllInBatch(Iterable<Participant> entities) {
        // TODO Auto-generated method stub
        
    }
    @Override
    public void deleteAllByIdInBatch(Iterable<Long> ids) {
        // TODO Auto-generated method stub
        
    }
    @Override
    public void deleteAllInBatch() {
        // TODO Auto-generated method stub
        
    }
    @Override
    public Participant getOne(Long id) {
        // TODO Auto-generated method stub
        return null;
    }
    @Override
    public Participant getById(Long id) {
        // TODO Auto-generated method stub
        return null;
    }
    @Override
    public Participant getReferenceById(Long id) {
        // TODO Auto-generated method stub
        return null;
    }
    @Override
    public <S extends Participant> List<S> findAll(Example<S> example) {
        // TODO Auto-generated method stub
        return null;
    }
    @Override
    public <S extends Participant> List<S> findAll(Example<S> example, Sort sort) {
        // TODO Auto-generated method stub
        return null;
    }
    @Override
    public <S extends Participant> List<S> saveAll(Iterable<S> entities) {
        // TODO Auto-generated method stub
        return null;
    }
    @Override
    public List<Participant> findAll() {
        call("findAll");
        return participants;
    }
    @Override
    public List<Participant> findAllById(Iterable<Long> ids) {
        // TODO Auto-generated method stub
        return null;
    }
    @Override
    public <S extends Participant> S save(S entity) {
        call("save");
        entity.setId(participants.size());
        participants.add(entity);
        return entity;
    }
    @Override
    public Optional<Participant> findById(Long id) {
        // TODO Auto-generated method stub
        call("findById");
        return participants.stream().filter(p -> p.getId() == id).findFirst();
    }
    @Override
    public boolean existsById(Long id) {
        // TODO Auto-generated method stub
        if (id < 0) return false;
        for (Participant participant : participants) {
            if (participant.getId() == id) return true;
        }
        return false;
    }
    @Override
    public long count() {
        // TODO Auto-generated method stub
        return 0;
    }
    @Override
    public void deleteById(Long id) {
        call("deleteById");
        if (!existsById(id)) return;
        participants.removeIf(p -> p.getId() == id);
        
    }
    @Override
    public void delete(Participant entity) {
        // TODO Auto-generated method stub
        
    }
    @Override
    public void deleteAllById(Iterable<? extends Long> ids) {
        // TODO Auto-generated method stub
        
    }
    @Override
    public void deleteAll(Iterable<? extends Participant> entities) {
        call("deleteAll");
        for (Participant participant : entities) {
            participants.remove(participant);
        }
        
    }
    @Override
    public void deleteAll() {
        // TODO Auto-generated method stub
        
    }
    @Override
    public List<Participant> findAll(Sort sort) {
        // TODO Auto-generated method stub
        return null;
    }
    @Override
    public Page<Participant> findAll(Pageable pageable) {
        // TODO Auto-generated method stub
        return null;
    }
    @Override
    public <S extends Participant> Optional<S> findOne(Example<S> example) {
        // TODO Auto-generated method stub
        return Optional.empty();
    }
    @Override
    public <S extends Participant> Page<S> findAll(Example<S> example, Pageable pageable) {
        // TODO Auto-generated method stub
        return null;
    }
    @Override
    public <S extends Participant> long count(Example<S> example) {
        // TODO Auto-generated method stub
        return 0;
    }
    @Override
    public <S extends Participant> boolean exists(Example<S> example) {
        // TODO Auto-generated method stub
        return false;
    }
    @Override
    public <S extends Participant, R> R findBy(Example<S> example, Function<FetchableFluentQuery<S>, R> queryFunction) {
        // TODO Auto-generated method stub
        return null;
    }
    
    
    @Override
    public Optional<List<Participant>> findByEventInviteCode(String eventInviteCode) {
        calledMethods.add("findByEventInviteCode");
        List<Participant> result = new ArrayList<>();
        for (Participant participant : this.participants) {
            if (participant.getEventInviteCode().equals(eventInviteCode)) {
                result.add(participant);
            }
        }
        return Optional.of(result);
    }
}
