package edu.helpdesk.repository;

import edu.helpdesk.model.Ticket;
import edu.helpdesk.model.TicketPriority;
import edu.helpdesk.model.TicketStatus;

import java.util.List;
import java.util.Optional;

public interface TicketRepository {
    Ticket create(String title,
                  String description,
                  TicketPriority priority,
                  long authorId,
                  Long categoryId);

    Optional<Ticket> findById(long id);

    List<Ticket> findAll();

    List<Ticket> findByStatus(TicketStatus status);

    List<Ticket> searchByTitle(String query);

    int assign(long ticketId, long assigneeId);

    void changeStatusAndRecordHistory(long ticketId,
                                      TicketStatus oldStatus,
                                      TicketStatus newStatus,
                                      long changedBy);
}
