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

Technologies Used
- Java 22+
- MySQL 8.0+
- MySQL Workbench
- JDBC (MySQL Connector/J)

How to setup project
- Download MySQL Community Server and MySQL Workbench
- Start the MySQL server.
- Create a new schema and run DB_Tables.sql script
