package tech.makers.aceplay.track;

import tech.makers.aceplay.user.User;
import javax.persistence.*;
import java.net.MalformedURLException;
import java.net.URL;
import javax.validation.constraints.NotEmpty;

public class TrackDto {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  @NotEmpty(message = "Title may not be empty")
  private String title;

  @NotEmpty(message = "Artist may not be empty")
  private String artist;

  private URL publicUrl;

  public TrackDto() {
  }

  public TrackDto(@NotEmpty String title, @NotEmpty String artist, URL publicUrl) {
    this.title = title;
    this.artist = artist;
    this.publicUrl = publicUrl;
  }

  public TrackDto(@NotEmpty String title, @NotEmpty String artist, String publicUrl) throws MalformedURLException {
    this(title, artist, new URL(publicUrl));
  }

  public Long getId() {
    return id;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getArtist() {
    return artist;
  }

  public void setArtist(String artist) {
    this.artist = artist;
  }

  public URL getPublicUrl() {
    return publicUrl;
  }

  public void setPublicUrl(String publicUrl) throws MalformedURLException {
    this.publicUrl = new URL(publicUrl);
  }

  public void setPublicUrl(URL publicUrl) {
    this.publicUrl = publicUrl;
  }
}