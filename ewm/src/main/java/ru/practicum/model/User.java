package ru.practicum.model;

import lombok.*;

import jakarta.persistence.*;

import java.util.List;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String email;
    private String name;
    @ToString.Exclude
    @ManyToMany
    @JoinTable(name = "user_subscriptions",
            joinColumns = @JoinColumn(name = "followee_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id"))
    private List<User> followees;
}
