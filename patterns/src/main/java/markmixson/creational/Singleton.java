import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

// 1. The Singleton Contract defined as an interface
interface ConfigurationStrategy {

  void setConfig(String key, String value);

  String getConfig(String key);

  // Default method to provide shared logic without using an abstract base class.
  // It relies on the abstract methods that the Singleton will implement.
  default String getConfigOrDefault(String key, String defaultValue) {
    String value = getConfig(key);
    return (value != null && !value.isBlank()) ? value : defaultValue;
  }

  default void printDiagnostics() {
    IO.println("System Configuration Diagnostic Check Run.");
  }
}

// 2. The Singleton Implementation using an Enum
// The JVM guarantees that enum values are instantiated exactly once.
enum AppConfigManager implements ConfigurationStrategy {

  // The single, globally accessible instance
  INSTANCE;

  // Internal state of the singleton.
  // We use ConcurrentHashMap to ensure thread-safe access to the state.
  private final Map<String, String> settings = new ConcurrentHashMap<>();

  // Enum constructors are implicitly private.
  // You can initialize default singleton state here.
  AppConfigManager() {
    settings.put("theme", "DARK");
    settings.put("version", "2.1.0");
  }

  @Override
  public void setConfig(String key, String value) {
    settings.put(key, value);
  }

  @Override
  public String getConfig(String key) {
    return settings.get(key);
  }
}

public static final String DATABASE_URL = "database_url";

// 3. Client Application
// Using Java 22+ Implicitly Declared Class / Instance Main Method
@SuppressWarnings("ConstantConditions")
void main() {
  IO.println("--- Singleton Access ---");

  // In a real application, you might inject this interface, but for
  // global access, you call the enum instance directly.
  ConfigurationStrategy config = AppConfigManager.INSTANCE;

  // Utilizing the default method from the interface
  String theme = config.getConfigOrDefault("theme", "LIGHT");
  String dbUrl = config.getConfigOrDefault(DATABASE_URL, "localhost:5432");

  IO.println("Current Theme: " + theme);
  IO.println("Database URL: " + dbUrl);

  // Modifying the singleton state
  config.setConfig(DATABASE_URL, "prod-db.internal:5432");

  IO.println("\n--- Proving it's a Singleton ---");

  // Accessing the singleton from a "different" part of the app
  ConfigurationStrategy anotherReference = AppConfigManager.INSTANCE;

  IO.println("Are references identical? " + (config == anotherReference));
  IO.println("Updated Database URL: " + anotherReference.getConfig(DATABASE_URL));

  // Calling a default method
  anotherReference.printDiagnostics();
}
