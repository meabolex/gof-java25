// ==========================================
// 1. Domain Models
// ==========================================
record RawData(String content, String format) {

}

record ProcessedData(String payload) {

}

// ==========================================
// 2. The Template Interface
// ==========================================
// Sealed interface restricts implementations, acting as a guardrail 
// to protect our default template method from being overridden by unknown classes.
sealed interface DataETLPipeline permits CsvToDbPipeline, JsonToApiPipeline {

  // ----------------------------------------------------
  // THE TEMPLATE METHOD
  // ----------------------------------------------------
  // Orchestrates the exact sequence of the algorithm.
  // Concrete implementations inherit this but should NOT override it.
  default void executePipeline(String source) {
    IO.println("=== Starting ETL Pipeline: " + pipelineName() + " ===");

    IO.println(" [Step 1] Extracting...");
    RawData raw = extract(source);

    if (raw == null || raw.content().isBlank()) {
      IO.println(" ❌ Extraction yielded no data. Aborting pipeline.\n");
      return;
    }

    IO.println(" [Step 2] Transforming...");
    ProcessedData processed = transform(raw);

    IO.println(" [Step 3] Loading...");
    load(processed);

    IO.println("=== Pipeline Execution Complete ===\n");
  }

  // ----------------------------------------------------
  // ABSTRACT STEPS
  // ----------------------------------------------------
  // These must be provided by the concrete implementations.
  String pipelineName();

  RawData extract(String source);

  ProcessedData transform(RawData data);

  void load(ProcessedData data);
}

// ==========================================
// 3. Concrete Implementations
// ==========================================
// Records are excellent here because these pipeline configurations 
// are stateless and purely behavioral once instantiated.
record CsvToDbPipeline(String databaseUrl) implements DataETLPipeline {

  @Override
  public String pipelineName() {
    return "CSV -> Relational Database";
  }

  @Override
  public RawData extract(String source) {
    // Simulating file extraction
    return new RawData("id,name,age\n1,Alice,30\n2,Bob,25", "CSV");
  }

  @Override
  public ProcessedData transform(RawData data) {
    // Simulating parsing CSV into SQL statements
    String sql = "INSERT INTO users VALUES ('Alice', 30), ('Bob', 25);";
    return new ProcessedData(sql);
  }

  @Override
  public void load(ProcessedData data) {
    IO.println("   -> Executing SQL at " + databaseUrl + ":");
    IO.println("      " + data.payload());
  }
}

record JsonToApiPipeline(String apiKey) implements DataETLPipeline {

  @Override
  public String pipelineName() {
    return "JSON -> External REST API";
  }

  @Override
  public RawData extract(String source) {
    // Simulating reading a JSON payload from a queue
    return new RawData("""
        { "users": [{"name": "Charlie", "role": "Admin"}] }
        """, "JSON");
  }

  @Override
  public ProcessedData transform(RawData data) {
    // Simulating minimizing JSON or translating keys for the target API
    String minified = data.content()
        .replace(" ", "")
        .replace("\n", "");
    return new ProcessedData(minified);
  }

  @Override
  public void load(ProcessedData data) {
    IO.println("   -> POSTing to API with Key [" + apiKey + "]:");
    IO.println("      " + data.payload());
  }
}

// ==========================================
// 4. Client Application
// ==========================================
// Using Java 22+ Implicitly Declared Class / Instance Main Method
void main() {
  // 1. Execute the CSV to Database Pipeline
  DataETLPipeline dbPipeline = new CsvToDbPipeline("jdbc:postgresql://localhost:5432/prod");
  dbPipeline.executePipeline("/mnt/data/users.csv");

  // 2. Execute the JSON to API Pipeline
  DataETLPipeline apiPipeline = new JsonToApiPipeline("sk_live_abc123");
  apiPipeline.executePipeline("queue://events/user-signup");
}
