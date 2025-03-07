package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.model.EndpointHit;
import ru.practicum.model.ViewStats;

import java.time.LocalDateTime;
import java.util.Collection;

@Repository
public interface EwmStatSrvRepository extends JpaRepository<EndpointHit, Long> {
    @Query("SELECT new ru.practicum.model.ViewStats(e.app, e.uri, COUNT (e.ip)) " +
            "FROM EndpointHit as e " +
            "WHERE e.timestamp BETWEEN ?1 AND ?2 " +
            "GROUP BY e.app, e.uri " +
            "ORDER BY COUNT (e.ip) DESC")
    Collection<ViewStats> findStatsByTime(LocalDateTime start,
                                          LocalDateTime end);

    @Query("SELECT new ru.practicum.model.ViewStats(e.app, e.uri, COUNT (e.ip)) " +
            "FROM EndpointHit as e " +
            "WHERE e.timestamp BETWEEN ?1 AND ?2 AND e.uri IN ?3 " +
            "GROUP BY e.app, e.uri " +
            "ORDER BY COUNT (e.ip) DESC")
    Collection<ViewStats> findStatsByTimeAndUris(LocalDateTime start,
                                                 LocalDateTime end,
                                                 Collection<String> uris);

    @Query("SELECT new ru.practicum.model.ViewStats(e.app, e.uri, COUNT (DISTINCT e.ip)) " +
            "FROM EndpointHit as e " +
            "WHERE e.timestamp BETWEEN ?1 AND ?2 " +
            "GROUP BY e.app, e.uri " +
            "ORDER BY COUNT (e.ip) DESC")
    Collection<ViewStats> findStatsByTimeAndUniqIp(LocalDateTime start,
                                                   LocalDateTime end);

    @Query("SELECT new ru.practicum.model.ViewStats(e.app, e.uri, COUNT (DISTINCT e.ip)) " +
            "FROM EndpointHit as e " +
            "WHERE e.timestamp BETWEEN ?1 AND ?2 AND e.uri IN ?3 " +
            "GROUP BY e.app, e.uri " +
            "ORDER BY COUNT (e.ip) DESC")
    Collection<ViewStats> findStatsByTimeAndUrisAndUniqIp(LocalDateTime start,
                                                          LocalDateTime end,
                                                          Collection<String> uris);
}