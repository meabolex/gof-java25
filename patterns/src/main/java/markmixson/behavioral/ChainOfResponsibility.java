import java.util.List;

// 1. Auxiliary Domain Models
enum Severity {LOW, MEDIUM, CRITICAL, FATAL}

record Ticket(String id, Severity severity, String description) {

}

// 2. The Chain of Responsibility Interface
@FunctionalInterface
interface TicketHandler {

  // The core contract: returns true if handled, false if it should be passed on
  boolean tryHandle(Ticket ticket);

  // Default method replacing the need for an abstract base class.
  // It dynamically creates a new handler that encapsulates the CoR delegation logic.
  default TicketHandler orElse(TicketHandler next) {
    return ticket -> {
      // Step 1: Try to handle the request with the current handler
      if (this.tryHandle(ticket)) {
        return true;
      }
      // Step 2: If it fails, pass it to the next handler in the chain (if it exists)
      if (next != null) {
        return next.tryHandle(ticket);
      }
      // Step 3: End of the chain, request was unhandled
      IO.println(
          "[UNRESOLVED] Ticket " + ticket.id() + " could not be resolved by any tier.");
      return false;
    };
  }
}

// 3. Concrete Handlers using Records
// Because the delegation logic is handled by the interface, these records 
// are purely focused on their domain logic and remain perfectly stateless.

record Level1Support() implements TicketHandler {

  @Override
  public boolean tryHandle(Ticket ticket) {
    if (ticket.severity() == Severity.LOW) {
      IO.println(
          "[L1 Support] Handled Ticket " + ticket.id() + ": " + ticket.description());
      return true;
    }
    return false; // Not my job, pass it on
  }
}

record Level2Support() implements TicketHandler {

  @Override
  public boolean tryHandle(Ticket ticket) {
    if (ticket.severity() == Severity.MEDIUM) {
      IO.println(
          "[L2 Support] Handled Ticket " + ticket.id() + ": " + ticket.description());
      return true;
    }
    return false;
  }
}

record EngineeringTeam() implements TicketHandler {

  @Override
  public boolean tryHandle(Ticket ticket) {
    // Modern switch expression to handle multiple severities
    return switch (ticket.severity()) {
      case CRITICAL, FATAL -> {
        IO.println("[Engineering] Deployed hotfix for Ticket " + ticket.id() + ": "
            + ticket.description());
        yield true;
      }
      default -> false;
    };
  }
}

// 4. Client Application
// Using Java 22+ Implicitly Declared Class / Instance Main Method
void main() {
  IO.println("--- Building the Support Chain ---");

  // We compose the chain fluently. Each call to `orElse` wraps the previous
  // handlers in a new lambda closure, forming the chain dynamically.
  TicketHandler supportChain = new Level1Support().orElse(new Level2Support())
      .orElse(new EngineeringTeam());

  // Creating some test requests
  List<Ticket> queue = List.of(
      new Ticket("T-100", Severity.LOW, "Password reset link expired"),
      new Ticket("T-101", Severity.MEDIUM, "Cannot export PDF report"),
      new Ticket("T-102", Severity.FATAL, "Production database is down!"),
      new Ticket("T-103", Severity.LOW, "How do I change my avatar?"));

  IO.println("\n--- Processing Ticket Queue ---");

  // The client interacts only with the head of the chain.
  for (Ticket ticket : queue) {
    supportChain.tryHandle(ticket);
  }
}
