import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

// ==========================================
// 1. The Event Payload
// ==========================================
record StockEvent(String ticker, double oldPrice, double newPrice) {

}

// ==========================================
// 2. The Observer Interface
// ==========================================
@FunctionalInterface
interface StockObserver {

  void onPriceChanged(StockEvent event);
}

// ==========================================
// 3. The Subject Interface (Observable)
// ==========================================
interface StockPublisher {

  // Contract method: Implementers must provide a Set to store observers.
  Set<StockObserver> observers();

  // Default methods handle the boilerplate.
  default void subscribe(StockObserver observer) {
    // Because we are using a Set, we no longer need to manually check 
    // if the observer is already present. The Set handles uniqueness natively.
    observers().add(observer);
  }

  default void unsubscribe(StockObserver observer) {
    observers().remove(observer);
  }

  default void notifyObservers(StockEvent event) {
    for (StockObserver observer : observers()) {
      observer.onPriceChanged(event);
    }
  }
}

// ==========================================
// 4. Concrete Subject
// ==========================================
static final class TechStock implements StockPublisher {

  private final String ticker;
  private double currentPrice;

  // CopyOnWriteArraySet provides Set semantics (no duplicates) while remaining 
  // perfectly thread-safe for iteration during broadcasts.
  private final Set<StockObserver> observers = new CopyOnWriteArraySet<>();

  public TechStock(String ticker, double initialPrice) {
    this.ticker = ticker;
    this.currentPrice = initialPrice;
  }

  @Override
  public Set<StockObserver> observers() {
    return this.observers;
  }

  public void updatePrice(double newPrice) {
    IO.println(
        "\n[MARKET UPDATE] " + ticker + " changed from $" + currentPrice + " to $" + newPrice);

    StockEvent event = new StockEvent(ticker, currentPrice, newPrice);
    this.currentPrice = newPrice;

    notifyObservers(event);
  }
}

// ==========================================
// 5. Concrete Observers
// ==========================================
record MobileAppUser(String userName) implements StockObserver {

  @Override
  public void onPriceChanged(StockEvent event) {
    IO.println(
        "   📱 [Mobile Push] " + userName + ": " + event.ticker() + " is now $" + event.newPrice());
  }
}

record TradingBot(String botId, double buyThreshold) implements StockObserver {

  @Override
  public void onPriceChanged(StockEvent event) {
    if (event.newPrice() < buyThreshold) {
      IO.println(
          "   🤖 [Bot-" + botId + "] Executing BUY order for " + event.ticker() + " at $"
              + event.newPrice());
    } else {
      IO.println("   🤖 [Bot-" + botId + "] Price too high. Holding cash.");
    }
  }
}

// ==========================================
// 6. Client Application
// ==========================================
// Using Java 22+ Implicitly Declared Class / Instance Main Method
void main() {
  IO.println("=== Starting Stock Market Ticker ===");

  TechStock cyberDyneStock = new TechStock("CYBR", 150.00);

  StockObserver aliceApp = new MobileAppUser("Alice");
  StockObserver autoTrader = new TradingBot("Algo-X", 145.00);

  // Subscribing Observers
  cyberDyneStock.subscribe(aliceApp);
  cyberDyneStock.subscribe(autoTrader);

  // Testing the Set uniqueness: Subscribing Alice a second time
  IO.println("Attempting to subscribe Alice a second time...");
  cyberDyneStock.subscribe(aliceApp);

  // Triggering State Changes
  // Alice will only receive this notification ONCE because the Set prevented the duplicate.
  cyberDyneStock.updatePrice(144.00);

  cyberDyneStock.unsubscribe(aliceApp);

  cyberDyneStock.updatePrice(140.00);
}
