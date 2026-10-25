package edu.helpdesk;

import edu.helpdesk.config.Database;
import edu.helpdesk.model.Ticket;
import edu.helpdesk.model.TicketPriority;
import edu.helpdesk.model.TicketStatus;
import edu.helpdesk.repository.jdbc.JdbcCommentRepository;
import edu.helpdesk.repository.jdbc.JdbcTicketRepository;
import edu.helpdesk.repository.jdbc.JdbcUserRepository;
import edu.helpdesk.service.TicketService;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

public class SlaDiagnostics {
    private final Clock clock = Clock.fixed(
            Instant.parse("2030-01-15T09:00:00Z"), ZoneId.of("Europe/Moscow"));
    private final OffsetDateTime now = OffsetDateTime.now(clock);
    private final TicketService service = new TicketService(
            new JdbcTicketRepository(), new JdbcUserRepository(), new JdbcCommentRepository(), clock);
    private final List<Long> temporaryTicketIds = new ArrayList<>();
    private int passed;
    private int failed;

    public static void main(String[] args) {
        new SlaDiagnostics().run();
    }

    private void run() {
        System.out.println("=== SLA DIAGNOSTICS ===");
        check("Сроки CRITICAL/HIGH/MEDIUM/LOW: 1/4/8/24 часа и сохранение в БД", this::deadlinesByPriority);
        check("Просрочены только открытые заявки со сроком строго раньше текущего времени", this::overdueBoundaries);
        check("RESOLVED и CLOSED не считаются просроченными", this::finishedTicketsExcluded);
        check("Решённая заявка исчезает из списка просроченных", this::resolvedTicketLeavesOverdueList);
        System.out.println("\nИтог SLA: PASS=" + passed + ", FAIL=" + failed);
        if (failed > 0) throw new IllegalStateException("Не пройдено SLA-проверок: " + failed);
    }

    private void deadlinesByPriority() {
        TicketPriority[] priorities = {
                TicketPriority.CRITICAL, TicketPriority.HIGH, TicketPriority.MEDIUM, TicketPriority.LOW
        };
        int[] expectedHours = {1, 4, 8, 24};
        for (int i = 0; i < priorities.length; i++) {
            Ticket created = createTicket(priorities[i], "срок " + priorities[i]);
            OffsetDateTime expected = now.plusHours(expectedHours[i]);
            require(created.getDueAt().isEqual(expected), "Неверный срок для " + priorities[i]);
            require(service.find(created.getId()).getDueAt().isEqual(expected),
                    "Срок не сохранился при повторном чтении для " + priorities[i]);
        }
    }

    private void overdueBoundaries() throws Exception {
        long older = fixture(TicketStatus.NEW, now.minusHours(2));
        long newer = fixture(TicketStatus.IN_PROGRESS, now.minusHours(1));
        long future = fixture(TicketStatus.NEW, now.plusHours(1));
        long boundary = fixture(TicketStatus.NEW, now);

        List<Ticket> overdue = service.findOverdue();
        List<Long> ids = overdue.stream().map(Ticket::getId).toList();
        require(ids.contains(older) && ids.contains(newer), "Не найдены просроченные NEW/IN_PROGRESS");
        require(!ids.contains(future), "Заявка с будущим сроком попала в просроченные");
        require(!ids.contains(boundary), "Срок, равный текущему времени, ещё не просрочен");
        require(ids.indexOf(older) < ids.indexOf(newer), "Сначала должны идти заявки с более ранним сроком");
    }

    private void finishedTicketsExcluded() throws Exception {
        long resolved = fixture(TicketStatus.RESOLVED, now.minusDays(1));
        long closed = fixture(TicketStatus.CLOSED, now.minusDays(1));
        List<Long> ids = service.findOverdue().stream().map(Ticket::getId).toList();
        require(!ids.contains(resolved) && !ids.contains(closed), "Завершённая заявка попала в просроченные");
    }

    private void resolvedTicketLeavesOverdueList() throws Exception {
        long ticketId = fixture(TicketStatus.NEW, now.minusHours(1));
        require(containsOverdue(ticketId), "Исходная открытая заявка должна быть просрочена");
        service.changeStatus(ticketId, TicketStatus.IN_PROGRESS, 2);
        require(containsOverdue(ticketId), "Заявка в работе должна оставаться просроченной");
        service.changeStatus(ticketId, TicketStatus.RESOLVED, 2);
        require(!containsOverdue(ticketId), "Решённая заявка осталась в списке просроченных");
        service.changeStatus(ticketId, TicketStatus.CLOSED, 2);
        require(!containsOverdue(ticketId), "Закрытая заявка осталась в списке просроченных");
    }

    private boolean containsOverdue(long ticketId) {
        return service.findOverdue().stream().anyMatch(ticket -> ticket.getId() == ticketId);
    }

    private Ticket createTicket(TicketPriority priority, String title) {
        Ticket ticket = service.createTicket("[SLA TEST] " + title, "Временная диагностическая заявка",
                priority, 1, null);
        temporaryTicketIds.add(ticket.getId());
        return ticket;
    }

    private long fixture(TicketStatus status, OffsetDateTime dueAt) throws Exception {
        Ticket ticket = createTicket(TicketPriority.LOW, status.name());
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement("UPDATE tickets SET status = ?, due_at = ? WHERE id = ?")) {
            ps.setString(1, status.name());
            ps.setObject(2, dueAt);
            ps.setLong(3, ticket.getId());
            require(ps.executeUpdate() == 1, "Не удалось подготовить тестовую заявку");
        }
        return ticket.getId();
    }

    private void cleanup() throws Exception {
        if (temporaryTicketIds.isEmpty()) return;
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM tickets WHERE id = ?")) {
            for (long id : temporaryTicketIds) {
                ps.setLong(1, id);
                ps.executeUpdate();
            }
        }
        temporaryTicketIds.clear();
    }

    private void check(String name, CheckedAction action) {
        try {
            try {
                action.run();
            } finally {
                cleanup();
            }
            passed++;
            System.out.println("[PASS] " + name);
        } catch (Exception | AssertionError e) {
            failed++;
            System.out.println("[FAIL] " + name + " -> " + e.getMessage());
        }
    }

    private void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    @FunctionalInterface
    private interface CheckedAction {
        void run() throws Exception;
    }
}
