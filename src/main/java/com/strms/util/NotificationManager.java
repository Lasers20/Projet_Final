package com.strms.util;

import com.strms.enums.NotificationType;
import com.strms.model.User;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class NotificationManager {

    private static final DateTimeFormatter F =
            DateTimeFormatter.ofPattern("HH:mm:ss");

    public void notifyUser(User user, NotificationType type, String message) {
        String time = LocalDateTime.now().format(F);
        switch (type) {
            case EMAIL:
                System.out.printf("[%s][EMAIL  -> %s] %s%n",
                        time, user.getEmail(), message);
                break;
            case SMS:
                System.out.printf("[%s][SMS    -> %s] %s%n",
                        time, user.getName(), message);
                break;
            case CONSOLE:
            default:
                System.out.printf("[%s][CONSOLE -> %s] %s%n",
                        time, user.getName(), message);
        }
    }
}
