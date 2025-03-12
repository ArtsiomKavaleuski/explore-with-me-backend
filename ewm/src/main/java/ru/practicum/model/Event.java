package ru.practicum.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@Entity
@Table(name = "events")
@AllArgsConstructor
@NoArgsConstructor
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String annotation;
    @ManyToOne
    @ToString.Exclude
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;
    @Column(name = "created_on")
    private LocalDateTime createdOn;
    private String description;
    @Column(name = "event_date")
    private LocalDateTime eventDate;
    @ManyToOne
    @ToString.Exclude
    @JoinColumn(name = "initiator_id", nullable = false)
    private User initiator;
    @Column(name = "is_paid")
    private Boolean isPaid;
    private String title;
    @Column(name = "loc_lat")
    private Double lat;
    @Column(name = "loc_lon")
    private Double lon;
    @Column(name = "participant_limit")
    private Integer participantLimit;
    @Column(name = "request_moderation")
    private Boolean requestModeration;
    @Column(name = "published_on")
    private LocalDateTime publishedOn;
    @Enumerated(value = EnumType.STRING)
    private EventState state;
    @Column(name = "confirmed_requests")
    private Integer confirmedRequests;
    @ManyToMany(mappedBy = "events")
    private List<Compilation> compilations;
}
