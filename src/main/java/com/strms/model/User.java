package com.strms.model;

import java.io.Serializable;
import java.util.Objects;

public abstract class User implements Serializable {
    private static final long serialVersionUID = 1L;

    protected String id;
    protected String name;
    protected String email;

    public User(String id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }

    public void setName(String name) { this.name = name; }
    public void setEmail(String email) { this.email = email; }

    public abstract boolean canCreateTask();
    public abstract boolean canDeleteTask();
    public abstract boolean canAssignTask();
    public abstract boolean canExecuteTask();
    public abstract boolean canGenerateReport();
    public abstract String getRole();

    @Override
    public String toString() {
        return String.format("%s [id=%s, name=%s, email=%s]", getRole(), id, name, email);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        User user = (User) o;
        return Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
