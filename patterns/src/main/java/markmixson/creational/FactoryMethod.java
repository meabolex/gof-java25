// 1. The Product interface using sealed types
sealed interface Notification permits EmailNotification, PushNotification {
  void send(String message);
}

// 2. Concrete Products using Records
record EmailNotification(String recipientAddress) implements Notification {
  @Override
  public void send(String message) {
    IO.println("Sending EMAIL to " + recipientAddress + " | Payload: " + message);
  }
}

record PushNotification(String deviceToken) implements Notification {
  @Override
  public void send(String message) {
    IO.println("Sending PUSH to device [" + deviceToken + "] | Payload: " + message);
  }
}

// 3. The Creator Interface
interface NotificationFactory {

  // The Factory Method itself: subclasses must implement this to return a specific product
  Notification createNotification(String target);

  // Default method replacing the need for an abstract base class.
  // It contains the core workflow that relies on the factory method.
  default void dispatchAlert(String target, String alertMessage) {
    IO.println("Starting dispatch sequence...");

    // Use the factory method to instantiate the product
    Notification notification = createNotification(target);

    // Execute business logic on the created product
    notification.send(alertMessage);
    IO.println("Dispatch sequence complete.\n");
  }
}

// 4. Concrete Creators
static final class EmailFactory implements NotificationFactory {
  @Override
  public Notification createNotification(String target) {
    // Creates the specific Email product
    return new EmailNotification(target);
  }
}

static final class PushFactory implements NotificationFactory {
  @Override
  public Notification createNotification(String target) {
    // Creates the specific Push product
    return new PushNotification(target);
  }
}

// 5. Client Application
// Using Java 22+ Implicitly Declared Class / Instance Main Method
void main() {
  // The client code relies purely on the interfaces
  NotificationFactory emailService = new EmailFactory();
  NotificationFactory pushService = new PushFactory();

  // The dispatch logic is identical, but the underlying product creation defers to the factories
  emailService.dispatchAlert("admin@company.com", "Server CPU usage exceeded 90%");

  pushService.dispatchAlert("device_token_xyz123", "You have a new message from Sarah");
}
