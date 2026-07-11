// ==========================================
// 1. The Implementor Hierarchy
// ==========================================

// Sealed interface restricts which devices exist in our domain.
sealed interface Device permits Tv, Radio {

  boolean isEnabled();

  void enable();

  void disable();

  int getVolume();

  void setVolume(int percent);
}

// Concrete Implementor A
static final class Tv implements Device {

  private boolean on = false;
  private int volume = 30;

  @Override
  public boolean isEnabled() {
    return on;
  }

  @Override
  public void enable() {
    on = true;
    IO.println("TV is turned ON.");
  }

  @Override
  public void disable() {
    on = false;
    IO.println("TV is turned OFF.");
  }

  @Override
  public int getVolume() {
    return volume;
  }

  @Override
  public void setVolume(int percent) {
    if (percent > 100) {
      percent = 100;
    }
    if (percent < 0) {
      percent = 0;
    }
    this.volume = percent;
    IO.println("TV volume set to " + this.volume + "%");
  }
}

// Concrete Implementor B
static final class Radio implements Device {

  private boolean on = false;
  private int volume = 10;

  @Override
  public boolean isEnabled() {
    return on;
  }

  @Override
  public void enable() {
    on = true;
    IO.println("Radio is turned ON.");
  }

  @Override
  public void disable() {
    on = false;
    IO.println("Radio is turned OFF.");
  }

  @Override
  public int getVolume() {
    return volume;
  }

  @Override
  public void setVolume(int percent) {
    if (percent > 100) {
      percent = 100;
    }
    if (percent < 0) {
      percent = 0;
    }
    this.volume = percent;
    IO.println("Radio volume set to " + this.volume + "%");
  }
}

// ==========================================
// 2. The Abstraction Hierarchy
// ==========================================

interface RemoteControl {

  // The "Bridge" contract: Concrete classes must provide the implementor.
  Device device();

  // Default methods replace the need for an abstract base class.
  // They orchestrate the Device behavior without knowing the specific device type.
  default void togglePower() {
    System.out.print("Remote: Toggling power -> ");
    if (device().isEnabled()) {
      device().disable();
    } else {
      device().enable();
    }
  }

  default void volumeUp() {
    System.out.print("Remote: Volume Up -> ");
    device().setVolume(device().getVolume() + 10);
  }

  default void volumeDown() {
    System.out.print("Remote: Volume Down -> ");
    device().setVolume(device().getVolume() - 10);
  }
}

// ==========================================
// 3. Refined Abstractions
// ==========================================

// A standard remote. 
// MAGIC TRICK: By naming the record component "device", Java automatically generates 
// the `public Device device()` method, which perfectly satisfies the RemoteControl interface!
record BasicRemote(Device device) implements RemoteControl {

}

// An advanced remote that adds its own specific behavior on top of the base abstraction.
record AdvancedRemote(Device device) implements RemoteControl {

  public void mute() {
    System.out.print("Advanced Remote: Mute pressed -> ");
    device().setVolume(0);
  }
}

// ==========================================
// 4. Client Application
// ==========================================
// Using Java 22+ Implicitly Declared Class / Instance Main Method
void main() {
  IO.println("--- Testing Basic Remote with TV ---");
  Device tv = new Tv();
  RemoteControl basicRemote = new BasicRemote(tv);

  basicRemote.togglePower();
  basicRemote.volumeUp();

  IO.println("\n--- Testing Advanced Remote with Radio ---");
  Device radio = new Radio();
  // We type this as AdvancedRemote so we can access the specialized mute() method
  AdvancedRemote advancedRemote = new AdvancedRemote(radio);

  advancedRemote.togglePower();
  advancedRemote.volumeUp();
  advancedRemote.mute();
}
