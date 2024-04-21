package server.database;

import commons.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TagRepository extends JpaRepository<Tag, String> {

    List<Tag> findAllByEventInviteCode(String eventInviteCode);
    Tag findByNameAndEvent_InviteCode(String name, String inviteCode);
}
