package tech.makers.aceplay.playlist;

import org.junit.jupiter.api.Test;
import tech.makers.aceplay.playlist.Playlist;

import java.net.MalformedURLException;
import java.util.Set;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

// https://www.youtube.com/watch?v=L4vkcgRnw2g&t=1099s
class PlaylistTest {
  @Test
  void testConstructs() {
    Playlist subject = new Playlist("Hello, world!", false, List.of());
    
    assertEquals("Hello, world!", subject.getName());

    assertEquals(List.of(), subject.getTracks());
    
    assertEquals(null, subject.getId());

  }

  @Test
  void testToString() {
    Playlist subject = new Playlist("Hello, world!", false);
    assertEquals(
        "Playlist[id=null name='Hello, world!' cool='false']",
        subject.toString());
  }
}

