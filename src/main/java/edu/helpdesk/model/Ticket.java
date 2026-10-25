package edu.helpdesk.model;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Set;

public class Ticket {
    private static final DateTimeFormatter DEADLINE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm XXX");
    private final long id;
    private final String title;
    private final String description;
    private TicketStatus status;
    private final TicketPriority priority;
    private final long authorId;
    private Long assigneeId;
    private final Long categoryId;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
    private final OffsetDateTime dueAt;

    public Ticket(long id,
                  String title,
                  String description,
                  TicketStatus status,
                  TicketPriority priority,
                  long authorId,
                  Long assigneeId,
                  Long categoryId,
                  LocalDateTime createdAt,
                  LocalDateTime updatedAt,
                  OffsetDateTime dueAt) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.status = status;
        this.priority = priority;
        this.authorId = authorId;
        this.assigneeId = assigneeId;
        this.categoryId = categoryId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.dueAt = dueAt;
    }

    public boolean canTransitionTo(TicketStatus next) {
        if (status == next) {
            return true;
        }
        return switch (status) {
            case NEW -> Set.of(TicketStatus.IN_PROGRESS).contains(next);
            case IN_PROGRESS -> Set.of(TicketStatus.RESOLVED, TicketStatus.NEW).contains(next);
            case RESOLVED -> Set.of(TicketStatus.CLOSED, TicketStatus.IN_PROGRESS).contains(next);
            case CLOSED -> false;
        };
    }

    public void transitionTo(TicketStatus next) {
        if (!canTransitionTo(next)) {
            throw new IllegalStateException("Недопустимый переход " + status + " -> " + next);
        }
        this.status = next;
    }

    public long getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public TicketStatus getStatus() { return status; }
    public TicketPriority getPriority() { return priority; }
    public long getAuthorId() { return authorId; }
    public Long getAssigneeId() { return assigneeId; }
    public Long getCategoryId() { return categoryId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public OffsetDateTime getDueAt() { return dueAt; }

    @Override
    public String toString() {
        return "#" + id + " [" + status + "/" + priority + "] " + title
                + " | author=" + authorId + " | assignee=" + assigneeId
                + " | срок=" + dueAt.atZoneSameInstant(ZoneId.systemDefault()).format(DEADLINE_FORMAT);
    }
}
