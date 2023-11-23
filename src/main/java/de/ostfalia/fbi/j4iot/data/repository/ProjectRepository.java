package de.ostfalia.fbi.j4iot.data.repository;

import de.ostfalia.fbi.j4iot.data.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.util.Pair;

import java.util.List;
import java.util.Optional;

public interface ProjectRepository extends JpaRepository<Project, Long> {

    //@Query("select p from Project p where p.name = :name order by p.name")
    List<Project> findAllByName(@Param("name") String name);

    Optional<Project> findOneByName(@Param("name") String name);

    Boolean existsByName(String name);

    @Query("select p from Project p where (lower(p.name) like lower(concat('%', :searchTerm, '%'))) or (lower(p.tags) like lower(concat('%', :searchTerm, '%'))) order by p.name")
    List<Project> searchAll(@Param("searchTerm") String searchTerm);

    @Query("select p.name from Project p order by p.name")
    List<String> findAllNames();

    @Query("select p.name from Project p where lower(p.name) like lower(concat('%', :searchTerm, '%')) or (lower(p.tags) like lower(concat('%', :searchTerm, '%'))) order by p.name")
    List<String> searchAllNames(@Param("searchTerm") String searchTerm);

    @Query("select p.id, p.name from Project p order by p.name")
    List<Pair<Long, String>> findAllIdName();
}
