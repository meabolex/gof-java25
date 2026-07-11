import java.util.ArrayList;
import java.util.List;

public static final String COPYING_DIRECTIVE = "] copying directive: ";

// ==========================================
// 1. The Command "Enum" (Algebraic Data Type)
// ==========================================
// A sealed interface strictly controls what commands exist.
sealed interface TowerCommand permits RequestLanding, Landed, Mayday {

}

// Records act as the "enum values". They can be empty...
record RequestLanding() implements TowerCommand {

}

record Landed() implements TowerCommand {

}

// ...or they can hold dynamic, immutable state!
record Mayday(String reason) implements TowerCommand {

}


// ==========================================
// 2. The Mediator Interface
// ==========================================
interface ControlTower {

  void register(Aircraft aircraft);

  // The signature now takes our strict ADT instead of a loose String
  void notify(Aircraft sender, TowerCommand command);

  List<Aircraft> registeredAircraft();

  default void broadcast(Aircraft sender, String message) {
    for (Aircraft aircraft : registeredAircraft()) {
      if (aircraft != sender) {
        aircraft.receiveDirective(message);
      }
    }
  }
}

// ==========================================
// 3. The Colleague Interface
// ==========================================
sealed interface Aircraft permits PassengerPlane, CargoPlane, Helicopter {

  String callsign();

  ControlTower tower();

  default void requestLanding() {
    IO.println("✈️ [" + callsign() + "] Requesting landing clearance...");
    tower().notify(this, new RequestLanding());
  }

  default void notifyLanded() {
    IO.println("🛬 [" + callsign() + "] Touchdown confirmed. Vacating runway.");
    tower().notify(this, new Landed());
  }

  default void sendMayday(String reason) {
    IO.println("🚨 [" + callsign() + "] MAYDAY MAYDAY: " + reason);
    // Passing the dynamic state directly into the command record
    tower().notify(this, new Mayday(reason));
  }

  void receiveDirective(String message);
}

// ==========================================
// 4. Concrete Colleagues
// ==========================================
record PassengerPlane(String callsign, ControlTower tower) implements Aircraft {

  public PassengerPlane {
    tower.register(this);
  }

  @Override
  public void receiveDirective(String message) {
    IO.println("   -> Passenger Jet [" + callsign + COPYING_DIRECTIVE + message);
  }
}

record CargoPlane(String callsign, ControlTower tower) implements Aircraft {

  public CargoPlane {
    tower.register(this);
  }

  @Override
  public void receiveDirective(String message) {
    IO.println("   -> Cargo Plane [" + callsign + COPYING_DIRECTIVE + message);
  }
}

record Helicopter(String callsign, ControlTower tower) implements Aircraft {

  public Helicopter {
    tower.register(this);
  }

  @Override
  public void receiveDirective(String message) {
    IO.println("   -> Helicopter [" + callsign + COPYING_DIRECTIVE + message);
  }
}

// ==========================================
// 5. Concrete Mediator
// ==========================================
static final class JFKControlTower implements ControlTower {

  private final List<Aircraft> fleet = new ArrayList<>();
  private Aircraft aircraftOnRunway = null;

  @Override
  public void register(Aircraft aircraft) {
    if (!fleet.contains(aircraft)) {
      fleet.add(aircraft);
    }
  }

  @Override
  public List<Aircraft> registeredAircraft() {
    return fleet;
  }

  @Override
  public void notify(Aircraft sender, TowerCommand command) {

    // Modern Switch Pattern Matching ensures we handle every command type safely.
    switch (command) {

      // Using Java 22+ Unnamed Variable (_) because we don't need to extract state
      case RequestLanding _ -> {
        if (aircraftOnRunway == null) {
          aircraftOnRunway = sender;
          sender.receiveDirective("Runway clear. Cleared to land.");
        } else {
          sender.receiveDirective(
              "Runway occupied by " + aircraftOnRunway.callsign() + ". Enter holding pattern.");
        }
      }

      case Landed _ -> {
        aircraftOnRunway = null;
        sender.receiveDirective("Taxi to gate.");
      }

      // Java 21+ Record Pattern:
      // Automatically extracts the 'reason' state directly into a variable!
      case Mayday(String reason) -> {
        IO.println(
            "\n[TOWER] EMERGENCY DECLARED BY " + sender.callsign().toUpperCase() + " - "
                + reason);
        broadcast(sender,
            "EMERGENCY IN PROGRESS. Divert immediately to alternate airspace.");
      }
    }
  }
}

// ==========================================
// 6. Client Application
// ==========================================
void main() {
  IO.println("=== Air Traffic Control System Initiated ===\n");

  ControlTower jfkTower = new JFKControlTower();

  Aircraft delta123 = new PassengerPlane("Delta-123", jfkTower);
  Aircraft fedex99 = new CargoPlane("FedEx-99", jfkTower);
  Aircraft newsChopper = new Helicopter("NewsChopper-1", jfkTower);

  delta123.requestLanding();
  IO.println();

  fedex99.requestLanding();
  IO.println();

  delta123.notifyLanded();
  IO.println();

  // The dynamic state ("Tail rotor failure!") is captured in the Mayday record
  newsChopper.sendMayday("Tail rotor failure!");
}

