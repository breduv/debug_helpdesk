package edu.helpdesk.model;

public class User {
    private final long id;
    private final String fullName;
    private final String email;
    private final UserRole role;
    private final Long departmentId;

    public User(long id, String fullName, String email, UserRole role, Long departmentId) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.role = role;
        this.departmentId = departmentId;
    }

    public long getId() { return id; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public UserRole getRole() { return role; }
    public Long getDepartmentId() { return departmentId; }

    @Override
    public String toString() {
        return "User{" + id + ", " + fullName + ", " + role + "}";
    }
}
