package edu.brown.cs.student.main;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Autocorrect but with databases.
 * <p>
 * Chooses to pass SQL exceptions on to the class that instantiates it.
 */
public class Database {


  private static Connection conn = null;
  private static List<String> words = new ArrayList<>();

  /**
   * Instantiates the database, creating tables if necessary.
   * Automatically loads files.
   *
   * @param filename file name of SQLite3 database to open.
   * @throws SQLException if an error occurs in any SQL query.
   */
  Database(String filename) throws SQLException, ClassNotFoundException {

    /*
     * TODO: Initialize the database connection, turn foreign keys on,
     *  and then create the word and corpus tables if they do not exist.
     */
  }


  /**
   * Reads words from a text file if the file hasn't been already loaded.
   *
   * @param filename name of file to read.
   * @throws SQLException if something goes wrong with a SQL query.
   */
  void readCorpus(String filename) throws SQLException, ClassNotFoundException {

    PreparedStatement prep =
        conn.prepareStatement("SELECT * from corpus WHERE filename=?");
    prep.setString(1, filename);
    ResultSet rs = prep.executeQuery();
    if (rs.isClosed()) {

      prep = conn.prepareStatement("INSERT INTO corpus VALUES (NULL, ?)");
      prep.setString(1, filename);
      prep.executeUpdate();
      prep.close();

      prep = conn.prepareStatement("SELECT id from corpus WHERE filename=?");
      prep.setString(1, filename);
      rs = prep.executeQuery();
      int id = rs.getInt(1);
      List<String> corpus = Autocorrector.parseCorpus(filename);
      System.err.println("Reading data from file " + filename + "...");
      for (String word : corpus) {
        prep = conn.prepareStatement("INSERT INTO word VALUES (?, ?) ");
        prep.setInt(1, id);
        prep.setString(2, word);
        prep.executeUpdate();
        prep.close();
      }
      System.err.println("\u001b[32mDone reading data \u001b[0m");

    } else {
      rs.close();
      prep.close();

      prep = conn.prepareStatement(
          "SELECT word.word from word JOIN corpus ON word.corpus_id = corpus.id WHERE corpus.filename=?;");
      prep.setString(1, filename);
      rs = prep.executeQuery();
      if (!rs.isClosed()) {
        while (rs.next()) {
          words.add(rs.getString(1));
        }
      }
      prep.close();
      rs.close();

      rs.close();
      prep.close();
    }
  }

  /**
   * Reads all the words from the database into the words list.
   *
   * @throws SQLException if something goes wrong with the SQL
   */
  void readAll() throws SQLException {

    PreparedStatement prep = conn.prepareStatement("SELECT word.word FROM word;");
    ResultSet rs = prep.executeQuery();
    while (rs.next()) {
      words.add(rs.getString(1));
    }
    rs.close();
    prep.close();
  }


  /**
   * Returns a map from corpus name to number of words in the database.
   *
   * @return Map
   * @throws SQLException if something goes wrong with a SQL query.
   */
  Map<String, Integer> getFrequencyMap() throws SQLException {
    Map<String, Integer> freqMap = new HashMap<>();
    //TODO: select all filenames and how many words are associated with those filenames from the database
    PreparedStatement prep = conn.prepareStatement(""); //Your SQL here!
    ResultSet rs = prep.executeQuery();
    while (rs.next()) {
      freqMap.put(rs.getString(1), rs.getInt(2));
    }

    prep.close();
    rs.close();

    return freqMap;
  }

  /**
   * Returns a map containing the top 5 most common words in the database, mapped to
   * the number of times they occur.
   *
   * @return Map
   * @throws SQLException if something goes wrong in the SQL.
   */
  Map<String, Integer> getInstanceMap() throws SQLException {
    Map<String, Integer> instMap = new HashMap<>();
    //TODO: select the five most common words from the entire database, and how many times they appear
    PreparedStatement prep = conn.prepareStatement(""); //Your SQL Here!
    ResultSet rs = prep.executeQuery();
    while (rs.next()) {
      instMap.put(rs.getString(1), rs.getInt(2));
    }

    prep.close();
    rs.close();
    return instMap;
  }

  /**
   * Retrieves all words that should be used.
   *
   * @return words.
   */
  public List<String> getWords() {
    return words;
  }
}
