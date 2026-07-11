// 1. The Component Interface using Sealed Types
sealed interface Coffee permits BasicEspresso, CoffeeDecorator {

  String getIngredients();

  double getPrice();

  // Default method shared by all components and decorators.
  default void printReceipt() {
    IO.println(getIngredients() + " -> $" + String.format("%.2f", getPrice()));
  }
}

// 2. The Concrete Component using a Record
record BasicEspresso() implements Coffee {

  @Override
  public String getIngredients() {
    return "Espresso";
  }

  @Override
  public double getPrice() {
    return 2.50;
  }
}

// 3. The Decorator Interface (Replaces the Abstract Base Class)
sealed interface CoffeeDecorator extends Coffee permits Milk, Caramel, Whip {

  // Concrete decorators must provide the instance they are wrapping.
  Coffee delegate();

  // Default methods forward calls to the delegate by default.
  // Concrete decorators only need to override the methods they want to modify!
  @Override
  default String getIngredients() {
    return delegate().getIngredients();
  }

  @Override
  default double getPrice() {
    return delegate().getPrice();
  }
}

// 4. Concrete Decorators using Records
// Notice the elegance: By naming the record component `delegate`, Java automatically 
// generates the `delegate()` accessor method, satisfying the CoffeeDecorator contract!

record Milk(Coffee delegate) implements CoffeeDecorator {

  @Override
  public String getIngredients() {
    // Appending to the delegated behavior
    return delegate().getIngredients() + ", Steamed Milk";
  }

  @Override
  public double getPrice() {
    // Adding cost to the delegated behavior
    return delegate().getPrice() + 0.75;
  }
}

record Caramel(Coffee delegate) implements CoffeeDecorator {

  @Override
  public String getIngredients() {
    return delegate().getIngredients() + ", Caramel Syrup";
  }

  @Override
  public double getPrice() {
    return delegate().getPrice() + 0.50;
  }
}

record Whip(Coffee delegate) implements CoffeeDecorator {

  @Override
  public String getIngredients() {
    return delegate().getIngredients() + ", Whipped Cream";
  }

  @Override
  public double getPrice() {
    return delegate().getPrice() + 0.60;
  }
}

// 5. Client Application
// Using Java 22+ Implicitly Declared Class / Instance Main Method
void main() {
  IO.println("--- Ordering a Basic Espresso ---");
  Coffee simpleCoffee = new BasicEspresso();
  simpleCoffee.printReceipt();

  IO.println("\n--- Ordering a Custom Latte ---");
  // Dynamically decorating the base component with multiple wrappers.
  // We pass the inner object to the constructor of the outer object.
  Coffee customLatte = new Whip(
      new Caramel(
          new Milk(
              new BasicEspresso()
          )
      )
  );

  customLatte.printReceipt();
}
