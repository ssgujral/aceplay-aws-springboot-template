package tech.makers.aceplay.playlist;

// https://www.youtube.com/watch?v=vreyOZxdb5Y&t=229s
public class TrackIdentifierDto {
  private Long id;

  public TrackIdentifierDto() {}

  public TrackIdentifierDto(Long id) {
    this.id = id;
  }

  public Long getId() {
    return id;
  }
}