package tech.makers.aceplay.playlist;

import com.fasterxml.jackson.annotation.JsonGetter;
import tech.makers.aceplay.track.Track;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import java.util.Set;

// https://www.youtube.com/watch?v=vreyOZxdb5Y&t=448s
@Entity
public class Playlist {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  @NotBlank(message = "Blank playlist names are not permitted.")
  private String name;

  private Boolean isCool;

  @ManyToMany(fetch = FetchType.EAGER)
  private Set<Track> tracks;

  public Playlist() {}

  public Playlist(String name, Boolean isCool) {
    this(name, isCool, null);
  }

  public Playlist(String name, Boolean isCool, Set<Track> tracks) {
    this.name = name;
    this.tracks = tracks;
    this.isCool = isCool;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Boolean getIsCool(){
    return isCool;
  }

  public void setCool(Boolean isCool){
    this.isCool = isCool;
  }

  public Long getId() {
    return id;
  }

  @JsonGetter("tracks")
  public Set<Track> getTracks() {
    if (null == tracks) {
      return Set.of();
    }
    return tracks;
  }

  @Override
  public String toString() {
    return String.format("Playlist[id=%d name='%s']", id, name);
  }
}
