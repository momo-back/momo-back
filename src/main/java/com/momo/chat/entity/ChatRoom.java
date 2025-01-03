package com.momo.chat.entity;

import com.momo.common.entity.BaseEntity;
import com.momo.meeting.entity.Meeting;
import com.momo.user.entity.User;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name="chat_room")
public class ChatRoom extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id")
  private User host; // 채팅 개설자

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "meeting_id")
  private Meeting meeting;

  @ManyToMany
  @JoinTable(
      name = "chat_room_reader",
      joinColumns = @JoinColumn(name = "chat_room_id"),
      inverseJoinColumns = @JoinColumn(name = "user_id")
  )
  private List<User> reader = new ArrayList<>();  //채팅방에 입장할 인원들

  @Builder.Default
  @OneToMany(mappedBy = "chatRoom", orphanRemoval = true)
  private List<Chat> ChatMessages = new ArrayList<>();


}