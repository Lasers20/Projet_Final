package com.strms.model;

public class Manager extends User {
    private static final long serialVersionUID = 1L;

    public Manager(String id, String name, String email) {
        super(id, name, email);
    }

    @Override public boolean canCreateTask()     { return false; }
    @Override public boolean canDeleteTask()     { return false; }
    @Override public boolean canAssignTask()     { return true; }
    @Override public boolean canExecuteTask()    { return false; }
    @Override public boolean canGenerateReport() { return true; }

    @Override public String getRole() { return "Manager"; }
}
