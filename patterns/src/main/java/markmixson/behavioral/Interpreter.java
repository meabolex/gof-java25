import java.util.HashMap;
import java.util.Map;

// ==========================================
// 1. The Context
// ==========================================
// Holds the global state/environment during interpretation.
record Context(Map<String, Integer> variables) {

  public Context() {
    this(new HashMap<>());
  }

  public void assign(String variable, int value) {
    variables.put(variable, value);
  }

  public int lookup(String variable) {
    if (!variables.containsKey(variable)) {
      throw new IllegalArgumentException("Variable '" + variable + "' is not defined.");
    }
    return variables.get(variable);
  }
}

// ==========================================
// 2. The Abstract Expression
// ==========================================
// Sealed interface enforces a strict grammar constraint. Only the types 
// permitted here can exist in our expression tree.
sealed interface Expression permits Constant, Variable, Add, Multiply, Subtract {

  // The core Interpreter method
  int interpret(Context context);

  // Default methods acting as a Fluent API for building the AST!
  // This entirely replaces the need for an abstract base class or utility builders.
  default Expression plus(Expression right) {
    return new Add(this, right);
  }

  default Expression minus(Expression right) {
    return new Subtract(this, right);
  }

  default Expression multiply(Expression right) {
    return new Multiply(this, right);
  }

  // A default convenience method for context-free evaluation
  default int evaluate() {
    return interpret(new Context());
  }
}

// ==========================================
// 3. Terminal Expressions
// ==========================================
// Leaves of the AST. They resolve to concrete values or lookup variables.

record Constant(int value) implements Expression {

  @Override
  public int interpret(Context context) {
    return value;
  }
}

record Variable(String name) implements Expression {

  @Override
  public int interpret(Context context) {
    return context.lookup(name);
  }
}

// ==========================================
// 4. Non-Terminal Expressions
// ==========================================
// Branches of the AST. They recursively call interpret() on their children.

record Add(Expression left, Expression right) implements Expression {

  @Override
  public int interpret(Context context) {
    return left.interpret(context) + right.interpret(context);
  }
}

record Subtract(Expression left, Expression right) implements Expression {

  @Override
  public int interpret(Context context) {
    return left.interpret(context) - right.interpret(context);
  }
}

record Multiply(Expression left, Expression right) implements Expression {

  @Override
  public int interpret(Context context) {
    return left.interpret(context) * right.interpret(context);
  }
}

// ==========================================
// 5. Client Application
// ==========================================
// Using Java 22+ Implicitly Declared Class / Instance Main Method
void main() {
  IO.println("--- Setting up Context ---");
  Context context = new Context();
  context.assign("x", 10);
  context.assign("y", 5);
  context.assign("z", 2);

  IO.println("x = 10, y = 5, z = 2\n");

  IO.println("--- Building and Interpreting Expressions ---");

  // 1. A traditional AST tree creation using Record constructors
  // Expression: (x + y)
  Expression equation1 = new Add(new Variable("x"), new Variable("y"));
  IO.println("(x + y) = " + equation1.interpret(context));

  // 2. Leveraging the Interface Default Methods for Fluent AST creation
  // Expression: (x + y) * z
  Expression equation2 = new Variable("x")
      .plus(new Variable("y"))
      .multiply(new Variable("z"));

  IO.println("(x + y) * z = " + equation2.interpret(context));

  // 3. Complex Fluent chaining with Constants
  // Expression: (x * 5) - (y + z)
  Expression equation3 = new Variable("x")
      .multiply(new Constant(5))
      .minus(new Variable("y")
          .plus(new Variable("z")));

  IO.println("(x * 5) - (y + z) = " + equation3.interpret(context));

  // 4. Using the default context-free evaluation method
  IO.println("\n--- Context-Free Evaluation ---");
  Expression simpleMath = new Constant(10).multiply(new Constant(20));
  IO.println("10 * 20 = " + simpleMath.evaluate());
}
