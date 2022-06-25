package tech.makers.aceplay.track;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.NOT_FOUND;

import tech.makers.aceplay.user.User;
import tech.makers.aceplay.user.UserRepository;

import java.security.Principal;

// https://www.youtube.com/watch?v=5r3QU09v7ig&t=2410s
@RestController
public class TracksController {
  @Autowired
  private TrackRepository trackRepository;

  @Autowired
  private UserRepository userRepository;

  @GetMapping("/api/tracks")
  public Iterable<Track> index(Principal principal) {
    User user = userRepository.findByUsername(principal.getName());
    Long userId = user.getId();
    return trackRepository.findAllByUserId(userId);
  }

  @PostMapping("/api/tracks")
  public Track create(Principal principal, @RequestBody TrackDto trackDTO) {

    Track track = new Track();
    BeanUtils.copyProperties(trackDTO, track);

    User user = userRepository.findByUsername(principal.getName());
    track.setUser(user);
    return trackRepository.save(track);
  }

  @PatchMapping("/api/tracks/{id}")
  public Track update(@PathVariable Long id, @RequestBody TrackDto newTrackDTO) {
    Track newTrack = new Track();
    BeanUtils.copyProperties(newTrackDTO, newTrack);
    Track track = trackRepository
        .findById(id)
        .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "No track exists with id " + id));
    track.setTitle(newTrack.getTitle());
    track.setArtist(newTrack.getArtist());
    trackRepository.save(track);
    return track;
  }

  @DeleteMapping("/api/tracks/{id}")
  public void delete(@PathVariable Long id) {
    Track track = trackRepository
        .findById(id)
        .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "No track exists with id " + id));
    trackRepository.delete(track);
  }
}