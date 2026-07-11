import java.util.ArrayDeque;
import java.util.Deque;

// ==========================================
// 1. The Memento Interface
// ==========================================
// This is a "Marker Interface". It has absolutely no methods.
// This guarantees that the Caretaker cannot read or alter the state.
interface EditorState {

}

// ==========================================
// 2. The Originator Interface
// ==========================================
interface Originator {

  EditorState save();

  void restore(EditorState state);
}

// ==========================================
// 3. The Caretaker Interface
// ==========================================
// By defining contract methods `history()` and `originator()`, we can write 
// the undo/redo logic as default methods. No abstract base class needed!
interface HistoryManager {

  // Contract methods for state required by the default methods
  Deque<EditorState> history();

  Originator originator();

  default void backup() {
    IO.println("[Caretaker] Backing up state...");
    history().push(originator().save());
  }

  default void undo() {
    if (history().isEmpty()) {
      IO.println("[Caretaker] No history to undo.");
      return;
    }
    IO.println("\n[Caretaker] Undoing last action...");
    EditorState previousState = history().pop();
    originator().restore(previousState);
  }
}

// ==========================================
// 4. Concrete Originator
// ==========================================
static final class TextEditor implements Originator {

  private String text = "";
  private int cursorPosition = 0;

  public void type(String newText) {
    text += newText;
    cursorPosition += newText.length();
    IO.println("Editor Output: \"" + text + "\" | Cursor at: " + cursorPosition);
  }

  // THE MAGIC: The Concrete Memento is a Private Record.
  // - Records are deeply immutable, perfect for snapshots.
  // - Because it is private, the outside world literally cannot cast to it or read its fields.
  private record Snapshot(String text, int cursorPosition) implements EditorState {

  }

  @Override
  public EditorState save() {
    // We create the snapshot and return it disguised as the empty EditorState interface
    return new Snapshot(this.text, this.cursorPosition);
  }

  @Override
  public void restore(EditorState state) {
    // Modern Java Pattern Matching for instanceof safely unpacks the record
    if (state instanceof Snapshot(String text1, int position)) {
      this.text = text1;
      this.cursorPosition = position;
      IO.println(
          "Editor Restored: \"" + this.text + "\" | Cursor at: " + this.cursorPosition);
    } else {
      throw new IllegalArgumentException("Unknown memento type provided!");
    }
  }
}

// ==========================================
// 5. Concrete Caretaker
// ==========================================
// Records implicitly fulfill the `history()` and `originator()` contract 
// methods required by the HistoryManager interface.
record EditorController(Originator originator, Deque<EditorState> history) implements
    HistoryManager {

  // Custom constructor to provide the default history stack automatically
  public EditorController(Originator originator) {
    this(originator, new ArrayDeque<>());
  }
}

// ==========================================
// 6. Client Application
// ==========================================
// Using Java 22+ Implicitly Declared Class / Instance Main Method
void main() {
  IO.println("=== Starting Text Editor ===\n");

  TextEditor editor = new TextEditor();
  HistoryManager controller = new EditorController(editor);

  // 1. Initial State
  editor.type("Hello");
  controller.backup(); // Save "Hello"

  // 2. More typing
  editor.type(" World");
  controller.backup(); // Save "Hello World"

  // 3. A mistake!
  editor.type("! I am a robot.");

  // 4. Time to undo
  controller.undo(); // Reverts to "Hello World"
  controller.undo(); // Reverts to "Hello"

  // 5. Empty undo test
  /*
  Reverts to initial empty state (technically fails gracefully because stack is empty if we didn't
  save the very first empty state)
   */
  controller.undo();
}
