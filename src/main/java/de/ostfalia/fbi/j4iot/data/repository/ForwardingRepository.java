package de.ostfalia.fbi.j4iot.data.repository;

import de.ostfalia.fbi.j4iot.data.entity.Forwarding;
import de.ostfalia.fbi.j4iot.data.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ForwardingRepository extends JpaRepository<Forwarding, Long> {

    List<Forwarding> findAllByProject(Project project);

    @Query("select f from Forwarding f join f.project p where p.name = :projectName and f.name = :forwardName")
    Optional<Forwarding> findByProjectNameAndForwardName(@Param("projectName")String projectName, @Param("forwardName")String forwardName);
}
