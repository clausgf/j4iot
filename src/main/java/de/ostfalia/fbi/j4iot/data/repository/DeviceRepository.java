package de.ostfalia.fbi.j4iot.data.repository;

import de.ostfalia.fbi.j4iot.data.entity.Device;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DeviceRepository extends JpaRepository<Device, Long> {
    //@Query("select d from Device d where lower(d.name) like lower(concat('%', :searchTerm, '%'))")
    //List<Device> searchAll(@Param("searchTerm") String searchTerm);
/*
    @Query("select d from Device d where d.project = :project and d.name = :name")
    Optional<Device> findOneByProjectAndName(@Param("project") Project project, @Param("name") String name);

    @Query("select case when count(d) > 0 then true else false end from Device d where d.name = :deviceName and d.project.name = :projectName")
    Boolean existsByProjectNameAndDeviceName(@Param("projectName") String projectName, @Param("deviceName") String deviceName);
*/
    @Query("select d from Device d where d.project.id = :projectId and d.name = :name")
    Optional<Device> findByProjectIdAndName(@Param("projectId") Long projectId, @Param("name") String name);

    @Query("select d from Device d where d.project.name = :projectName and d.name = :name")
    Optional<Device> findByProjectNameAndName(@Param("projectName") String projectName, @Param("name") String name);

    @Query("select d from Device d join d.project.users u where u.id = :userId order by d.name")
    List<Device> findAllByUserId(@Param("userId") Long userId);

    @Query("select d from Device d join d.project.users u where u.id = :userId and d.id = :id")
    Optional<Device> findByUserIdAndId(@Param("userId") Long userId, @Param("id") Long id);

    @Query("select d from Device d join d.project.users u where u.id = :userId and d.project.id = :projectId order by d.name")
    List<Device> findAllByUserIdAndProjectId(@Param("userId") Long userId, @Param("projectId") Long projectId);

    @Query("select d from Device d join d.project.users u where u.id = :userId and d.project.id = :projectId and d.name = :name")
    Optional<Device> findByUserIdAndProjectIdAndName(@Param("userId") Long userId, @Param("projectId") Long projectId, @Param("name") String name);

    @Query("select d.name from Device d join d.project.users u where u.id = :userId and d.project.id = :projectId order by d.name")
    List<String> findAllNamesByUserIdAndProjectId(@Param("userId") Long userId, @Param("projectId") Long projectId);

    long countByProjectId(Long projectId);
}
