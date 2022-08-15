package com.bobocode.entities;

import com.bobocode.bibernate.annotation.Entity;
import com.bobocode.bibernate.annotation.Id;
import com.bobocode.bibernate.annotation.JoinColumn;
import com.bobocode.bibernate.annotation.OneToMany;
import com.bobocode.bibernate.annotation.Table;
import com.bobocode.bibernate.enums.FetchType;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "users")
@Getter
@Setter
@ToString(exclude = "tweets")
public class User {

  @Id private Long id;
  private String name;
  private String handle;

  @OneToMany(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id")
  private List<Tweet> tweets;
}
