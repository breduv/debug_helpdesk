package edu.helpdesk.repository;

import edu.helpdesk.model.TicketComment;

import java.util.List;

public interface CommentRepository {
    TicketComment add(long ticketId, long authorId, String text);
    List<TicketComment> findByTicketId(long ticketId);
}
