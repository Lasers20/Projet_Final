package com.strms;

import com.strms.enums.PriorityLevel;
import com.strms.enums.TaskCategory;
import com.strms.enums.TaskStatus;
import com.strms.exceptions.*;
import com.strms.manager.TaskManager;
import com.strms.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class TaskManagerTest {

    private TaskManager manager;
    private Admin admin;
    private Manager mgr;
    private Engineer eng;
    private Engineer eng2;

    @BeforeEach
    void setUp() {
        manager = new TaskManager();
        admin = new Admin("U1", "Alice", "alice@strms.io");
        mgr   = new com.strms.model.Manager("U2", "Bob", "bob@strms.io");
        eng   = new Engineer("U3", "Charlie", "charlie@strms.io");
        eng2  = new Engineer("U4", "Diana", "diana@strms.io");
        manager.registerUser(admin);
        manager.registerUser(mgr);
        manager.registerUser(eng);
        manager.registerUser(eng2);
    }

    private Task makeTask(String id) {
        return new Task(id, "Title " + id, "Desc",
                PriorityLevel.MEDIUM, TaskCategory.FEATURE,
                LocalDate.now().plusDays(7));
    }

    @Test
    @DisplayName("Admin can create a task")
    void adminCanCreateTask() throws Exception {
        Task t = makeTask("T1");
        manager.addTask(t, admin);
        assertEquals(t, manager.findTask("T1"));
    }

    @Test
    @DisplayName("Engineer cannot create a task")
    void engineerCannotCreate() {
        assertThrows(InvalidRoleException.class,
                () -> manager.addTask(makeTask("T1"), eng));
    }

    @Test
    @DisplayName("Duplicate task is rejected")
    void duplicateRejected() throws Exception {
        manager.addTask(makeTask("T1"), admin);
        assertThrows(DuplicateTaskException.class,
                () -> manager.addTask(makeTask("T1"), admin));
    }

    @Test
    @DisplayName("Adding a valid linear dependency succeeds")
    void linearDependencyOk() throws Exception {
        manager.addTask(makeTask("A"), admin);
        manager.addTask(makeTask("B"), admin);
        manager.addTask(makeTask("C"), admin);
        manager.addDependency("B", "A", admin);
        manager.addDependency("C", "B", admin);
        assertEquals(1, manager.findTask("B").getDependencies().size());
        assertEquals(1, manager.findTask("C").getDependencies().size());
    }

    @Test
    @DisplayName("Self-dependency is rejected")
    void selfDependencyRejected() throws Exception {
        manager.addTask(makeTask("A"), admin);
        assertThrows(CircularDependencyException.class,
                () -> manager.addDependency("A", "A", admin));
    }

    @Test
    @DisplayName("Circular dependency is detected and rejected")
    void circularRejected() throws Exception {
        manager.addTask(makeTask("A"), admin);
        manager.addTask(makeTask("B"), admin);
        manager.addTask(makeTask("C"), admin);
        manager.addDependency("B", "A", admin);
        manager.addDependency("C", "B", admin);

        assertThrows(CircularDependencyException.class,
                () -> manager.addDependency("A", "C", admin));

        assertEquals(0, manager.findTask("A").getDependencies().size(),
                "Graph integrity must be preserved after rejection.");
    }

    @Test
    @DisplayName("Task cannot start when dependencies are not DONE")
    void cannotStartIfDepNotDone() throws Exception {
        manager.addTask(makeTask("A"), admin);
        manager.addTask(makeTask("B"), admin);
        manager.addDependency("B", "A", admin);
        manager.assignTask("B", eng, mgr);

        assertThrows(DependencyNotCompletedException.class,
                () -> manager.startTask("B", eng));
        assertEquals(TaskStatus.BLOCKED, manager.findTask("B").getStatus());
    }

    @Test
    @DisplayName("Complete -> dependent task becomes ready")
    void completeUnblocksDependent() throws Exception {
        manager.addTask(makeTask("A"), admin);
        manager.addTask(makeTask("B"), admin);
        manager.addDependency("B", "A", admin);

        manager.assignTask("A", eng, mgr);
        manager.startTask("A", eng);
        manager.completeTask("A", eng);

        assertEquals(TaskStatus.DONE, manager.findTask("A").getStatus());
        assertEquals(TaskStatus.TODO, manager.findTask("B").getStatus());
    }

    @Test
    @DisplayName("Only assigned engineer can complete a task")
    void onlyAssignedCanComplete() throws Exception {
        manager.addTask(makeTask("A"), admin);
        manager.assignTask("A", eng, mgr);
        manager.startTask("A", eng);
        assertThrows(InvalidRoleException.class,
                () -> manager.completeTask("A", eng2));
    }

    @Test
    @DisplayName("Removing dependency unblocks BLOCKED task")
    void removingDependencyUnblocks() throws Exception {
        manager.addTask(makeTask("A"), admin);
        manager.addTask(makeTask("B"), admin);
        manager.addDependency("B", "A", admin);
        assertEquals(TaskStatus.BLOCKED, manager.findTask("B").getStatus());

        manager.removeDependency("B", "A", admin);
        assertEquals(TaskStatus.TODO, manager.findTask("B").getStatus());
    }

    @Test
    @DisplayName("Invalid state transition DONE -> IN_PROGRESS")
    void invalidStateTransition() throws Exception {
        manager.addTask(makeTask("A"), admin);
        manager.assignTask("A", eng, mgr);
        manager.startTask("A", eng);
        manager.completeTask("A", eng);
        Task t = manager.findTask("A");
        assertThrows(InvalidTaskStateException.class,
                () -> t.updateStatus(TaskStatus.IN_PROGRESS));
    }

    @Test
    @DisplayName("findTask throws when task missing")
    void findTaskMissing() {
        assertThrows(TaskNotFoundException.class,
                () -> manager.findTask("UNKNOWN"));
    }

    @Test
    @DisplayName("History entries are recorded on operations")
    void historyRecorded() throws Exception {
        manager.addTask(makeTask("A"), admin);
        manager.assignTask("A", eng, mgr);
        Task t = manager.findTask("A");
        assertTrue(t.getHistory().size() >= 2);
    }

    @Test
    @DisplayName("Priority order in ready queue")
    void priorityOrdering() throws Exception {
        Task low  = new Task("L", "Low",  "", PriorityLevel.LOW,
                TaskCategory.FEATURE, LocalDate.now().plusDays(5));
        Task high = new Task("H", "High", "", PriorityLevel.CRITICAL,
                TaskCategory.FEATURE, LocalDate.now().plusDays(5));
        manager.addTask(low, admin);
        manager.addTask(high, admin);
        assertEquals("H", manager.getReadyTasksByPriority().get(0).getId());
    }
}
