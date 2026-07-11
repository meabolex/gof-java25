import java.util.List;

public static final String EMP_02 = "EMP-02";

// 1. Auxiliary Domain Models
enum Role {ADMIN, USER, GUEST}

record Employee(String name, Role role) {

}

// 2. The Subject Interface using Sealed Types
sealed interface CompanyDatabase permits CoreDatabase, DatabaseAccessProxy {

  // The core operation that needs proxying
  String retrieveSalary(String employeeId);

  // Default method providing shared functionality without an abstract class.
  // NOTE: Because of polymorphism, when a client calls this on the Proxy, 
  // the internal call to retrieveSalary() still routes through the Proxy's 
  // access-control logic!
  default void printReport(List<String> employeeIds) {
    IO.println("--- Generating Salary Report ---");
    for (String id : employeeIds) {
      try {
        String salary = retrieveSalary(id);
        IO.println("ID: " + id + " -> " + salary);
      } catch (SecurityException e) {
        IO.println("ID: " + id + " -> [ACCESS DENIED: " + e.getMessage() + "]");
      }
    }
    IO.println("--------------------------------\n");
  }
}

// 3. The Real Subject
// We use a Record here to immutably hold configuration like a server URL.
record CoreDatabase(String serverUrl) implements CompanyDatabase {

  @Override
  public String retrieveSalary(String employeeId) {
    // Simulating an expensive or highly sensitive database lookup
    return switch (employeeId) {
      case "EMP-01" -> "$120,000";
      case EMP_02 -> "$95,000";
      case "EMP-03" -> "$80,000";
      default -> "Not Found";
    };
  }
}

// 4. The Proxy
// Records are fantastic for Protection Proxies. They immutably hold the delegate 
// (the Real Subject) and the context (the requester) making the request.
record DatabaseAccessProxy(CompanyDatabase delegate, Employee requester) implements
    CompanyDatabase {

  @Override
  public String retrieveSalary(String employeeId) {
    // Modern switch expression cleanly handles the access control logic
    return switch (requester.role()) {

      case ADMIN -> {
        // Admins get full access plus an audit log
        System.out.print("[AUDIT] Admin '" + requester.name() + "' queried " + employeeId + " | ");
        yield delegate.retrieveSalary(employeeId);
      }

      case USER -> {
        // Regular users can only access their own data 
        // (Assuming employeeId matches their name for this demo)
        if (requester.name().equals(employeeId)) {
          yield delegate.retrieveSalary(employeeId);
        } else {
          throw new SecurityException("Standard users may only view their own salary.");
        }
      }

      case GUEST -> throw new SecurityException("Guests cannot access financial data.");
    };
  }
}

// 5. Client Application
// Using Java 22+ Implicitly Declared Class / Instance Main Method
void main() {
  // The actual system we want to protect
  CompanyDatabase realDb = new CoreDatabase("jdbc:postgresql://internal-db:5432/hr");

  // Creating our actors
  Employee aliceAdmin = new Employee("Alice", Role.ADMIN);
  Employee bobUser = new Employee(EMP_02, Role.USER);
  Employee charlie = new Employee("Charlie", Role.GUEST);

  // Wrapping the real database in proxies tailored to the actors
  CompanyDatabase aliceProxy = new DatabaseAccessProxy(realDb, aliceAdmin);
  CompanyDatabase bobProxy = new DatabaseAccessProxy(realDb, bobUser);
  CompanyDatabase charlieProxy = new DatabaseAccessProxy(realDb, charlie);

  List<String> lookupIds = List.of("EMP-01", EMP_02, "EMP-03");

  IO.println("=== 1. ADMIN ACCESS ===");
  aliceProxy.printReport(lookupIds);

  IO.println("=== 2. USER ACCESS ===");
  bobProxy.printReport(lookupIds);

  IO.println("=== 3. GUEST ACCESS ===");
  charlieProxy.printReport(lookupIds);
}
