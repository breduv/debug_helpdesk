package edu.helpdesk;

import edu.helpdesk.exception.DataAccessException;
import edu.helpdesk.model.TicketPriority;
import edu.helpdesk.model.TicketStatus;
import edu.helpdesk.repository.CommentRepository;
import edu.helpdesk.repository.TicketRepository;
import edu.helpdesk.repository.UserRepository;
import edu.helpdesk.repository.jdbc.JdbcCommentRepository;
import edu.helpdesk.repository.jdbc.JdbcTicketRepository;
import edu.helpdesk.repository.jdbc.JdbcUserRepository;
import edu.helpdesk.service.TicketService;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        TicketRepository tickets = new JdbcTicketRepository();
        UserRepository users = new JdbcUserRepository();
        CommentRepository comments = new JdbcCommentRepository();
        TicketService service = new TicketService(tickets, users, comments);

        System.out.println("=== HELP DESK / DEBUG LAB ===");
        System.out.println("Учебный проект. Некоторые сценарии работают неверно — это часть задания.\n");

        try (Scanner scanner = new Scanner(System.in)) {
            boolean running = true;
            while (running) {
                printMenu();
                System.out.print("> ");
                String command = scanner.nextLine().trim();
                try {
                    switch (command) {
                        case "1" -> service.findAll().forEach(System.out::println);
                        case "2" -> {
                            System.out.print("ID заявки: ");
                            System.out.println(service.find(Long.parseLong(scanner.nextLine())));
                        }
                        case "3" -> {
                            System.out.print("Статус (NEW/IN_PROGRESS/RESOLVED/CLOSED): ");
                            service.findByStatus(TicketStatus.valueOf(scanner.nextLine().trim().toUpperCase()))
                                    .forEach(System.out::println);
                        }
                        case "4" -> {
                            System.out.print("Фрагмент темы: ");
                            service.search(scanner.nextLine()).forEach(System.out::println);
                        }
                        case "5" -> {
                            System.out.print("ID заявки: ");
                            long ticketId = Long.parseLong(scanner.nextLine());
                            System.out.print("ID исполнителя: ");
                            long assignee = Long.parseLong(scanner.nextLine());
                            service.assignTicket(ticketId, assignee);
                            System.out.println("Исполнитель назначен.");
                        }
                        case "6" -> {
                            System.out.print("ID заявки: ");
                            long ticketId = Long.parseLong(scanner.nextLine());
                            System.out.print("ID автора: ");
                            long authorId = Long.parseLong(scanner.nextLine());
                            System.out.print("Комментарий: ");
                            System.out.println(service.addComment(ticketId, authorId, scanner.nextLine()));
                        }
                        case "7" -> {
                            System.out.print("ID заявки: ");
                            service.comments(Long.parseLong(scanner.nextLine())).forEach(System.out::println);
                        }
                        case "8" -> {
                            System.out.print("ID заявки: ");
                            long ticketId = Long.parseLong(scanner.nextLine());
                            System.out.print("Новый статус: ");
                            TicketStatus status = TicketStatus.valueOf(scanner.nextLine().trim().toUpperCase());
                            System.out.print("ID пользователя, меняющего статус: ");
                            long actor = Long.parseLong(scanner.nextLine());
                            service.changeStatus(ticketId, status, actor);
                            System.out.println("Статус изменён.");
                        }
                        case "9" -> {
                            System.out.print("Тема: ");
                            String title = scanner.nextLine();
                            System.out.print("Описание: ");
                            String description = scanner.nextLine();
                            System.out.print("Приоритет (LOW/MEDIUM/HIGH/CRITICAL): ");
                            TicketPriority priority = TicketPriority.valueOf(scanner.nextLine().trim().toUpperCase());
                            System.out.print("ID автора: ");
                            long authorId = Long.parseLong(scanner.nextLine());
                            System.out.print("ID категории (Enter = без категории): ");
                            String categoryRaw = scanner.nextLine().trim();
                            Long categoryId = categoryRaw.isBlank() ? null : Long.parseLong(categoryRaw);
                            System.out.println(service.createTicket(title, description, priority, authorId, categoryId));
                        }
                        case "0" -> running = false;
                        default -> System.out.println("Неизвестная команда.");
                    }
                } catch (DataAccessException e) {
                    System.out.println("Ошибка доступа к данным: " + e.getMessage());
                    if (e.getCause() != null) System.out.println("Причина: " + e.getCause().getMessage());
                } catch (Exception e) {
                    System.out.println("Ошибка: " + e.getMessage());
                }
                System.out.println();
            }
        }
    }

    private static void printMenu() {
        System.out.println("""
                1 — список заявок
                2 — заявка по ID
                3 — заявки по статусу
                4 — поиск по теме
                5 — назначить исполнителя
                6 — добавить комментарий
                7 — комментарии заявки
                8 — изменить статус
                9 — создать заявку
                0 — выход
                """);
    }
}
