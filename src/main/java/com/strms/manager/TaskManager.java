package com.strms.manager;

import com.strms.enums.PriorityLevel;
import com.strms.enums.TaskStatus;
import com.strms.exceptions.*;
import com.strms.model.*;

import java.io.*;
import java.util.*;

public class TaskManager {

    private final HashMap<String, Task> tasks = new HashMap<>();
    private final HashMap<String, User> users = new HashMap<>();
    private final HashSet<Task> inProgress = new HashSet<>();
    private final PriorityQueue<Task> readyQueue = new PriorityQueue<>();

    public void registerUser(User u) {
        users.put(u.getId(), u);
    }

    public User findUser(String userId) {
        return users.get(userId);
    }

    public Collection<User> listUsers() {
        return users.values();
    }

    public Collection<Task> listTasks() {
        return tasks.values();
    }

    public Task findTask(String taskId) throws TaskNotFoundException {
        Task t = tasks.get(taskId);
        if (t == null) {
            throw new TaskNotFoundException("Task '" + taskId + "' not found.");
        }
        return t;
    }

    public void addTask(Task task, User requester)
            throws InvalidRoleException, DuplicateTaskException {

        if (requester == null || !requester.canCreateTask()) {
            throw new InvalidRoleException(
                    "User does not have permission to create tasks.");
        }
        if (tasks.containsKey(task.getId())) {
            throw new DuplicateTaskException(
                    "Task with id '" + task.getId() + "' already exists.");
        }
        tasks.put(task.getId(), task);
        if (task.getStatus() == TaskStatus.TODO && task.allDependenciesDone()) {
            readyQueue.offer(task);
        }
        task.addHistoryEntry(new TaskHistoryEntry(
                "CREATE", requester.getName(),
                "Task created with status " + task.getStatus()));
    }

    public void deleteTask(String taskId, User requester)
            throws TaskNotFoundException, InvalidRoleException {

        if (requester == null || !requester.canDeleteTask()) {
            throw new InvalidRoleException(
                    "User does not have permission to delete tasks.");
        }
        Task t = findTask(taskId);
        tasks.remove(taskId);
        inProgress.remove(t);
        readyQueue.remove(t);
        for (Task other : tasks.values()) {
            other.removeDependencyInternal(t);
        }
        t.addHistoryEntry(new TaskHistoryEntry(
                "DELETE", requester.getName(), "Task deleted."));
    }

    public void assignTask(String taskId, Engineer engineer, User requester)
            throws TaskNotFoundException, InvalidRoleException {

        if (requester == null || !requester.canAssignTask()) {
            throw new InvalidRoleException(
                    "User does not have permission to assign tasks.");
        }
        Task t = findTask(taskId);
        t.setEngineer(engineer);
        t.addHistoryEntry(new TaskHistoryEntry(
                "ASSIGN", requester.getName(),
                "Task assigned to " + engineer.getName()));
    }

    public void startTask(String taskId, User requester)
            throws TaskNotFoundException, InvalidRoleException,
                   DependencyNotCompletedException, InvalidTaskStateException {

        Task t = findTask(taskId);
        if (requester == null || !requester.canExecuteTask()) {
            throw new InvalidRoleException(
                    "Only engineers can start a task.");
        }
        if (t.getEngineer() == null || !t.getEngineer().equals(requester)) {
            throw new InvalidRoleException(
                    "Only the assigned engineer can start this task.");
        }
        if (!t.allDependenciesDone()) {
            t.forceStatus(TaskStatus.BLOCKED);
            t.addHistoryEntry(new TaskHistoryEntry(
                    "BLOCKED", requester.getName(),
                    "Cannot start: prerequisite tasks are not all DONE."));
            throw new DependencyNotCompletedException(
                    "Task '" + t.getId() + "' has incomplete dependencies.");
        }
        t.updateStatus(TaskStatus.IN_PROGRESS);
        inProgress.add(t);
        readyQueue.remove(t);
        t.addHistoryEntry(new TaskHistoryEntry(
                "START", requester.getName(),
                "Task moved to IN_PROGRESS."));
    }

