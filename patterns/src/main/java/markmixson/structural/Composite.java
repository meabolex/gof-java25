import java.util.ArrayList;
import java.util.List;

// 1. The Component Interface using a Sealed Interface
// Defines the common contract for both individual files and directories.
sealed interface FileSystemNode permits File, Directory {

  String name();

  int calculateSize();

  void print(String indent);

  // Default method representing a shared workflow (replaces an abstract class).
  // This provides a unified entry point for executing the recursive operations.
  default void display() {
    IO.println("--- Node: " + name() + " ---");
    print("");
    IO.println("Total Size: " + calculateSize() + " bytes\n");
  }

  // Default methods for child management (Transparent Composite approach).
  // Leaves will inherit these and throw exceptions if misused, 
  // without needing to implement the boilerplate themselves.
  default void add(FileSystemNode node) {
    throw new UnsupportedOperationException(name() + " is a leaf node. Cannot add children.");
  }

  default void remove(FileSystemNode node) {
    throw new UnsupportedOperationException(name() + " is a leaf node. Cannot remove children.");
  }
}

// 2. The Leaf using a Record
// Records are perfect for leaves because they are lightweight and immutable.
record File(String name, int size) implements FileSystemNode {

  @Override
  public int calculateSize() {
    // Base case for the recursive size calculation
    return size;
  }

  @Override
  public void print(String indent) {
    IO.println(indent + "- [File] " + name + " (" + size + " bytes)");
  }
}

// 3. The Composite
// A final class is used here because a classic composite needs mutable state 
// to manage its children over time.
static final class Directory implements FileSystemNode {

  private final String name;
  private final List<FileSystemNode> children = new ArrayList<>();

  public Directory(String name) {
    this.name = name;
  }

  @Override
  public String name() {
    return this.name;
  }

  // The composite overrides the default interface methods to provide real functionality
  @Override
  public void add(FileSystemNode node) {
    children.add(node);
  }

  @Override
  public void remove(FileSystemNode node) {
    children.remove(node);
  }

  @Override
  public int calculateSize() {
    // Aggregating the sizes of all children using the Stream API
    return children.stream()
        .mapToInt(FileSystemNode::calculateSize)
        .sum();
  }

  @Override
  public void print(String indent) {
    IO.println(indent + "+ [Directory] " + name);

    // Recursively delegating the print operation to children
    for (FileSystemNode child : children) {
      child.print(indent + "  ");
    }
  }
}

// 4. Client Application
// Using Java 22+ Implicitly Declared Class / Instance Main Method
void main() {
  // Creating the tree structure
  FileSystemNode root = new Directory("root");
  FileSystemNode bin = new Directory("bin");
  FileSystemNode user = new Directory("user");

  FileSystemNode bash = new File("bash", 1024);
  FileSystemNode ls = new File("ls", 2048);

  // Building the hierarchy
  bin.add(bash);
  bin.add(ls);

  user.add(new File(".bashrc", 512));
  user.add(new File(".profile", 256));

  root.add(bin);
  root.add(user);
  root.add(new File("boot.img", 4096));

  IO.println("--- Displaying the Entire File System ---");
  // Client treats the root directory uniformly via the interface
  root.display();

  IO.println("--- Displaying a Single Leaf Uniformly ---");
  // Client treats an individual file the exact same way
  bash.display();

  IO.println("--- Testing Transparent Interface Guard ---");
  try {
    bash.add(new File("virus.exe", 9999));
  } catch (UnsupportedOperationException e) {
    IO.println("Exception caught: " + e.getMessage());
  }
}
