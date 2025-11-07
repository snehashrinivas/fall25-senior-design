# fall25-senior-design

Overview:
Sentence Builder is a language-processing system that constructs grammatically valid sentences based on word relationships extracted from text documents.
It uses a MySQL database to store word frequency data and relationships between consecutive words.
A Java-based parser reads text files, processes them into tokens, and populates the database.
A Java-based logic layer applies NLP-based algorithms to analyze word relationships and generate contextually coherent sentence predictions.
A JavaFX frontend interface can later use the stored data to generate new sentences or provide autocomplete suggestions.

Features:
- Parses text files to extract and store words in a MySQL database
- Tracks word frequencies and sentence start/end occurrences
- Stores relationships between consecutive words (bigram-style)
- Supports rating and commenting on generated sentences
- Modular design for frontend integration

Database Schema:
- Files Table: Stores metadata about each imported file
- Generated Sentences Table: Stores generated sentences with optional comments/ratings
- Words Table: Stores each unique word, its frequency, and start/end occurrences
- Relationships Table: Maps relationships between consecutive words (bigrams)

JavaFX Frontend:
- Home: Choose first word; buttons for Generate Sentence and Auto Complete
- Word Completion: shows three suggested next words with Re-Roll and Finish
- Feedback: shows generated sentence and a clickable 1–5 star rating
- Thank You: confirmation screen with Back to Home

Technologies Used
- Java 22+
- MySQL 8.0+
- MySQL Workbench
- JDBC (MySQL Connector/J)
- JavaFX SDK 25

How to setup project
- Download MySQL Community Server and MySQL Workbench
- Start the MySQL server.
- Create a new schema and run DB_Tables.sql script
- Open the project in IntelliJ.
- Create Run Configuration -> Application:
- Main class: frontend.FrontendMain
- VM options (adjust the path to your JavaFX SDK):
- macOS/Linux: --module-path "/path/to/javafx-sdk-25.0.1/lib" --add-modules javafx.controls,javafx.fxml
- Windows: --module-path "C:\path\to\javafx-sdk-25.0.1\lib" --add-modules javafx.controls,javafx.fxml
- Run the configuration.
