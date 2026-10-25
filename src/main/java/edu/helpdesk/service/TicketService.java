package edu.helpdesk.service;

import edu.helpdesk.model.*;
import edu.helpdesk.repository.CommentRepository;
import edu.helpdesk.repository.TicketRepository;
import edu.helpdesk.repository.UserRepository;

import java.util.List;

public class TicketService {
    private final TicketRepository tickets;
    private final UserRepository users;
    private final CommentRepository comments;

    public TicketService(TicketRepository tickets, UserRepository users, CommentRepository comments) {
        this.tickets = tickets;
        this.users = users;
        this.comments = comments;
    }

    public Ticket createTicket(String title,
                               String description,
                               TicketPriority priority,
                               long authorId,
                               Long categoryId) {
        if (title == null || title.isBlank()) throw new IllegalArgumentException("Тема обязательна");
        if (description == null || description.isBlank()) throw new IllegalArgumentException("Описание обязательно");
        users.findById(authorId).orElseThrow(() -> new IllegalArgumentException("Автор не найден"));
        return tickets.create(title.trim(), description.trim(), priority, authorId, categoryId);
    }

    public void assignTicket(long ticketId, long assigneeId) {
        tickets.findById(ticketId).orElseThrow(() -> new IllegalArgumentException("Заявка не найдена"));
        User assignee = users.findById(assigneeId).orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));

        if (assignee.getRole() == UserRole.CUSTOMER) {
            throw new IllegalArgumentException(
                    "CUSTOMER нельзя назначить исполнителем"
            );
        }

        int updated = tickets.assign(ticketId, assigneeId);
        if (updated != 1) {
            throw new IllegalStateException("Ожидалось изменение одной заявки, изменено: " + updated);
        }
    }

    public TicketComment addComment(long ticketId, long authorId, String text) {
        tickets.findById(ticketId).orElseThrow(() -> new IllegalArgumentException("Заявка не найдена"));
        users.findById(authorId).orElseThrow(() -> new IllegalArgumentException("Автор комментария не найден"));
        if (text == null || text.isBlank()) throw new IllegalArgumentException("Комментарий пуст");
        return comments.add(ticketId, authorId, text.trim());
    }

    public List<TicketComment> comments(long ticketId) {
        return comments.findByTicketId(ticketId);
    }

    public void changeStatus(long ticketId, TicketStatus newStatus, long changedBy) {
        Ticket ticket = tickets.findById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Заявка не найдена"));
        users.findById(changedBy).orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));
        TicketStatus old = ticket.getStatus();
        ticket.transitionTo(newStatus);
        tickets.changeStatusAndRecordHistory(ticketId, old, newStatus, changedBy);
    }

    public List<Ticket> findAll() { return tickets.findAll(); }
    public List<Ticket> findByStatus(TicketStatus status) { return tickets.findByStatus(status); }
    public List<Ticket> search(String query) { return tickets.searchByTitle(query); }
    public Ticket find(long id) { return tickets.findById(id).orElseThrow(() -> new IllegalArgumentException("Заявка не найдена")); }
}
