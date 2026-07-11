// ==========================================
// 1. The Complex Subsystems
// ==========================================
// These are various classes with their own specific, detailed APIs.
// A client would normally have to coordinate all of these manually.
static final class Lighting {

  public void dim(int level) {
    IO.println("Lighting: Dimming lights to " + level + "%.");
  }

  public void on() {
    IO.println("Lighting: Turning lights fully ON.");
  }
}

static final class Projector {

  public void on() {
    IO.println("Projector: Powered ON.");
  }

  public void off() {
    IO.println("Projector: Powered OFF.");
  }

  public void setWideScreenMode() {
    IO.println("Projector: Aspect ratio set to Widescreen (16:9).");
  }
}

static final class SoundSystem {

  public void on() {
    IO.println("SoundSystem: Powered ON.");
  }

  public void off() {
    IO.println("SoundSystem: Powered OFF.");
  }

  public void setVolume(int level) {
    IO.println("SoundSystem: Volume set to " + level + ".");
  }

  public void enableSurroundSound() {
    IO.println("SoundSystem: Surround Sound enabled.");
  }
}

static final class StreamingService {

  public void play(String movieTitle) {
    IO.println("StreamingService: Buffering and playing '" + movieTitle + "'.");
  }

  public void stop() {
    IO.println("StreamingService: Playback stopped.");
  }
}

// ==========================================
// 2. The Facade Interface
// ==========================================
// This interface defines the simplified operations for the client.

interface HomeTheaterFacade {

  // Contract methods defining the necessary subsystems
  Lighting lighting();

  Projector projector();

  SoundSystem soundSystem();

  StreamingService streamingService();

  // The core Facade logic lives in default methods.
  // This orchestrates the complex subsystems without using an abstract base class.
  default void watchMovie(String movieTitle) {
    IO.println("\n--- Get ready to watch a movie ---");
    lighting().dim(10);

    projector().on();
    projector().setWideScreenMode();

    soundSystem().on();
    soundSystem().setVolume(45);
    soundSystem().enableSurroundSound();

    streamingService().play(movieTitle);
    IO.println("--- Movie started! Enjoy! ---\n");
  }

  default void endMovie() {
    IO.println("\n--- Shutting down home theater ---");
    streamingService().stop();
    projector().off();
    soundSystem().off();
    lighting().on();
    IO.println("--- Home theater is OFF ---\n");
  }
}

// ==========================================
// 3. The Concrete Facade using a Record
// ==========================================
// By mapping the Record components exactly to the interface contract methods,
// Java automatically generates the methods required by the interface.
// Zero boilerplate is needed!

record SmartHomeTheater(
    Lighting lighting,
    Projector projector,
    SoundSystem soundSystem,
    StreamingService streamingService
) implements HomeTheaterFacade {

}

// ==========================================
// 4. Client Application
// ==========================================
// Using Java 22+ Implicitly Declared Class / Instance Main Method

void main() {
  // 1. Initialize the complex subsystems (often done by Dependency Injection in real apps)
  Lighting roomLights = new Lighting();
  Projector epsonProjector = new Projector();
  SoundSystem sonosSystem = new SoundSystem();
  StreamingService netflixService = new StreamingService();

  // 2. Wrap them in the Facade
  HomeTheaterFacade myHomeTheater = new SmartHomeTheater(
      roomLights,
      epsonProjector,
      sonosSystem,
      netflixService
  );

  // 3. The Client uses the simplified interface
  // Instead of calling 7 different methods on 4 different objects, 
  // the client makes a single, expressive call.
  myHomeTheater.watchMovie("Dune: Part Two");

  // ... later ...

  myHomeTheater.endMovie();
}
