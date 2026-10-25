package edu.helpdesk.repository;

import edu.helpdesk.model.User;

import java.util.Optional;

public interface UserRepository {
    Optional<User> findById(long id);
}
