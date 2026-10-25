package edu.helpdesk.model;

import java.time.LocalDateTime;

public class TicketComment {
    private final long id;
    private final long ticketId;
    private final long authorId;
    private final String text;
    private final LocalDateTime createdAt;

    public TicketComment(long id, long ticketId, long authorId, String text, LocalDateTime createdAt) {
        this.id = id;
        this.ticketId = ticketId;
        this.authorId = authorId;
        this.text = text;
        this.createdAt = createdAt;
    }

    public long getId() { return id; }
    public long getTicketId() { return ticketId; }
    public long getAuthorId() { return authorId; }
    public String getText() { return text; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    @Override
    public String toString() {
        return "comment#" + id + " ticket=" + ticketId + " author=" + authorId + ": " + text;
    }
}
