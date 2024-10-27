package by.prokopovich.time_tracker.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tasks")
@Getter
@Setter
@NoArgsConstructor
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String title;
    private String description;
    private Byte expectedHours;
    private Byte actualHours = 0;
    @Enumerated(EnumType.STRING)
    private Status status = Status.UNASSIGNED;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "executor_id", nullable = true)
    private User executor;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "task_id", referencedColumnName = "id")
    private List<Record> records = new ArrayList<>();

    public Task(String title, String description, Byte expectedHours) {
        this.title = title;
        this.description = description;
        this.expectedHours = expectedHours;
    }
}

