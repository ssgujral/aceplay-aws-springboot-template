package tech.makers.aceplay.playlist;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import tech.makers.aceplay.track.Track;
import tech.makers.aceplay.track.TrackRepository;
import tech.makers.aceplay.user.User;
import tech.makers.aceplay.user.UserRepository;

import static org.springframework.http.HttpStatus.NOT_FOUND;

import java.security.Principal;

// https://www.youtube.com/watch?v=vreyOZxdb5Y&t=0s
@RestController
public class PlaylistsController {
  @Autowired
  private PlaylistRepository playlistRepository;

  @Autowired
  private TrackRepository trackRepository;

  @Autowired
  private UserRepository userRepository;

  @GetMapping("/api/playlists")
  public Iterable<Playlist> playlists(Principal principal) {
    User user = userRepository.findByUsername(principal.getName());
    Long userId = user.getId();
    return playlistRepository.findByUserId(userId);
  }

  @GetMapping("/api/playlists/cool")
  public Iterable<Playlist> coolPlaylists() {
    return playlistRepository.findByCoolEquals(true);
  }

  @GetMapping("/api/playlists/uncool")
  public Iterable<Playlist> uncoolPlaylists() {
    return playlistRepository.findByCoolEquals(false);
  }

  @PostMapping("/api/playlists")
  public Playlist create(Principal principal, @RequestBody PlaylistDto playlistDto) {

    Playlist playlist = new Playlist();
    BeanUtils.copyProperties(playlistDto, playlist);

    User user = userRepository.findByUsername(principal.getName());
    playlist.setUser(user);
    return playlistRepository.save(playlist);
  }

  @GetMapping("/api/playlists/{id}")
  public Playlist get(@PathVariable Long id) {
    return playlistRepository.findById(id)
        .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "No playlist exists with id " + id));
  }

  @PutMapping("/api/playlists/{id}/tracks")
  public Track addTrack(@PathVariable Long id, @RequestBody TrackIdentifierDto trackIdentifierDto) {
    Playlist playlist = playlistRepository.findById(id)
        .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "No playlist exists with id " + id));
    Track track = trackRepository.findById(trackIdentifierDto.getId())
        .orElseThrow(
            () -> new ResponseStatusException(NOT_FOUND, "No track exists with id " + trackIdentifierDto.getId()));
    try {
      playlist.getTracks().add(track);
      playlistRepository.save(playlist);
    } catch (Exception e) {
      throw new ResponseStatusException(
          HttpStatus.EXPECTATION_FAILED, "Track already in playlist", e);
    }
    return track;
  }

  @DeleteMapping("/api/playlists/{playlist_id}/tracks/{track_id}")
  public Track removeTrack(@PathVariable Long playlist_id, @PathVariable Long track_id) {
    Playlist playlist = playlistRepository.findById(playlist_id)
        .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "No playlist exists with id " + playlist_id));
    Track track = trackRepository.findById(track_id)
        .orElseThrow(
            () -> new ResponseStatusException(NOT_FOUND, "No track exists with id " + track_id));
    playlist.getTracks().remove(track);
    playlistRepository.save(playlist);
    return track;
  }

  @DeleteMapping("/api/playlists/{id}")
  public void delete(@PathVariable Long id) {
    Playlist playlist = playlistRepository
        .findById(id)
        .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "No playlist exists with id " + id));
    playlistRepository.delete(playlist);
  }
}