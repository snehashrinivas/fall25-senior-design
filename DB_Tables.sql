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
word_id INT PRIMARY KEY,
word VARCHAR(255),
word_frequency INT NOT NULL,
starting_word_occurences INT DEFAULT 0,
ending_word_occurences INT DEFAULT 0
);

# relationships table, current word and subsequent word combo is a composite primary key
CREATE TABLE Relationships (
current_word VARCHAR(255) NOT NULL,
next_word VARCHAR(255) NOT NULL,
PRIMARY KEY (current_word, next_word),
combination_count INT DEFAULT 0 
);