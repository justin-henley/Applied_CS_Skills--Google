/* Copyright 2016 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.engedu.anagrams;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class AnagramDictionary {

    private static final int MIN_NUM_ANAGRAMS = 5;
    private static final int DEFAULT_WORD_LENGTH = 3;
    private static final int MAX_WORD_LENGTH = 7;
    private Random random = new Random();
    private ArrayList<String> wordList = new ArrayList<>();  // Holds all dictionary entries
    private HashSet<String> wordSet = new HashSet<>();  // A hash set of dict entries with O(1) existence checking
    private HashMap<String, ArrayList<String> > lettersToWords = new HashMap<>();  // Each key is a sorted string, each value is a list of all words matching that sorted string (all anagrams of each other)
    private HashMap<Integer, ArrayList<String> >  sizeToWords = new HashMap<>();
    private int wordLength = DEFAULT_WORD_LENGTH;

    public AnagramDictionary(Reader reader) throws IOException {
        BufferedReader in = new BufferedReader(reader);
        String line;

        // TODO Milestone 3: Refactoring and Extensions

        // Strip all words into an ArrayList
        while((line = in.readLine()) != null) {
            String word = line.trim();
            wordList.add(word);
            // Add entry to wordSet
            wordSet.add(word);

            // Add entry to lettersToWords
            // sort the word
            String sortedWord = sortLetters(word);
            // check if lettersToWords has this sorted string yet
            if (lettersToWords.containsKey(sortedWord)) {
                // If the entry exists, retrieve the list and add the current word to the list
                lettersToWords.get(sortedWord).add(word);
            }
            else {
                // Create the ArrayList for this hashed (sorted) string
                ArrayList<String> anagramGroup = new ArrayList<>();
                anagramGroup.add(word);
                // Create the HashMap entry for this anagram grouping
                lettersToWords.put(sortedWord, anagramGroup);
            }

            // Add entry to sizeToWords
            // Check if this length has an entry yet
            int currLength = word.length();
            if (sizeToWords.containsKey(currLength)) {
                // Yes: add to the  ArrayList of words this size
                sizeToWords.get(currLength).add(word);
            }
            else {
                // Create the array list for this size
                ArrayList<String> sizeGroup = new ArrayList<>();
                sizeGroup.add(word);
                // Create HashMap entry for this size group
                sizeToWords.put(currLength, sizeGroup);
            }
        }


    }

    public boolean isGoodWord(String word, String base) {
        // Return false if word is NOT in dictionary
        if (!wordSet.contains(word)) return false;

        // Check if word contains the base word
        if (word.contains(base)) return false;

        // If both checks pass, return true
        return true;
    }

    public List<String> getAnagrams(String targetWord) {
        ArrayList<String> result = new ArrayList<String>();
        targetWord = sortLetters(targetWord);  // Only sort once

        // Iterate over dictionary and compare every entry
        for (String s: wordList) {
            // Check if lengths are same, if not cant be an anagram
            if (targetWord.length() == s.length()) {
                if (targetWord.equals(sortLetters(s))) {
                    result.add(s);
                }
            }
        }
        return result;
    }

    public List<String> getAnagramsWithOneMoreLetter(String word) {
        ArrayList<String> result = new ArrayList<String>();

        // Add the anagrams of the current word
        result.addAll(getAnagrams(word));

        // Try adding every lowercase letter, and checking for anagrams of that sorted string
        for (char c = 'a'; c <= 'z'; c++) {
            // Add the letter to the word
            String extendedWord = word + c;
            // Resort the word
            extendedWord = sortLetters(extendedWord);
            // Check if that anagram group exists in lettersToWords
            if (lettersToWords.containsKey(extendedWord)) {
                // If exists, add all words in the list of that anagram group
                for (String s: lettersToWords.get(extendedWord)) {
                    result.add(s);
                }
            }
        }

        return result;
    }

    public String pickGoodStarterWord() {
        // Define a local list reference for the list of wordLength-length words
        ArrayList<String> sizeList = sizeToWords.get(wordLength);

        // Pick a random word in sizeList
        int randomIndex = random.nextInt(Objects.requireNonNull(sizeList).size() - 1);  // Allows for next line without danger
        int currIndex = randomIndex + 1;  // Allows to check if a full loop completed and return error

        // Iteratively check words until one is found, wrapping around to beginning if necessary
        while (currIndex != randomIndex) {
            // get word and sort
            String entry = sizeList.get(currIndex);
            String sortedEntry = sortLetters(entry);
            // Check number of anagrams is at least the minimum
            int possibleAnagrams = Objects.requireNonNull(lettersToWords.get(sortedEntry)).size();
            // Also check anagrams adding one letter
            possibleAnagrams += getAnagramsWithOneMoreLetter(sortedEntry).size();

            if (possibleAnagrams >= MIN_NUM_ANAGRAMS) {
                // Increment word size unless maximum reached
                if (wordLength < MAX_WORD_LENGTH)
                    wordLength++;
                // Return the picked starting word
                return entry;
            }
            else {
                // Increment if end of list not reached
                if (currIndex < sizeList.size() - 1) {
                    currIndex++;
                }
                // Else wrap around to start
                else {
                    currIndex = 0;
                }
            }
        }

        // If a good value is never found, give a word signifying error
        return "ERROR:NO_WORD_FOUND";
    }

    private String sortLetters(String unsorted) {
        char[] arr = unsorted.toCharArray();
        Arrays.sort(arr);
        String sorted = new String(arr);

        return sorted;
    }
}
