package com.strms.model;

public class Admin extends User {
    private static final long serialVersionUID = 1L;

    public Admin(String id, String name, String email) {
        super(id, name, email);
    }

    @Override public boolean canCreateTask()     { return true; }
    @Override public boolean canDeleteTask()     { return true; }
    @Override public boolean canAssignTask()     { return true; }
    @Override public boolean canExecuteTask()    { return false; }
    @Override public boolean canGenerateReport() { return true; }

    @Override public String getRole() { return "Admin"; }
}
