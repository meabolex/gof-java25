// 1. The Product: Deeply immutable using a Record
record Computer(String cpu, int ramGb, int storageGb, boolean hasGpu, String os) {

  // Entry point for the builder
  public static CpuStep builder() {
    return new ComputerBuilderImpl();
  }
}

// 2. Sealed Interfaces representing construction stages.
// By limiting permissions to a single implementation, we secure the builder lifecycle.
sealed interface CpuStep permits ComputerBuilderImpl {

  RamStep cpu(String cpu);

  // Default method providing a common preset, bypassing the need for an abstract builder
  default RamStep standardIntelCpu() {
    return cpu("Intel Core i5");
  }
}

sealed interface RamStep permits ComputerBuilderImpl {

  StorageStep ramGb(int gb);

  default StorageStep standardRam() {
    return ramGb(16);
  }
}

sealed interface StorageStep permits ComputerBuilderImpl {

  OptionalStep storageGb(int gb);
}

// 3. The final stage allows optional configurations and building
sealed interface OptionalStep permits ComputerBuilderImpl {

  OptionalStep hasGpu(boolean hasGpu);

  OptionalStep os(String os);

  Computer build();

  // Default method composing multiple optional steps into a single preset
  default OptionalStep makeGamingReady() {
    return hasGpu(true).os("Windows 11 Pro");
  }
}

// 4. The single concrete Builder implementation
// It remains package-private or private; the client only interacts with the interfaces.
static final class ComputerBuilderImpl implements CpuStep, RamStep, StorageStep, OptionalStep {

  private String cpu;
  private int ramGb;
  private int storageGb;
  private boolean hasGpu = false; // Default value
  private String os = "Ubuntu Linux"; // Default value

  @Override
  public RamStep cpu(String cpu) {
    this.cpu = cpu;
    return this;
  }

  @Override
  public StorageStep ramGb(int gb) {
    this.ramGb = gb;
    return this;
  }

  @Override
  public OptionalStep storageGb(int gb) {
    this.storageGb = gb;
    return this;
  }

  @Override
  public OptionalStep hasGpu(boolean hasGpu) {
    this.hasGpu = hasGpu;
    return this;
  }

  @Override
  public OptionalStep os(String os) {
    this.os = os;
    return this;
  }

  @Override
  public Computer build() {
    // Validation could also occur here before object creation
    return new Computer(cpu, ramGb, storageGb, hasGpu, os);
  }
}

// 5. Client Application
// Using Java 22+ Implicitly Declared Class / Instance Main Method
void main() {
  IO.println("--- Custom Build ---");
  // The compiler forces you to provide CPU -> RAM -> Storage in that exact order.
  Computer customRig = Computer.builder()
      .cpu("AMD Ryzen 9")
      .ramGb(64)
      .storageGb(2000)
      .hasGpu(true)
      .os("Arch Linux")
      .build();

  IO.println(customRig);

  IO.println("\n--- Preset Build utilizing Default Methods ---");
  // Leveraging the default methods defined in the interfaces for a quicker configuration
  final var budgetGamingRig = Computer.builder()
      .standardIntelCpu()
      .standardRam()
      .storageGb(1000)
      .makeGamingReady()
      .build();

  IO.println(budgetGamingRig);
}
