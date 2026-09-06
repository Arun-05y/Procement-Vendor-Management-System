package com.procurea.procurementsystem.service;

import com.procurea.procurementsystem.dto.NotificationDto;

import java.util.List;

public interface NotificationService {
    void sendNotification(Long userId, String message, String type);
    List<NotificationDto> getNotificationsForUser(Long userId, Boolean unreadOnly);
    void markAsRead(Long notificationId);
    void markAllAsRead(Long userId);
}
