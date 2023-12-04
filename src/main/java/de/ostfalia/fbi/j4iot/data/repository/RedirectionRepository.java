package de.ostfalia.fbi.j4iot.data.repository;

import de.ostfalia.fbi.j4iot.data.entity.Project;
import de.ostfalia.fbi.j4iot.data.entity.ProvisioningToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RedirectionRepository extends JpaRepository<ProvisioningToken, Long> {

    Optional<ProvisioningToken> findByToken(@Param("token") String token);

    List<ProvisioningToken> findAllByProject(Project project);
}
