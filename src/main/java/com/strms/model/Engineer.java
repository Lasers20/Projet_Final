package com.strms.model;

public class Engineer extends User {
    private static final long serialVersionUID = 1L;

    public Engineer(String id, String name, String email) {
        super(id, name, email);
    }

    @Override public boolean canCreateTask()     { return false; }
    @Override public boolean canDeleteTask()     { return false; }
    @Override public boolean canAssignTask()     { return false; }
    @Override public boolean canExecuteTask()    { return true; }
    @Override public boolean canGenerateReport() { return false; }

    @Override public String getRole() { return "Engineer"; }
}
