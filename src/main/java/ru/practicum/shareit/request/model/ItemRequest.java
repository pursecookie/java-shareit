package ru.practicum.shareit.request.model;

import lombok.*;
import ru.practicum.shareit.user.model.User;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "requests")
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class ItemRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false, length = 200)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requestor_id", referencedColumnName = "id")
    @ToString.Exclude
    private User requestor;

    private LocalDateTime created;
}