    public void completeTask(String taskId, User requester)
            throws TaskNotFoundException, InvalidRoleException,
                   DependencyNotCompletedException, InvalidTaskStateException {

        Task t = findTask(taskId);
        if (requester == null || !requester.canExecuteTask()) {
            throw new InvalidRoleException(
                    "Only engineers can complete a task.");
        }
        if (t.getEngineer() == null || !t.getEngineer().equals(requester)) {
            throw new InvalidRoleException(
                    "Only the assigned engineer can complete this task.");
        }
        if (!t.allDependenciesDone()) {
            throw new DependencyNotCompletedException(
                    "Task '" + t.getId() + "' has incomplete dependencies.");
        }
        t.updateStatus(TaskStatus.DONE);
        inProgress.remove(t);
        t.addHistoryEntry(new TaskHistoryEntry(
                "COMPLETE", requester.getName(),
                "Task marked DONE."));
        unblockDependents(t);
    }

    private void unblockDependents(Task completed) {
        for (Task t : tasks.values()) {
            if (t.getDependencies().contains(completed)
                    && t.getStatus() == TaskStatus.BLOCKED
                    && t.allDependenciesDone()) {
                t.forceStatus(TaskStatus.TODO);
                readyQueue.offer(t);
                t.addHistoryEntry(new TaskHistoryEntry(
                        "UNBLOCK", "system",
                        "All dependencies completed."));
            }
        }
    }

    public void addDependency(String taskId, String dependsOnId, User requester)
            throws TaskNotFoundException, CircularDependencyException,
                   InvalidRoleException {

        if (requester == null || !requester.canCreateTask()) {
            throw new InvalidRoleException(
                    "User does not have permission to modify dependencies.");
        }
        Task t  = findTask(taskId);
        Task dp = findTask(dependsOnId);

        if (t.equals(dp)) {
            throw new CircularDependencyException(
                    "A task cannot depend on itself.");
        }
        if (detectCircularDependency(t, dp)) {
            t.addHistoryEntry(new TaskHistoryEntry(
                    "DEPENDENCY_REJECTED", requester.getName(),
                    "Rejected dependency " + t.getId() + " -> "
                            + dp.getId() + " (circular)."));
            throw new CircularDependencyException(
                    "Adding dependency " + t.getId() + " -> "
                            + dp.getId() + " would create a cycle.");
        }
        t.addDependencyInternal(dp);
        t.addHistoryEntry(new TaskHistoryEntry(
                "ADD_DEPENDENCY", requester.getName(),
                t.getId() + " now depends on " + dp.getId()));

        if (!t.allDependenciesDone() && t.getStatus() == TaskStatus.TODO) {
            t.forceStatus(TaskStatus.BLOCKED);
            readyQueue.remove(t);
            t.addHistoryEntry(new TaskHistoryEntry(
                    "BLOCK", "system", "Dependency added: prerequisite not DONE."));
        }
    }

    public void removeDependency(String taskId, String dependsOnId, User requester)
            throws TaskNotFoundException, InvalidRoleException {

        if (requester == null || !requester.canCreateTask()) {
            throw new InvalidRoleException(
                    "User does not have permission to modify dependencies.");
        }
        Task t  = findTask(taskId);
        Task dp = findTask(dependsOnId);
        t.removeDependencyInternal(dp);
        t.addHistoryEntry(new TaskHistoryEntry(
                "REMOVE_DEPENDENCY", requester.getName(),
                t.getId() + " no longer depends on " + dp.getId()));

        if (t.getStatus() == TaskStatus.BLOCKED && t.allDependenciesDone()) {
            t.forceStatus(TaskStatus.TODO);
            readyQueue.offer(t);
            t.addHistoryEntry(new TaskHistoryEntry(
                    "UNBLOCK", "system",
                    "All remaining dependencies are DONE."));
        }
    }

