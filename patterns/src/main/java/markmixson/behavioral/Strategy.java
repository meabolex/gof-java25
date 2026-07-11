import java.util.ArrayList;
import java.util.List;

// ==========================================
// 1. Auxiliary Domain Model
// ==========================================
// Records are deeply immutable, perfect for modeling data.
record Item(String name, double price) {

}

// ==========================================
// 2. The Strategy Interface
// ==========================================
@FunctionalInterface
interface PricingStrategy {

  // The core algorithmic contract
  double calculateFinalTotal(List<Item> items);

  // Default Method 1: Shared Utility
  // Replaces the need for a "protected double getSubtotal()" in an abstract class
  default double calculateSubtotal(List<Item> items) {
    return items.stream().mapToDouble(Item::price).sum();
  }

  // Default Method 2: Shared Workflow
  // Replaces the need for an abstract base class handling standard output formatting
  default void printReceipt(List<Item> items) {
    double subtotal = calculateSubtotal(items);
    double finalTotal = calculateFinalTotal(items);

    IO.println("Items:");
    items.forEach(item -> IO.println(
        " - " + item.name() + ": $" + String.format("%.2f", item.price())));

    IO.println("Subtotal: $" + String.format("%.2f", subtotal));
    IO.println("Final Charged: $" + String.format("%.2f", finalTotal) + "\n");
  }
}

// ==========================================
// 3. Concrete Strategies
// ==========================================
// Records allow us to pass configuration (like discount percentages) 
// into the strategy without writing constructors or final fields.
record StandardPricing() implements PricingStrategy {

  @Override
  public double calculateFinalTotal(List<Item> items) {
    // No discount applied
    return calculateSubtotal(items);
  }
}

record PercentageDiscountPricing(double percentage) implements PricingStrategy {

  @Override
  public double calculateFinalTotal(List<Item> items) {
    double subtotal = calculateSubtotal(items);
    return subtotal * (1.0 - percentage);
  }
}

record FlatRateDiscountPricing(double discountAmount) implements PricingStrategy {

  @Override
  public double calculateFinalTotal(List<Item> items) {
    double subtotal = calculateSubtotal(items);
    return Math.max(0, subtotal - discountAmount); // Prevents negative totals
  }
}

// ==========================================
// 4. The Context
// ==========================================
static final class ShoppingCart {

  private final List<Item> items = new ArrayList<>();

  // The Context holds a reference to the interface, not a concrete implementation
  private PricingStrategy pricingStrategy;

  public ShoppingCart(PricingStrategy pricingStrategy) {
    this.pricingStrategy = pricingStrategy;
  }

  // Allows swapping the algorithm at runtime!
  public void setPricingStrategy(PricingStrategy pricingStrategy) {
    this.pricingStrategy = pricingStrategy;
  }

  public void addItem(Item item) {
    items.add(item);
  }

  public void checkout() {
    IO.println("=== Processing Checkout ===");
    // The context delegates the algorithmic work to the Strategy object.
    // It utilizes the default method on the interface to handle the heavy lifting.
    pricingStrategy.printReceipt(items);
  }
}

// ==========================================
// 5. Client Application
// ==========================================
// Using Java 22+ Implicitly Declared Class / Instance Main Method
void main() {
  // 1. Initialize the Context with a default Strategy
  ShoppingCart cart = new ShoppingCart(new StandardPricing());

  cart.addItem(new Item("Mechanical Keyboard", 120.00));
  cart.addItem(new Item("Wireless Mouse", 50.00));
  cart.addItem(new Item("Desk Mat", 30.00));

  // Checkout with Standard Pricing
  IO.println("Scenario: Standard Customer");
  cart.checkout();

  // 2. Change the Strategy dynamically at runtime
  IO.println("Scenario: VIP Customer gets 20% off");
  cart.setPricingStrategy(new PercentageDiscountPricing(0.20));
  cart.checkout();

  // 3. Change the Strategy again
  IO.println("Scenario: Applying a $15 coupon code");
  cart.setPricingStrategy(new FlatRateDiscountPricing(15.00));
  cart.checkout();

  // 4. Utilizing lambda expressions
  // Because PricingStrategy is a @FunctionalInterface, clients can pass 
  // custom, one-off inline strategies instantly without creating a new class!
  IO.println("Scenario: Flash Sale - All items are exactly $10");
  cart.setPricingStrategy(items -> items.size() * 10.00);
  cart.checkout();
}
