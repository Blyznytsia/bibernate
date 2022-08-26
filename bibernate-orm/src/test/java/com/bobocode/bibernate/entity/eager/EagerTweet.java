package com.bobocode.bibernate.entity.eager;

import com.bobocode.bibernate.annotation.Column;
import com.bobocode.bibernate.annotation.Entity;
import com.bobocode.bibernate.annotation.Id;
import com.bobocode.bibernate.annotation.JoinColumn;
import com.bobocode.bibernate.annotation.ManyToOne;
import com.bobocode.bibernate.annotation.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "tweets")
@Getter
@Setter
@ToString(exclude = "user")
public class EagerTweet {

  @Id private Long id;

  @Column(name = "tweet_text")
  private String tweetText;
  //
  //  @Column(name = "created_at")
  //  private LocalDate createdAt;

  @ManyToOne
  @JoinColumn(name = "user_id")
  private EagerUser user;
}