    public boolean detectCircularDependency(Task source, Task target) {
        Set<Task> visited = new HashSet<>();
        return dfsHasPath(target, source, visited);
    }

    private boolean dfsHasPath(Task current, Task goal, Set<Task> visited) {
        if (current.equals(goal)) return true;
        if (!visited.add(current)) return false;
        for (Task next : current.getDependencies()) {
            if (dfsHasPath(next, goal, visited)) return true;
        }
        return false;
    }

    public void changeTaskPriority(String taskId, PriorityLevel level, User requester)
            throws TaskNotFoundException, InvalidRoleException {

        if (requester == null
                || (!requester.canCreateTask() && !requester.canAssignTask())) {
            throw new InvalidRoleException(
                    "User cannot change task priority.");
        }
        Task t = findTask(taskId);
        PriorityLevel old = t.getPriority();
        t.changePriority(level);
        if (readyQueue.remove(t)) readyQueue.offer(t);
        t.addHistoryEntry(new TaskHistoryEntry(
                "CHANGE_PRIORITY", requester.getName(),
                "Priority changed from " + old + " to " + level));
    }

    public List<Task> getReadyTasksByPriority() {
        List<Task> ordered = new ArrayList<>();
        PriorityQueue<Task> copy = new PriorityQueue<>(readyQueue);
        while (!copy.isEmpty()) ordered.add(copy.poll());
        return ordered;
    }

    public Set<Task> getInProgressTasks() {
        return new HashSet<>(inProgress);
    }

    public void printInProgressTasks() {
        System.out.println("--- In Progress Tasks ---");
        for (Task t : inProgress) System.out.println(t);
    }

    public void saveTasksToFile(String path) throws FilePersistenceException {
        try (ObjectOutputStream out =
                     new ObjectOutputStream(new FileOutputStream(path))) {
            out.writeObject(tasks);
        } catch (IOException e) {
            throw new FilePersistenceException(
                    "Failed to save tasks to '" + path + "'.", e);
        }
    }

    @SuppressWarnings("unchecked")
    public void loadTasksFromFile(String path) throws FilePersistenceException {
        try (ObjectInputStream in =
                     new ObjectInputStream(new FileInputStream(path))) {
            HashMap<String, Task> loaded =
                    (HashMap<String, Task>) in.readObject();
            tasks.clear();
            tasks.putAll(loaded);
            inProgress.clear();
            readyQueue.clear();
            for (Task t : tasks.values()) {
                if (t.getStatus() == TaskStatus.IN_PROGRESS) inProgress.add(t);
                else if (t.getStatus() == TaskStatus.TODO
                        && t.allDependenciesDone()) readyQueue.offer(t);
            }
        } catch (IOException | ClassNotFoundException e) {
            throw new FilePersistenceException(
                    "Failed to load tasks from '" + path + "'.", e);
        }
    }

    public void saveUsersToFile(String path) throws FilePersistenceException {
        try (ObjectOutputStream out =
                     new ObjectOutputStream(new FileOutputStream(path))) {
            out.writeObject(users);
        } catch (IOException e) {
            throw new FilePersistenceException(
                    "Failed to save users to '" + path + "'.", e);
        }
    }

    @SuppressWarnings("unchecked")
    public void loadUsersFromFile(String path) throws FilePersistenceException {
        try (ObjectInputStream in =
                     new ObjectInputStream(new FileInputStream(path))) {
            HashMap<String, User> loaded =
                    (HashMap<String, User>) in.readObject();
            users.clear();
            users.putAll(loaded);
        } catch (IOException | ClassNotFoundException e) {
            throw new FilePersistenceException(
                    "Failed to load users from '" + path + "'.", e);
        }
    }
}
