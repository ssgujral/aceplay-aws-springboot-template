package tech.makers.aceplay.playlist;

import java.util.Set;
import tech.makers.aceplay.track.Track;
import tech.makers.aceplay.user.User;
import javax.validation.constraints.NotEmpty;

public class PlaylistDto {
  private Long id;
  private String name;
  private Boolean cool;

  public PlaylistDto() {
  }

  public PlaylistDto(@NotEmpty String name) {
    this(name, false);
  }

  public PlaylistDto(@NotEmpty String name, Boolean isCool) {
    this.name = name;
    this.cool = isCool;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Boolean getCool() {
    return cool;
  }

  public Long getId() {
    return id;
  }

}