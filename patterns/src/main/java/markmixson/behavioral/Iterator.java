import java.util.NoSuchElementException;
import java.util.function.Consumer;

// ==========================================

// 1. The Domain Model

// ==========================================

// Records are perfect for immutable items within our collection.

record Song(String title, String artist, String genre) {

}

// ==========================================
// 2. The Iterator Interface
// ==========================================
interface SongIterator {

  // Core contract methods for traversal
  boolean hasNext();

  Song next();

  // Default method replacing the need for an abstract base class.
  // Any concrete iterator instantly gains this functionality.
  default void forEachRemaining(Consumer<Song> action) {
    while (hasNext()) {
      action.accept(next());
    }
  }
}

// ==========================================
// 3. The Aggregate Interface
// ==========================================
interface Playlist {

  // The Factory Method that creates the iterator
  SongIterator iterator();

  // Default method utilizing the iterator to provide rich behavior
  // to all concrete playlists without exposing their internal structure.
  default void playAll() {
    IO.println("Starting playlist playback...");
    iterator().forEachRemaining(
        song -> IO.println(" ▶ Playing: " + song.title() + " - " + song.artist()));
    IO.println("Playback finished.\n");
  }

  // Advanced Default Method: Dynamically creates a filtered iterator!
  // This allows the interface itself to act as a decorator, saving us
  // from writing a separate "FilteredPlaylist" class.
  default SongIterator iteratorByGenre(String targetGenre) {
    SongIterator baseIterator = this.iterator();

    // Using a Local Class inside the method to encapsulate the filtering state
    class GenreIterator implements SongIterator {

      private Song nextMatch = null;

      @Override
      public boolean hasNext() {
        if (nextMatch != null) {
          return true;
        }

        // Fast-forward until we find the next song matching the genre
        while (baseIterator.hasNext()) {
          Song candidate = baseIterator.next();
          if (candidate.genre().equalsIgnoreCase(targetGenre)) {
            nextMatch = candidate;
            return true;
          }
        }
        return false;
      }

      @Override
      public Song next() {
        if (!hasNext()) {
          throw new NoSuchElementException("No more songs in genre: " + targetGenre);
        }
        Song songToReturn = nextMatch;
        nextMatch = null; // Clear the match so hasNext() fetches the next one
        return songToReturn;
      }
    }
    return new GenreIterator();
  }
}

// ==========================================
// 4. Concrete Aggregate
// ==========================================
// A custom collection wrapping a primitive array.
static final class Mixtape implements Playlist {


  private final String name;
  private final Song[] tracks;
  private int trackCount = 0;

  public String getName() {
    return name;
  }

  public Mixtape(String name, int capacity) {
    this.name = name;
    this.tracks = new Song[capacity];
  }

  public void addTrack(Song song) {
    if (trackCount < tracks.length) {
      tracks[trackCount++] = song;
    } else {
      IO.println("Mixtape is full!");
    }
  }

  @Override
  public SongIterator iterator() {
    // We return an anonymous inner class as our Concrete Iterator.
    // It has intimate knowledge of the Mixtape's internal 'tracks' array,
    // but completely hides this detail from the client code.
    return new SongIterator() {
      private int currentIndex = 0;

      @Override
      public boolean hasNext() {
        return currentIndex < trackCount;
      }

      @Override
      public Song next() {
        if (!hasNext()) {
          throw new NoSuchElementException();
        }
        return tracks[currentIndex++];
      }
    };
  }
}

public static final String COUNTRY = "Country";

// ==========================================
// 5. Client Application
// ==========================================
// Using Java 22+ Implicitly Declared Class / Instance Main Method
void main() {
  Mixtape roadTripMix = new Mixtape("Summer Road Trip", 10);
  roadTripMix.addTrack(new Song("Hotel California", "Eagles", "Rock"));
  roadTripMix.addTrack(new Song("Take Me Home, Country Roads", "John Denver", COUNTRY));
  roadTripMix.addTrack(new Song("Bohemian Rhapsody", "Queen", "Rock"));
  roadTripMix.addTrack(new Song("Life is a Highway", "Tom Cochrane", "Rock"));
  roadTripMix.addTrack(new Song("Jolene", "Dolly Parton", COUNTRY));

  IO.println("=== Full Playback of %s via Interface Default Method ===".formatted(
      roadTripMix.getName()));
  // The client doesn't know Mixtape uses an array under the hood.
  roadTripMix.playAll();

  IO.println("=== Filtered Playback (Rock) via Filtered Iterator ===");
  // Leveraging the default method on the interface to traverse a specific subset
  SongIterator rockIterator = roadTripMix.iteratorByGenre("Rock");
  while (rockIterator.hasNext()) {
    Song song = rockIterator.next();
    IO.println(" 🎸 " + song.title() + " by " + song.artist());
  }

  IO.println("\n=== Filtered Playback (Country) using forEachRemaining ===");
  // Combining our custom filtered iterator with the lambda-friendly traversal method
  SongIterator countryIterator = roadTripMix.iteratorByGenre(COUNTRY);
  countryIterator.forEachRemaining(
      song -> IO.println(" 🤠 " + song.title() + " by " + song.artist()));
}


