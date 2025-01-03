package com.momo.notification.repository;

import com.momo.notification.entity.Notification;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

  Optional<Notification> findByIdAndReceiver_Id(Long notificationId, Long receiverId);

  List<Notification> findAllByReceiver_Id(Long receiverId);

  void deleteAllByReceiver_Id(Long userId);

  boolean existsByReceiver_Id(Long id);
}
