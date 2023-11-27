package de.ostfalia.fbi.j4iot.data.repository;

import de.ostfalia.fbi.j4iot.data.entity.DeviceToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface DeviceTokenRepository extends JpaRepository<DeviceToken, Long> {
    //@Query("select d.name from Device d")
    //List<ProvisioningToken> findAll();

    //@Query("select d.name from Device d where d.project = :project")
    Optional<DeviceToken> findByToken(@Param("token") String token);
}
