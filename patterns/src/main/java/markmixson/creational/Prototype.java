import java.util.List;

// 1. The Prototype Interface using Sealed Types
sealed interface ServerProfile permits WebProfile, DatabaseProfile {

  // The core Prototype contract
  ServerProfile copy();

  // A "wither" method to allow modification of the cloned immutable object
  ServerProfile withServerName(String newName);

  // Default method replacing an abstract base class.
  // It orchestrates the standard Prototype workflow: Clone -> Modify -> Return
  default ServerProfile cloneAndProvision(String newServerName) {
    IO.println("Provisioning new server [" + newServerName + "] from prototype...");

    // Step 1: Clone the expensive or complex prototype
    ServerProfile clone = this.copy();

    // Step 2: Apply specific modifications to the clone
    return clone.withServerName(newServerName);
  }
}

// 2. Concrete Prototypes using Records
record WebProfile(String serverName, String os, int memoryGb, List<String> openPorts) implements
    ServerProfile {

  @Override
  public WebProfile copy() {
    // For records, an exact copy passes the existing fields to the constructor.
    // We use List.copyOf() to enforce deep immutability on the collection,
    // preventing the clone from sharing mutable state with the prototype.
    return new WebProfile(this.serverName, this.os, this.memoryGb, List.copyOf(this.openPorts));
  }

  @Override
  public WebProfile withServerName(String newName) {
    // Returns a new instance with the modified field
    return new WebProfile(newName, this.os, this.memoryGb, this.openPorts);
  }
}

record DatabaseProfile(String serverName, String engine, int storageTb) implements
    ServerProfile {

  @Override
  public DatabaseProfile copy() {
    return new DatabaseProfile(this.serverName, this.engine, this.storageTb);
  }

  @Override
  public DatabaseProfile withServerName(String newName) {
    return new DatabaseProfile(newName, this.engine, this.storageTb);
  }
}

// 3. Client Application
// Using Java 22+ Implicitly Declared Class / Instance Main Method
void main() {
  // Imagine these prototypes are highly complex or expensive to construct
  // (e.g., loaded from a remote configuration database).
  ServerProfile webPrototype = new WebProfile(
      "web-base-template",
      "Ubuntu 24.04",
      16,
      List.of("80", "443", "8080")
  );

  ServerProfile dbPrototype = new DatabaseProfile(
      "db-base-template",
      "PostgreSQL 16",
      100
  );

  IO.println("--- Using Prototypes to stamp out new instances ---");

  // We use the interface's default method to clone and mutate in one step.
  // No need to rebuild the complex configurations from scratch.
  ServerProfile webNode1 = webPrototype.cloneAndProvision("web-node-us-east-1");
  ServerProfile webNode2 = webPrototype.cloneAndProvision("web-node-us-east-2");

  ServerProfile dbNode1 = dbPrototype.cloneAndProvision("db-main-cluster");

  IO.println("\n--- Results ---");
  IO.println(webNode1);
  IO.println(webNode2);
  IO.println(dbNode1);

  IO.println("\n--- Integrity Check ---");
  // Verify they are distinct objects and not just memory references to the prototype
  IO.println(
      "Is webNode1 the exact same object as prototype? " + (webNode1 == webPrototype));
}
