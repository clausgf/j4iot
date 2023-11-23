package de.ostfalia.fbi.j4iot.data.repository;

import de.ostfalia.fbi.j4iot.data.entity.Device;
import de.ostfalia.fbi.j4iot.data.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DeviceRepository extends JpaRepository<Device, Long> {
    @Query("select d from Device d where lower(d.name) like lower(concat('%', :searchTerm, '%'))")
    List<Device> searchAll(@Param("searchTerm") String searchTerm);

    @Query("select d from Device d where d.project = :project and d.name = :name")
    Optional<Device> findOneByProjectAndName(@Param("project") Project project, @Param("name") String name);

    @Query("select d from Device d where d.project.name = :projectName and d.name = :name")
    Optional<Device> findOneByProjectNameAndName(@Param("projectName") String projectName, @Param("name") String name);

    @Query("select case when count(d) > 0 then true else false end from Device d where d.name = :deviceName and d.project.name = :projectName")
    Boolean existsByProjectNameAndDeviceName(@Param("projectName") String projectName, @Param("deviceName") String deviceName);

    @Query("select d from Device d where d.project.name = :projectName")
    List<Device> findAllByProjectName(@Param("projectName") String projectName);

    @Query("select d from Device d where d.project.name = :projectName and lower(d.name) like lower(concat('%', :searchTerm, '%'))")
    List<Device> searchAllByProjectName(@Param("projectName") String projectName, @Param("searchTerm") String searchTerm);

    @Query("select d.name from Device d")
    List<String> findAllNames();

    @Query("select d.name from Device d where lower(d.name) like lower(concat('%', :searchTerm, '%'))")
    List<String> searchAllNames(@Param("searchTerm") String searchTerm);

    @Query("select d.name from Device d where d.project.name = :projectName")
    List<String> findAllNamesByProjectName(@Param("projectName") String projectName);

    @Query("select d.name from Device d where d.project = :projectName and lower(d.name) like lower(concat('%', :searchTerm, '%'))")
    List<String> searchAllNamesByProjectName(@Param("project") String projectName, @Param("searchTerm") String searchTerm);

    long countByProjectId(Long projectId);
}
