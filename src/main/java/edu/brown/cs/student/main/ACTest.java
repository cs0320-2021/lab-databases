package edu.brown.cs.student.main;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import java.sql.SQLException;

import static org.junit.Assert.*;

public class ACTest {


  @Test
  public void testDatabase() throws SQLException {
    Database db = new Database("data/data2.sqlite3");
    db.readCorpus("data/dictionary.txt");
  }
}
