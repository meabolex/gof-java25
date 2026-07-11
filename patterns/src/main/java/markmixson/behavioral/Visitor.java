import java.util.List;

// ==========================================
// 1. The Visitable Interface (Element)
// ==========================================
// Sealed interface locks down the node types, guaranteeing visitors
// know about every possible element in the domain.
sealed interface ContentNode permits TextNode, ImageNode, VideoNode {

  // The core of the Visitor pattern: Double Dispatch
  void accept(ContentVisitor visitor);
}

// ==========================================
// 2. Concrete Elements
// ==========================================
// Records implicitly provide our fields, constructors, and accessors.
// They only need to implement the dispatch handshake.
record TextNode(String text) implements ContentNode {

  @Override
  public void accept(ContentVisitor visitor) {
    visitor.visit(this);
  }
}

record ImageNode(String url, String altText) implements ContentNode {

  @Override
  public void accept(ContentVisitor visitor) {
    visitor.visit(this);
  }
}

record VideoNode(String videoId, int durationSeconds) implements ContentNode {

  @Override
  public void accept(ContentVisitor visitor) {
    visitor.visit(this);
  }
}

// ==========================================
// 3. The Visitor Interface
// ==========================================
interface ContentVisitor {

  // Default methods replace the need for an `AbstractVisitor` adapter class!
  // By providing empty defaults, concrete visitors only need to override
  // the specific `visit` methods they actually care about.

  default void visit(TextNode node) {
    // No-op by default
  }

  default void visit(ImageNode node) {
    // No-op by default
  }

  default void visit(VideoNode node) {
    // No-op by default
  }

  // A shared utility method utilizing default interface behavior
  default void visitAll(Iterable<ContentNode> nodes) {
    for (ContentNode node : nodes) {
      node.accept(this);
    }
  }
}

// ==========================================
// 4. Concrete Visitors
// ==========================================
// This visitor cares about EVERYTHING. It overrides all methods.
static final class HtmlExportVisitor implements ContentVisitor {

  private final StringBuilder htmlBuilder = new StringBuilder();

  @Override
  public void visit(TextNode node) {
    htmlBuilder.append("  <p>").append(node.text()).append("</p>\n");
  }

  @Override
  public void visit(ImageNode node) {
    htmlBuilder.append("  <img src=\"").append(node.url()).append("\" alt=\"")
        .append(node.altText()).append("\" />\n");
  }

  @Override
  public void visit(VideoNode node) {
    htmlBuilder.append("  <video src=\"").append(node.videoId()).append("\" duration=\"")
        .append(node.durationSeconds()).append("s\"></video>\n");
  }

  public String getHtml() {
    return htmlBuilder.toString();
  }
}

// This visitor ONLY cares about TextNodes.
// Thanks to the default methods in the interface, it doesn't have to 
// write empty boilerplate methods for Images or Videos!
static final class WordCountAnalyzer implements ContentVisitor {

  private int wordCount = 0;

  @Override
  public void visit(TextNode node) {
    // Simple word count logic
    String[] words = node.text().trim().split("\\s+");
    if (words.length > 0 && !words[0].isEmpty()) {
      wordCount += words.length;
    }
  }

  public int getWordCount() {
    return wordCount;
  }
}

// ==========================================
// 5. Client Application
// ==========================================
// Using Java 22+ Implicitly Declared Class / Instance Main Method
void main() {
  IO.println("=== Initializing CMS Document ===");

  List<ContentNode> document = List.of(
      new TextNode("Welcome to the Java 25 Patterns Guide."),
      new ImageNode("/img/visitor-diagram.png", "UML Diagram of Visitor"),
      new TextNode("The Visitor pattern separates algorithms from object structures."),
      new VideoNode("vid_xyz123", 120));

  // 1. Run the HTML Exporter
  HtmlExportVisitor htmlExporter = new HtmlExportVisitor();
  htmlExporter.visitAll(document);

  IO.println("\n--- HTML Output ---");
  IO.println(htmlExporter.getHtml());

  // 2. Run the Word Counter
  WordCountAnalyzer wordCounter = new WordCountAnalyzer();
  wordCounter.visitAll(document);

  IO.println("--- Analysis Output ---");
  IO.println("Total Words: " + wordCounter.getWordCount());
}
