# the files table, file id is primary key
CREATE TABLE Files (
file_id INT AUTO_INCREMENT PRIMARY KEY,
filename VARCHAR(255) NOT NULL,
file_word_count INT NOT NULL,
import_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

# generated sentences table, sessionid is primary key
CREATE TABLE GeneratedSentences (
sessionID INT AUTO_INCREMENT PRIMARY KEY,
comments VARCHAR(255), # optional
rating SMALLINT CHECK (rating BETWEEN 1 AND 5), # optional
time_generated DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

# words table, the word id is primary key
CREATE TABLE Words (
word_id INT AUTO_INCREMENT PRIMARY KEY,
word VARCHAR(255) UNIQUE,
word_frequency INT NOT NULL DEFAULT 0,
starting_word_occurences INT DEFAULT 0,
ending_word_occurences INT DEFAULT 0
);

# relationships table, current word and subsequent word combo is a composite primary key
CREATE TABLE Relationships (
current_word_id INT NOT NULL,
next_word_id INT NOT NULL,
combination_count INT DEFAULT 0,
PRIMARY KEY (current_word_id, next_word_id),
FOREIGN KEY (current_word_id) REFERENCES Words(word_id) ON DELETE CASCADE,
FOREIGN KEY (next_word_id) REFERENCES Words(word_id) ON DELETE CASCADE
);
