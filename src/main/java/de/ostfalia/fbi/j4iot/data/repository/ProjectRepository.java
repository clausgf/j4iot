package de.ostfalia.fbi.j4iot.data.repository;

import de.ostfalia.fbi.j4iot.data.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProjectRepository extends JpaRepository<Project, Long> {

    @Query("select p from Project p where lower(p.name) like lower(concat('%', :searchTerm, '%'))")
    List<Project> searchAll(@Param("searchTerm") String searchTerm);

    @Query("select p.name from Project p")
    List<String> findAllNames();

    @Query("select p.name from Project p where lower(p.name) like lower(concat('%', :searchTerm, '%'))")
    List<String> searchAllNames(@Param("searchTerm") String searchTerm);

    @Query("select p from Project p where p.name = :name")
    Project findByName(@Param("name") String name);
}
