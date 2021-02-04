package edu.brown.cs.student.main;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Trie data structure for storing strings, prefixes in particular.
 */
public class Trie {

  private Map<Character, Trie> children;
  private boolean isWord;

  /**
   * Creates an empty Trie object.
   */
  public Trie() {
    isWord = false;
    children = new HashMap<Character, Trie>();
  }

  /**
   * Inserts a single word into Trie.
   *
   * @param word
   */
  public void insert(String word) {
    if (word.equals("")) {
      isWord = true;
    } else {
      char firstChar = word.charAt(0);
      if (!(children.containsKey(firstChar))) {
        children.put(firstChar, new Trie());
      }
      children.get(firstChar).insert(word.substring(1));
    }
  }

  /**
   * Inserts a list of words into Trie.
   *
   * @param words
   */
  public void insertAll(List<String> words) {
    for (String word : words) {
      insert(word);
    }
  }

  /**
   * Determines if prefix input is in Trie.
   *
   * @param prefix
   * @return True if held, false otherwise
   */
  public boolean hasPrefix(String prefix) {
    if (prefix.equals("")) {
      return true;
    } else {
      char firstChar = prefix.charAt(0);
      if (children.containsKey(firstChar)) {
        return children.get(firstChar).hasPrefix(prefix.substring(1));
      } else {
        return false;
      }
    }
  }

  /**
   * Returns true if Trie contains word.
   *
   * @param word
   * @return True if word is in trie, false otherwise.
   */
  public boolean hasWord(String word) {
    if (word.equals("")) {
      return isWord;
    } else {
      char firstChar = word.charAt(0);
      if (children.containsKey(firstChar)) {
        return children.get(firstChar).hasWord(word.substring(1));
      } else {
        return false;
      }
    }
  }

  /**
   * Finds all words in trie with given prefix.
   *
   * @param prefix      the prefix in question
   * @param totalPrefix repeat parameter necessary for building strings.
   * @return A set of strings that are in trie and have input prefix
   */
  public Set<String> findAllWithPrefix(String prefix, String totalPrefix) {
    if (prefix.equals("")) {
      return findAll(new TreeSet<String>(), totalPrefix);
    } else {
      char firstChar = prefix.charAt(0);
      if (children.containsKey(firstChar)) {
        return children.get(firstChar)
            .findAllWithPrefix(prefix.substring(1), totalPrefix);
      } else {
        return Collections.emptySet();
      }
    }
  }

  /**
   * Helper function to find all words from a certain Trie node.
   *
   * @param currentList Set of words found so far.
   * @param prefix      Characters built up to current node
   * @return All possible words formed from node in Trie
   */
  private Set<String> findAll(Set<String> currentList, String prefix) {
    if (isWord) {
      currentList.add(prefix);
    }
    Map<Character, Trie> possibilities = children;
    for (char c : possibilities.keySet()) {
      StringBuilder sb = new StringBuilder();
      sb.append(prefix);
      sb.append(c);
      possibilities.get(c).findAll(currentList, sb.toString());
    }
    return currentList;
  }

  /**
   * Given a phrase, sees if there is way to split by space and both new words
   * exist in Trie.
   *
   * @param word a word without spaces.
   * @return A list of words with spaces that can be formed by input phrase and
   * are in Trie.
   */
  public Set<String> whiteSpace(String word) {
    Set<String> currentList = new TreeSet<String>();

    for (int i = 1; i < word.length(); i++) {
      String word1 = word.substring(0, i);
      String word2 = word.substring(i);

      if (this.hasWord(word1) && this.hasWord(word2)) {
        StringBuilder sb = new StringBuilder();
        sb.append(word1);
        sb.append(' ');
        sb.append(word2);
        currentList.add(sb.toString());
      }
    }
    return currentList;
  }

  /**
   * Finds all words within a certain led from some phrase in trie.
   *
   * @param phrase      Phrase of query
   * @param maxDistance Max led
   * @param prefix      Current prefix built up to this point
   * @param currentList Current set of words found
   * @return Returns set of all words with led less than or equal to phrase in
   * trie.
   */
  private Set<String> findLedWithin(String phrase, int maxDistance,
                                    String prefix, Set<String> currentList) {

    int currentDist = maxDistance + 1;

    if (isWord) {
      currentDist = getLedDistance(phrase, prefix);
    }

    if (currentDist <= maxDistance) {
      currentList.add(prefix);
    }
    if (prefix.length() - phrase.length() <= maxDistance) {
      Map<Character, Trie> possibilities = children;
      for (char c : possibilities.keySet()) {
        StringBuilder sb = new StringBuilder();
        sb.append(prefix);
        sb.append(c);
        possibilities.get(c).findLedWithin(phrase, maxDistance, sb.toString(),
            currentList);
      }
    }
    return currentList;
  }

  /**
   * Finds all words in Trie with led less than or equal to maxDistance from
   * phrase.
   *
   * @param phrase      Phrase from query
   * @param maxDistance max led
   * @return Returns list of all words with led less than or equal to phrase in
   * trie
   */
  public Set<String> findLedWithinRoot(String phrase, int maxDistance) {
    return findLedWithin(phrase, maxDistance, "", new TreeSet<String>());
  }

  /**
   * Gets led between two words.
   * Uses dynamic programming.
   *
   * @param word1 First word
   * @param word2 Second word
   * @return led between two words
   */
  public static int getLedDistance(String word1, String word2) {
    int size1 = word1.length();
    int size2 = word2.length();
    int[][] ledMatrix = new int[size1 + 1][size2 + 1];

    for (int i = 0; i < size1 + 1; i++) {
      for (int j = 0; j < size2 + 1; j++) {

        // Comparing if word1 was empty.
        if (i == 0) {
          ledMatrix[i][j] = j;
        } else if (j == 0) {
          // Comparing is word2 was empty.
          ledMatrix[i][j] = i;
        } else {

          // See if adding corresponding characters would not increase led.
          int substitution = 1;
          if (word1.charAt(i - 1) == word2.charAt(j - 1)) {
            substitution = 0;
          }
          // Take min of insertion, deletion, substituion.
          ledMatrix[i][j] = Math.min(Math.min(ledMatrix[i][j - 1] + 1, ledMatrix[i - 1][j] + 1),
              ledMatrix[i - 1][j - 1] + substitution);
        }
      }
    }
    return ledMatrix[size1][size2];
  }
}
