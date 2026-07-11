import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// ==========================================
// 1. The Flyweight Interface
// ==========================================
// Sealed interface restricts domain models and avoids abstract classes.
sealed interface TreeModel permits SharedTreeModel {

  // Intrinsic state accessors (automatically fulfilled by the Record)
  String species();

  String leafColor();

  String barkTexture();

  // Default method handles the behavior that requires both intrinsic 
  // state (from the accessors) and extrinsic state (passed as parameters).
  default void render(int x, int y) {
    IO.println(
        "Rendering [" + species() + "] tree at coordinates (" + x + ", " + y + ") " + "| Color: "
            + leafColor() + " | Bark: " + barkTexture());
  }
}

// ==========================================
// 2. The Concrete Flyweight (Intrinsic State)
// ==========================================
// Records are naturally immutable, making them perfect for shared, read-only state.
// The record implicitly implements species(), leafColor(), and barkTexture().
record SharedTreeModel(String species, String leafColor, String barkTexture) implements
    TreeModel {

}

// ==========================================
// 3. The Flyweight Factory
// ==========================================
// Manages the pool of shared intrinsic state objects.
static final class TreeModelFactory {

  // Thread-safe cache to store our shared flyweights
  private final Map<String, TreeModel> cache = new ConcurrentHashMap<>();

  public TreeModel getTreeModel(String species, String leafColor, String barkTexture) {
    // Create a unique key for the intrinsic state combination
    String key = species + "_" + leafColor;

    // Return existing model if present, otherwise create and cache a new one
    return cache.computeIfAbsent(key, k -> {
      IO.println("   --> [FACTORY] Creating new shared instance for: " + species);
      return new SharedTreeModel(species, leafColor, barkTexture);
    });
  }
}

// ==========================================
// 4. The Context Object (Extrinsic State)
// ==========================================
// This represents the unique instance the client interacts with.
// It stores its own unique coordinates, but delegates heavy data to the shared Flyweight.
record Tree(int x, int y, TreeModel model) {

  public void draw() {
    // Passes the extrinsic state (x, y) to the flyweight
    model.render(x, y);
  }
}

// ==========================================
// 5. Client Application
// ==========================================
// Using Java 22+ Implicitly Declared Class / Instance Main Method

void main() {
  TreeModelFactory factory = new TreeModelFactory();
  List<Tree> forest = new ArrayList<>();

  IO.println("--- Planting the Forest ---");

  // Client requests a Pine tree. The factory creates it.
  TreeModel pineModel = factory.getTreeModel("Pine", "Dark Green", "Rough");
  forest.add(new Tree(10, 20, pineModel));

  // Client requests another Pine tree. The factory reuses the existing instance.
  TreeModel pineModel2 = factory.getTreeModel("Pine", "Dark Green", "Rough");
  forest.add(new Tree(15, 30, pineModel2));

  // Client requests an Oak tree. The factory creates it.
  TreeModel oakModel = factory.getTreeModel("Oak", "Autumn Red", "Smooth");
  forest.add(new Tree(50, 60, oakModel));

  // 1,000 more pines would all share the exact same 'pineModel' memory reference.

  IO.println("\n--- Rendering the Forest ---");
  for (Tree tree : forest) {
    tree.draw();
  }

  IO.println("\n--- Memory Integrity Check ---");
  IO.println("Are both pine trees sharing the exact same model in memory? " + (pineModel
      == pineModel2));
}
