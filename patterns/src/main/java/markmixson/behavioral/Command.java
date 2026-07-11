import java.util.ArrayDeque;
import java.util.Deque;

// ==========================================
// 1. The Command Interface
// ==========================================
interface Command {

  void execute();

  void undo();

  // Default method to compose multiple commands (Macro Command).
  // It entirely replaces the need for a dedicated "MacroCommand" class.
  // We use a local record inside the method to return a completely immutable composite command!
  default Command andThen(Command next) {
    record CompositeCommand(Command first, Command second) implements Command {

      @Override
      public void execute() {
        first.execute();
        second.execute();
      }

      @Override
      public void undo() {
        // Undo must happen in reverse order!
        second.undo();
        first.undo();
      }
    }
    return new CompositeCommand(this, next);
  }
}

// ==========================================
// 2. The Receivers (The systems doing the actual work)
// ==========================================
static final class Light {

  private final String location;

  public Light(String location) {
    this.location = location;
  }

  public void turnOn() {
    IO.println("[" + location + "] Light turned ON.");
  }

  public void turnOff() {
    IO.println("[" + location + "] Light turned OFF.");
  }
}

static final class Thermostat {

  private int temperature = 70; // default state

  public int getTemperature() {
    return temperature;
  }

  public void setTemperature(int temperature) {
    this.temperature = temperature;
    IO.println("[Thermostat] Temperature set to " + temperature + "°F.");
  }
}

// ==========================================
// 3. Concrete Commands using Records
// ==========================================
// Records are perfect for commands because the parameters of a command 
// (the receiver and the state) should be immutable once the command is created.

record LightOnCommand(Light light) implements Command {

  @Override
  public void execute() {
    light.turnOn();
  }

  @Override
  public void undo() {
    light.turnOff();
  }
}

record LightOffCommand(Light light) implements Command {

  @Override
  public void execute() {
    light.turnOff();
  }

  @Override
  public void undo() {
    light.turnOn();
  }
}

// For stateful undos, the record simply captures the previous state at creation time
record SetTemperatureCommand(Thermostat thermostat, int previousTemp, int targetTemp) implements
    Command {

  @Override
  public void execute() {
    thermostat.setTemperature(targetTemp);
  }

  @Override
  public void undo() {
    thermostat.setTemperature(previousTemp);
  }
}

// ==========================================
// 4. The Invoker
// ==========================================
// The Invoker asks the command to carry out the request and tracks history for undoing.
static final class SmartHomeRemote {

  // A stack to keep track of our command history
  private final Deque<Command> history = new ArrayDeque<>();

  public void pressButton(Command command) {
    command.execute();
    history.push(command);
  }

  public void pressUndo() {
    if (!history.isEmpty()) {
      IO.println("--- Undoing Last Action ---");
      Command lastCommand = history.pop();
      lastCommand.undo();
    } else {
      IO.println("--- No actions to undo ---");
    }
  }
}

// ==========================================
// 5. Client Application
// ==========================================
// Using Java 22+ Implicitly Declared Class / Instance Main Method
void main() {
  // 1. Initialize Receivers
  Light livingRoomLight = new Light("Living Room");
  Thermostat homeThermostat = new Thermostat();

  // 2. Initialize Invoker
  SmartHomeRemote remote = new SmartHomeRemote();

  IO.println("=== Testing Basic Commands ===");
  // Creating and executing discrete commands
  Command lightsOn = new LightOnCommand(livingRoomLight);
  remote.pressButton(lightsOn);

  Command heatUp = new SetTemperatureCommand(homeThermostat, homeThermostat.getTemperature(), 75);
  remote.pressButton(heatUp);

  // 3. Testing Undo
  remote.pressUndo(); // Undoes the thermostat change
  remote.pressUndo(); // Undoes the light change

  IO.println("\n=== Testing Composite (Macro) Commands ===");
  // Using our interface's default method to chain commands together seamlessly
  Command movieMode = new LightOffCommand(livingRoomLight).andThen(
      new SetTemperatureCommand(homeThermostat, homeThermostat.getTemperature(), 68));

  remote.pressButton(movieMode);

  // Undoing a composite command safely undoes both actions in reverse order!
  remote.pressUndo();
}
