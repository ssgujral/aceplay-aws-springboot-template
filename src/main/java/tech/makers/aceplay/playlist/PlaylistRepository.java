package tech.makers.aceplay.playlist;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

// https://www.youtube.com/watch?v=vreyOZxdb5Y&t=343s
public interface PlaylistRepository extends CrudRepository<Playlist, Long> {
  Playlist findFirstByOrderByIdAsc();

  List<Playlist> findByCoolEquals(Boolean cool);

  List<Playlist> findByUserId(Long userId);
}