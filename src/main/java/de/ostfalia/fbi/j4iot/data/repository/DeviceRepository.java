package de.ostfalia.fbi.j4iot.data.repository;

import de.ostfalia.fbi.j4iot.data.entity.Device;
import de.ostfalia.fbi.j4iot.data.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DeviceRepository extends JpaRepository<Device, Long> {
    @Query("select d from Device d where lower(d.name) like lower(concat('%', :searchTerm, '%'))")
    List<Device> searchAll(@Param("searchTerm") String searchTerm);

    @Query("select d.name from Device d")
    List<String> findAllNames();

    @Query("select d.name from Device d where lower(d.name) like lower(concat('%', :searchTerm, '%'))")
    List<String> searchAllNames(@Param("searchTerm") String searchTerm);

    @Query("select d.name from Device d where d.project = :project")
    List<String> findAllNamesByProject(@Param("project") Project project);

    @Query("select d.name from Device d where d.project = :project and lower(d.name) like lower(concat('%', :searchTerm, '%'))")
    List<String> searchAllNamesByProject(@Param("project") Project project, @Param("searchTerm") String searchTerm);

    @Query("select d from Device d where d.project = :project and d.name = :name")
    Device findByProjectAndName(@Param("project") Project project, @Param("name") String name);
}
