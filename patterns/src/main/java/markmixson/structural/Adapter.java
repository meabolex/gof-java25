// 1. The Target Interface
// This is the modern interface that our client code expects and knows how to use.
interface ModernPaymentProcessor {

  // The core method the client will call
  void payInUsd(String accountId, double amountUsd);

  // Default method to share logic without an abstract base class.
  // Every adapter automatically inherits this audit functionality.
  default void auditTransaction(String accountId, double amountUsd) {
    IO.println("[AUDIT] Successfully processed $" + String.format("%.2f", amountUsd) +
        " for account: " + accountId);
  }
}

// 2. The Adaptee (Legacy System)
// This is an existing class with an incompatible interface. 
// It only understands Euros and uses a completely different method name.
static final class LegacyEuroBankSystem {

  public void executeSepaTransfer(String iban, double amountEuros) {
    IO.println("[LEGACY BANK] Executed SEPA transfer of €" +
        String.format("%.2f", amountEuros) + " to IBAN: " + iban);
  }
}

// 3. The Adapter
// We use a Record here. Since an adapter's primary job is to hold a reference 
// to the Adaptee and translate calls, a record is a perfect, boilerplate-free fit.
record LegacyBankAdapter(LegacyEuroBankSystem legacySystem) implements ModernPaymentProcessor {

  // Static conversion rate for demonstration
  private static final double USD_TO_EUR_RATE = 0.92;

  @Override
  public void payInUsd(String accountId, double amountUsd) {
    IO.println("Adapter: Translating modern USD request to legacy Euro request...");

    // 1. Translate the data format (USD -> EUR)
    double amountEuros = amountUsd * USD_TO_EUR_RATE;

    // 2. Delegate the call to the underlying legacy system
    legacySystem.executeSepaTransfer(accountId, amountEuros);

    // 3. Utilize the default method from the Target interface
    auditTransaction(accountId, amountUsd);
  }
}

// 4. A Modern Concrete Implementation (Optional, for context)
// This shows a class that natively implements the Target interface without adaptation.
record StripePaymentProcessor() implements ModernPaymentProcessor {

  @Override
  public void payInUsd(String accountId, double amountUsd) {
    IO.println("[STRIPE] Native USD payment of $" + amountUsd + " to " + accountId);
    auditTransaction(accountId, amountUsd);
  }
}

// 5. Client Application
// Using Java 22+ Implicitly Declared Class / Instance Main Method
void main() {
  IO.println("--- Native Modern Implementation ---");
  ModernPaymentProcessor modernProcessor = new StripePaymentProcessor();
  modernProcessor.payInUsd("user_abc123", 50.00);

  IO.println("\n--- Adapted Legacy Implementation ---");
  // The client wants to use the ModernPaymentProcessor interface, but our 
  // infrastructure only has the LegacyEuroBankSystem. 
  LegacyEuroBankSystem oldBank = new LegacyEuroBankSystem();

  // We wrap the legacy system in our adapter.
  ModernPaymentProcessor adaptedProcessor = new LegacyBankAdapter(oldBank);

  // The client interacts purely with the modern interface, completely unaware 
  // of the EUR conversion or the legacy method signatures happening under the hood.
  adaptedProcessor.payInUsd("DE89370400440532013000", 100.00);
}
