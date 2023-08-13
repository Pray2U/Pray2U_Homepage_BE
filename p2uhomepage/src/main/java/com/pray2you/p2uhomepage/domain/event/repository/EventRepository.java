package com.pray2you.p2uhomepage.domain.event.repository;

import com.pray2you.p2uhomepage.domain.event.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    Optional<Event> findEventByIdAndDeleted(Long id, boolean deleted);

    List<Event> findAllByEventStartDateBetweenOrEventEndDateBetweenAndDeleted(LocalDateTime startDate, LocalDateTime endDate,LocalDateTime startDate2, LocalDateTime endDate2, boolean deleted);
}
