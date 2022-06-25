package tech.makers.aceplay.playlist;

import org.hamcrest.collection.IsEmptyCollection;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import tech.makers.aceplay.track.Track;
import tech.makers.aceplay.track.TrackRepository;
import tech.makers.aceplay.user.User;
import tech.makers.aceplay.user.UserRepository;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// https://www.youtube.com/watch?v=L4vkcgRnw2g&t=1156s
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class PlaylistsControllerIntegrationTest {
  @Autowired
  private MockMvc mvc;

  @Autowired
  private TrackRepository trackRepository;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private PlaylistRepository repository;

  @Test
  void WhenLoggedOut_PlaylistsIndexReturnsForbidden() throws Exception {
    mvc.perform(MockMvcRequestBuilders.get("/api/playlists").contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(username = "SSG")
  void WhenLoggedIn_AndThereAreNoPlaylists_PlaylistsIndexReturnsNoTracks()
      throws Exception {
    String username = "SSG";
    String password = "pw";
    User SSG = new User(username, password);
    SSG.setId(2L);
    userRepository.save(SSG);

    mvc.perform(MockMvcRequestBuilders
        .get("/api/playlists")
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$", hasSize(0)));
  }

  @Test
  @WithMockUser
  void WhenLoggedIn_AndThereArePlaylists_PlaylistsReturnsCoolPlaylists() throws Exception {
    repository.save(new Playlist("New Playlist", false));
    repository.save(new Playlist("Another Playlist", true));
    mvc.perform(MockMvcRequestBuilders.get("/api/playlists/cool").contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$", hasSize(1)));
  }

  @Test
  @WithMockUser
  void WhenLoggedIn_AndThereArePlaylists_PlaylistsReturnsUnCoolPlaylists() throws Exception {
    repository.save(new Playlist("New Playlist", false));
    repository.save(new Playlist("Another Playlist", false));
    mvc.perform(MockMvcRequestBuilders.get("/api/playlists/uncool").contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$", hasSize(2)));
  }

  @Test
  @WithMockUser(username = "SSG")
  void WhenLoggedIn_AndThereArePlaylists_PlaylistIndexReturnsTracks() throws Exception {
    String username = "SSG";
    String password = "pw";
    User SSG = new User(username, password);
    SSG.setId(10L);
    userRepository.save(SSG);

    mvc.perform(
        MockMvcRequestBuilders.post("/api/playlists")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"name\": \"My Playlist\"}"));

    mvc.perform(
        MockMvcRequestBuilders.post("/api/tracks")
            .contentType(MediaType.APPLICATION_JSON)
            .content(
                "{\"title\": \"Blue Line Swinger\", \"artist\": \"Yo La Tengo\", \"publicUrl\": \"https://example.org/track.mp3\"}"));

    Playlist playlist = repository.findFirstByOrderByIdAsc();
    Track track = trackRepository.findFirstByOrderByIdAsc();

    mvc.perform(
        MockMvcRequestBuilders.put("/api/playlists/" + playlist.getId() + "/tracks")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"id\": \"" + track.getId() + "\"}"));

    repository.save(new Playlist("Their Playlist", true));

    mvc.perform(MockMvcRequestBuilders.get("/api/playlists").contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$", hasSize(1)))
        .andExpect(jsonPath("$[0].name").value("My Playlist"))
        .andExpect(jsonPath("$[0].tracks[0].title").value("Blue Line Swinger"))
        .andExpect(jsonPath("$[0].tracks[0].artist").value("Yo La Tengo"))
        .andExpect(jsonPath("$[0].tracks[0].publicUrl").value("https://example.org/track.mp3"));
  }

  @Test
  void WhenLoggedOut_PlaylistsGetReturnsForbidden() throws Exception {
    Playlist playlist = repository.save(new Playlist("My Playlist", false));
    mvc.perform(
        MockMvcRequestBuilders.get("/api/playlists/" + playlist.getId()).contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser
  void WhenLoggedIn_AndThereIsNoPlaylist_PlaylistsGetReturnsNotFound() throws Exception {
    mvc.perform(MockMvcRequestBuilders.get("/api/playlists/1").contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound());
  }

  @Test
  @WithMockUser
  void WhenLoggedIn_AndThereIsAPlaylist_PlaylistGetReturnsPlaylist() throws Exception {
    Track track = trackRepository.save(new Track("Title", "Artist", "https://example.org/"));
    Playlist playlist = repository.save(new Playlist("My Playlist", false, List.of(track)));

    mvc.perform(
        MockMvcRequestBuilders.get("/api/playlists/" + playlist.getId()).contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.name").value("My Playlist"))
        .andExpect(jsonPath("$.cool").value(false))
        .andExpect(jsonPath("$.tracks[0].title").value("Title"))
        .andExpect(jsonPath("$.tracks[0].artist").value("Artist"))
        .andExpect(jsonPath("$.tracks[0].publicUrl").value("https://example.org/"));
  }

  @Test
  void WhenLoggedOut_PlaylistPostIsForbidden() throws Exception {
    mvc.perform(
        MockMvcRequestBuilders.post("/api/playlists")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"name\": \"My Playlist Name\"}"))
        .andExpect(status().isForbidden());
    assertEquals(0, repository.count());
  }

  @Test
  @WithMockUser
  void WhenLoggedIn_PlaylistPostCreatesNewPlaylist() throws Exception {
    mvc.perform(
        MockMvcRequestBuilders.post("/api/playlists")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"name\": \"My Playlist Name\"}"))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.name").value("My Playlist Name"))
        .andExpect(jsonPath("$.tracks").value(IsEmptyCollection.empty()));

    Playlist playlist = repository.findFirstByOrderByIdAsc();
    assertEquals("My Playlist Name", playlist.getName());
    assertEquals(List.of(), playlist.getTracks());
  }

  @Test
  void WhenLoggedOut_PlaylistAddTrackIsForbidden() throws Exception {
    Track track = trackRepository.save(new Track("Title", "Artist", "https://example.org/"));
    Playlist playlist = repository.save(new Playlist("My Playlist", false));

    mvc.perform(
        MockMvcRequestBuilders.put("/api/playlists/" + playlist.getId() + "/tracks")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"id\": \"" + track.getId() + "\"}"))
        .andExpect(status().isForbidden());

    Playlist currentPlaylist = repository.findById(playlist.getId()).orElseThrow();
    assertTrue(currentPlaylist.getTracks().isEmpty());
  }

  @Test
  @WithMockUser
  void WhenLoggedIn_TracksPostCreatesNewTrack() throws Exception {
    Track track = trackRepository.save(new Track("Title", "Artist", "https://example.org/"));
    Playlist playlist = repository.save(new Playlist("My Playlist", false));

    mvc.perform(
        MockMvcRequestBuilders.put("/api/playlists/" + playlist.getId() + "/tracks")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"id\": \"" + track.getId() + "\"}"))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.title").value("Title"));

    Playlist updatedPlaylist = repository.findById(playlist.getId()).orElseThrow();

    assertEquals(1, updatedPlaylist.getTracks().size());
    Track includedTrack = updatedPlaylist.getTracks().stream().findFirst().orElseThrow();
    assertEquals(track.getId(), includedTrack.getId());
    assertEquals("Title", includedTrack.getTitle());
  }

  @Test
  @WithMockUser
  void WhenLoggedIn_DeletesTrackFromPlaylist() throws Exception {
    Track track = trackRepository.save(new Track("Title", "Artist", "https://example.org/"));
    Playlist playlist = repository.save(new Playlist("My Playlist", false, List.of(track)));

    assertEquals(1, playlist.getTracks().size());

    mvc.perform(
        MockMvcRequestBuilders.delete("/api/playlists/" + playlist.getId() + "/tracks/" + track.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());

    Playlist updatedPlaylist = repository.findById(playlist.getId()).orElseThrow();

    assertEquals(0, updatedPlaylist.getTracks().size());
  }

  @Test
  @WithMockUser
  void WhenLoggedIn_DeletesPlaylist() throws Exception {
    Playlist playlist = repository.save(new Playlist("My Playlist", false));

    mvc.perform(
        MockMvcRequestBuilders.delete("/api/playlists/" + playlist.getId()))
        .andExpect(status().isOk());

    assertEquals(0, repository.count());
  }

  // Tracks are ordered by latest added in playlist

  @Test
  @WithMockUser(username = "SSG")
  void WhenLoggedIn_TracksPostCreatesNewTracks() throws Exception {

    String username = "SSG";
    String password = "pw";
    User SSG = new User(username, password);
    SSG.setId(2L);
    userRepository.save(SSG);

    Track track1 = trackRepository.save(new Track("Title1", "Artist1",
        "https://example.org/"));
    Track track2 = trackRepository.save(new Track("Title2", "Artist2",
        "https://example.org/"));
    Track track3 = trackRepository.save(new Track("Title3", "Artist3",
        "https://example.org/"));
    Playlist playlist = repository.save(new Playlist("My Playlist", false));

    mvc.perform(
        MockMvcRequestBuilders.put("/api/playlists/" + playlist.getId() + "/tracks")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"id\": \"" + track2.getId() + "\"}"))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.title").value("Title2"));

    mvc.perform(
        MockMvcRequestBuilders.put("/api/playlists/" + playlist.getId() + "/tracks")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"id\": \"" + track1.getId() + "\"}"))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.title").value("Title1"));

    mvc.perform(
        MockMvcRequestBuilders.put("/api/playlists/" + playlist.getId() + "/tracks")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"id\": \"" + track3.getId() + "\"}"))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.title").value("Title3"));

    Playlist updatedPlaylist = repository.findById(playlist.getId()).orElseThrow();

    assertEquals(3, updatedPlaylist.getTracks().size());

    assertEquals(track2.getId(), updatedPlaylist.getTracks().get(0).getId());
    assertEquals(track1.getId(), updatedPlaylist.getTracks().get(1).getId());
    assertEquals(track3.getId(), updatedPlaylist.getTracks().get(2).getId());
  }
}