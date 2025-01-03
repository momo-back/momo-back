package com.momo.notification.repository;

import com.momo.notification.entity.Notification;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

  List<Notification> findAllByUser_Id(Long userId);

  int deleteByIdAndUser_Id(Long notificationId, Long userId);

  void deleteAllByUser_Id(Long userId);

  boolean existsByUser_Id(Long userId);
}
