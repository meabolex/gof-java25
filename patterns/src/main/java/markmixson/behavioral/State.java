// ==========================================
// 1. The State Interface
// ==========================================
// A sealed interface strictly defines the allowed states in our domain.
sealed interface DocumentState permits Draft, Review, Published {

  // Default methods replace the traditional "AbstractState" base class.
  // They provide graceful error handling for invalid state transitions by default.
  default void submitForReview(Document document) {
    IO.println(" ❌ [Error] Cannot submit for review from the current state.");
  }

  default void approve(Document document) {
    IO.println(" ❌ [Error] Cannot approve from the current state.");
  }

  default void reject(Document document) {
    IO.println(" ❌ [Error] Cannot reject from the current state.");
  }

  // A contract method that every state MUST explicitly implement
  void render(Document document);
}

// ==========================================
// 2. Concrete States
// ==========================================
// Records are perfect here because these states are purely behavioral 
// and require no internal mutation.
record Draft() implements DocumentState {

  @Override
  public void submitForReview(Document document) {
    IO.println(" 🔄 Transition: Draft -> Review. Submitted to moderation.");
    document.setState(new Review());
  }

  @Override
  public void render(Document document) {
    IO.println(" 📄 Rendering [DRAFT]: " + document.content());
  }
}

record Review() implements DocumentState {

  @Override
  public void approve(Document document) {
    IO.println(" 🔄 Transition: Review -> Published. Document approved.");
    document.setState(new Published());
  }

  @Override
  public void reject(Document document) {
    IO.println(" 🔄 Transition: Review -> Draft. Revisions required.");
    document.setState(new Draft());
  }

  @Override
  public void render(Document document) {
    IO.println(" 🔎 Rendering [UNDER REVIEW]: " + document.content() + " (Watermarked)");
  }
}

record Published() implements DocumentState {
  // The Published state is terminal. Because it overrides none of the transition 
  // methods, it automatically inherits the "Error" responses from the default 
  // methods on the DocumentState interface!
  @Override
  public void render(Document document) {
    IO.println(" 📰 Rendering [PUBLISHED]: " + document.content());
  }
}

// ==========================================
// 3. The Context
// ==========================================
static final class Document {

  private DocumentState state;
  private final String content;

  public Document(String content) {
    this.content = content;
    this.state = new Draft(); // Initial state
  }

  // Package-private setter: Only our State objects should be able to mutate this!
  void setState(DocumentState state) {
    this.state = state;
  }

  public String content() {
    return content;
  }

  // The Context delegates all behavioral logic to its current State object
  public void submitForReview() {
    state.submitForReview(this);
  }

  public void approve() {
    state.approve(this);
  }

  public void reject() {
    state.reject(this);
  }

  public void render() {
    state.render(this);
  }
}

// ==========================================
// 4. Client Application
// ==========================================
// Using Java 22+ Implicitly Declared Class / Instance Main Method
void main() {
  IO.println("=== Initializing Document ===\n");
  Document article = new Document("Java 25 Design Patterns Guide");

  // 1. Initial State: Draft
  article.render();

  // Attempt an invalid transition
  article.approve();

  IO.println("\n=== Moving to Moderation ===");
  // 2. Draft to Review
  article.submitForReview();
  article.render();

  IO.println("\n=== Moderator Rejects ===");
  // 3. Review to Draft
  article.reject();
  article.render();

  IO.println("\n=== Resubmitting and Approving ===");
  // 4. Draft to Review, then to Published
  article.submitForReview();
  article.approve();
  article.render();

  IO.println("\n=== Terminal State Checks ===");
  // 5. Attempting to modify a Published document
  article.submitForReview();
  article.reject();
}
