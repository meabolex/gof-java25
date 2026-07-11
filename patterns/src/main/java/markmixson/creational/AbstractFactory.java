// 1. Abstract Products using Sealed Interfaces and Default Methods
sealed interface Button permits DarkButton, LightButton {
  void paint();

  // Default method to share logic across all button types
  default void click() {
    IO.println("Button clicked! Triggering action...");
  }
}

sealed interface Checkbox permits DarkCheckbox, LightCheckbox {
  void paint();

  // Default method to share logic across all checkbox types
  default void toggle() {
    IO.println("Checkbox state toggled.");
  }
}

// 2. Concrete Products using Records
record DarkButton(String label) implements Button {
  @Override
  public void paint() {
    IO.println("Rendering [DARK] button: " + label);
  }
}

record LightButton(String label) implements Button {
  @Override
  public void paint() {
    IO.println("Rendering [LIGHT] button: " + label);
  }
}

record DarkCheckbox(String label) implements Checkbox {
  @Override
  public void paint() {
    IO.println("Rendering [DARK] checkbox: " + label);
  }
}

record LightCheckbox(String label) implements Checkbox {
  @Override
  public void paint() {
    IO.println("Rendering [LIGHT] checkbox: " + label);
  }
}

// 3. Abstract Factory Interface
interface GUIFactory {
  Button createButton(final String label);

  Checkbox createCheckbox(final String label);

  // Default method containing orchestration logic, eliminating the need
  // for an abstract base class for factories.
  default void createAndRenderTheme(final String buttonLabel, final String checkboxLabel) {
    Button btn = createButton(buttonLabel);
    Checkbox cb = createCheckbox(checkboxLabel);

    btn.paint();
    cb.paint();

    // Demonstrating the default methods from the product interfaces
    btn.click();
    cb.toggle();
  }
}

// 4. Concrete Factories
static final class DarkThemeFactory implements GUIFactory {
  @Override
  public Button createButton(final String label) {
    return new DarkButton(label);
  }

  @Override
  public Checkbox createCheckbox(final String label) {
    return new DarkCheckbox(label);
  }
}

static final class LightThemeFactory implements GUIFactory {
  @Override
  public Button createButton(final String label) {
    return new LightButton(label);
  }

  @Override
  public Checkbox createCheckbox(final String label) {
    return new LightCheckbox(label);
  }
}

// 5. Client Application
// Note: Using Java 22+ Implicitly Declared Class / Instance Main Method
void main(final String[] args) {
  final var osThemePreference = args.length == 0 ? "DARK" : args[0];

  // Using a modern switch expression to yield the correct factory
  final var factory =
      switch (osThemePreference) {
        case "DARK" -> new DarkThemeFactory();
        case "LIGHT" -> new LightThemeFactory();
        default ->
            throw new IllegalArgumentException("Unknown theme preference: " + osThemePreference);
      };

  System.out.println("--- Building UI ---");

  // Call the default method on the interface, which utilizes the factory's concrete instantiations
  factory.createAndRenderTheme("Submit", "Remember Me");
}
