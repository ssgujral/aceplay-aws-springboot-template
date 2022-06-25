package tech.makers.aceplay.track;

import org.hamcrest.Matchers;
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
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import tech.makers.aceplay.track.Track;
import tech.makers.aceplay.track.TrackRepository;
import tech.makers.aceplay.user.User;
import tech.makers.aceplay.user.UserRepository;
import tech.makers.aceplay.playlist.*;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// https://www.youtube.com/watch?v=L4vkcgRnw2g&t=908s
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class TracksControllerIntegrationTest {
  @Autowired
  private MockMvc mvc;

  @Autowired
  private TrackRepository repository;

  @Autowired
  private PlaylistRepository playlistRepository;

  @Autowired
  private UserRepository userRepository;

  @Test
  @WithMockUser(username = "SSG")
  void WhenLoggedIn_AndThereAreNoTracks_TracksIndexReturnsNoTracks() throws Exception {
    String username = "SSG";
    String password = "pw";
    User SSG = new User(username, password);
    SSG.setId(2L);
    userRepository.save(SSG);

    mvc.perform(MockMvcRequestBuilders.get("/api/tracks").contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$", hasSize(0)));
  }

  @Test
  @WithMockUser(username = "SSG")
  void WhenLoggedIn_AndThereAreTracks_TracksIndexReturnsTracks() throws Exception {
    String username = "SSG";
    String password = "pw";
    User SSG = new User(username, password);
    SSG.setId(10L);
    userRepository.save(SSG);

    mvc.perform(
        MockMvcRequestBuilders.post("/api/tracks")
            .contentType(MediaType.APPLICATION_JSON)
            .content(
                "{\"title\": \"Marching Bands of Manhattan\", \"artist\": \"Death Cab for Cutie\", \"publicUrl\": \"https://example.org\"}"));

    mvc.perform(
        MockMvcRequestBuilders.post("/api/tracks")
            .contentType(MediaType.APPLICATION_JSON)
            .content(
                "{\"title\": \"Soul Meets Body\", \"artist\": \"Artist 2\", \"publicUrl\": \"https://example.org\"}"));

    mvc.perform(MockMvcRequestBuilders.get("/api/tracks").contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$", hasSize(2)))
        .andExpect(jsonPath("$[0].title").value("Marching Bands of Manhattan"))
        .andExpect(jsonPath("$[0].artist").value("Death Cab for Cutie"))
        .andExpect(jsonPath("$[0].publicUrl").value("https://example.org"))
        .andExpect(jsonPath("$[1].title").value("Soul Meets Body"));
  }

  @Test
  void WhenLoggedOut_TracksIndexReturnsForbidden() throws Exception {
    mvc.perform(MockMvcRequestBuilders.get("/api/tracks").contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser
  void WhenLoggedIn_TracksPostCreatesNewTrack() throws Exception {

    mvc.perform(
        MockMvcRequestBuilders.post("/api/tracks")
            .contentType(MediaType.APPLICATION_JSON)
            .content(
                "{\"title\": \"Marching Bands of Manhattan\", \"artist\": \"Death Cab for Cutie\", \"publicUrl\": \"https://example.org\"}"))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.title").value("Marching Bands of Manhattan"))
        .andExpect(jsonPath("$.artist").value("Death Cab for Cutie"))
        .andExpect(jsonPath("$.publicUrl").value("https://example.org"));

    Track track = repository.findFirstByOrderByIdAsc();
    assertEquals("Marching Bands of Manhattan", track.getTitle());
    assertEquals("https://example.org", track.getPublicUrl().toString());
  }

  @Test
  void WhenLoggedOut_TrackPostIsForbidden() throws Exception {
    mvc.perform(
        MockMvcRequestBuilders.post("/api/tracks")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"title\": \"Marching Bands of Manhattan\"}"))
        .andExpect(status().isForbidden());
    assertEquals(0, repository.count());
  }

  @Test
  @WithMockUser
  void WhenLoggedIn_TrackUpdateUpdatesTrack() throws Exception {
    Track track = repository.save(new Track("Marching Bands of Manhattan", "Death Cab for Cutie", "https://example.org"));
    mvc.perform(
        MockMvcRequestBuilders.patch("/api/tracks/" + track.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"title\": \"Postal Service\", \"artist\": \"Such Great Heights\"}"))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.title").value("Postal Service"))
        .andExpect(jsonPath("$.artist").value("Such Great Heights"))
        .andExpect(jsonPath("$.publicUrl").value("https://example.org"));

    Track updatedTrack = repository.findById(track.getId()).orElseThrow();
    assertEquals("Postal Service", updatedTrack.getTitle());
  }

  @Test
  @WithMockUser
  void WhenLoggedIn_ButNoTrack_TrackUpdateThrows404() throws Exception {
    mvc.perform(
        MockMvcRequestBuilders.patch("/api/tracks/1")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"title\": \"Postal Service\"}"))
        .andExpect(status().isNotFound());
  }

  @Test
  void WhenLoggedOut_TrackUpdateIsForbidden() throws Exception {
    Track track = repository.save(new Track("Marching Bands of Manhattan", "Death Cab for Cutie", "http://example.org/"));
    mvc.perform(
        MockMvcRequestBuilders.patch("/api/tracks/" + track.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"title\": \"Postal Service\"}"))
        .andExpect(status().isForbidden());

    Track updatedTrack = repository.findById(track.getId()).orElseThrow();
    assertEquals("Marching Bands of Manhattan", updatedTrack.getTitle());
  }

  @Test
  @WithMockUser
  void WhenLoggedIn_TrackDeleteDeletesTrack() throws Exception {
    Track track = repository.save(new Track("Marching Bands of Manhattan", "Death Cab for Cutie", "https://example.org"));

    mvc.perform(
        MockMvcRequestBuilders.delete("/api/tracks/" + track.getId()))
        .andExpect(status().isOk());

    assertEquals(0, repository.count());
  }

  @Test
  @WithMockUser
  void WhenLoggedIn_ButNoTrack_TrackDeleteThrows404() throws Exception {
    mvc.perform(
        MockMvcRequestBuilders.delete("/api/tracks/1"))
        .andExpect(status().isNotFound());
  }

  @Test
  void WhenLoggedOut_TrackDeleteIsForbidden() throws Exception {
    Track track = repository.save(new Track("Marching Bands of Manhattan", "Death Cab for Cutie", "http://example.org"));

    mvc.perform(
        MockMvcRequestBuilders.delete("/api/tracks/" + track.getId()))
        .andExpect(status().isForbidden());

    assertEquals(1, repository.count());
  }
}