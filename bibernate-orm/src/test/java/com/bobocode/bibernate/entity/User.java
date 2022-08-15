package com.bobocode.bibernate.entity;

import com.bobocode.bibernate.annotation.Entity;
import com.bobocode.bibernate.annotation.Id;
import com.bobocode.bibernate.annotation.OneToMany;
import com.bobocode.bibernate.annotation.Table;
import com.bobocode.bibernate.enums.FetchType;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Entity
@Table(name = "users")
@Getter
@Setter
@ToString(exclude = "tweets")
@Accessors(chain = true)
public class User {

  @Id private Long id;
  private String name;
  private String handle;

  @OneToMany(fetch = FetchType.EAGER)
  private List<Tweet> tweets;
}
