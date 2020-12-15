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

package com.google.engedu.wordstack;

import android.content.res.AssetManager;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Random;
import java.util.Stack;

public class MainActivity extends AppCompatActivity {

    private static final int WORD_LENGTH = 5;
    public static final int LIGHT_BLUE = Color.rgb(176, 200, 255);
    public static final int LIGHT_GREEN = Color.rgb(200, 255, 200);
    private ArrayList<String> words = new ArrayList<>();
    private ArrayList<String> fullDictionary = new ArrayList<>();
    private Random random = new Random();
    private StackedLayout stackedLayout;
    private String word1, word2;
    private Stack<LetterTile> placedTiles = new Stack<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AssetManager assetManager = getAssets();
        try {
            InputStream inputStream = assetManager.open("words.txt");
            BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while((line = in.readLine()) != null) {
                String word = line.trim();
                fullDictionary.add(word);
                if (word.length() == WORD_LENGTH) {
                    words.add(word);
                }
            }
        } catch (IOException e) {
            Toast toast = Toast.makeText(this, "Could not load dictionary", Toast.LENGTH_LONG);
            toast.show();
        }
        LinearLayout verticalLayout = findViewById(R.id.vertical_layout);
        stackedLayout = new StackedLayout(this);
        verticalLayout.addView(stackedLayout, 3);

        View word1LinearLayout = findViewById(R.id.word1);
        //word1LinearLayout.setOnTouchListener(new TouchListener());
        word1LinearLayout.setOnDragListener(new DragListener());
        View word2LinearLayout = findViewById(R.id.word2);
        //word2LinearLayout.setOnTouchListener(new TouchListener());
        word2LinearLayout.setOnDragListener(new DragListener());
    }

    private class TouchListener implements View.OnTouchListener {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN && !stackedLayout.empty()) {
                LetterTile tile = (LetterTile) stackedLayout.peek();
                tile.moveToViewGroup((ViewGroup) v);
                if (stackedLayout.empty()) {
                    TextView messageBox = findViewById(R.id.message_box);
                    messageBox.setText(String.format(getString(R.string.display_originals), word1, word2));
                }
                placedTiles.push(tile);
                return true;
            }
            return false;
        }
    }

    private class DragListener implements View.OnDragListener {

        public boolean onDrag(View v, DragEvent event) {
            int action = event.getAction();
            switch (event.getAction()) {
                case DragEvent.ACTION_DRAG_STARTED:
                    v.setBackgroundColor(LIGHT_BLUE);
                    v.invalidate();
                    return true;
                case DragEvent.ACTION_DRAG_ENTERED:
                    v.setBackgroundColor(LIGHT_GREEN);
                    v.invalidate();
                    return true;
                case DragEvent.ACTION_DRAG_EXITED:
                    v.setBackgroundColor(LIGHT_BLUE);
                    v.invalidate();
                    return true;
                case DragEvent.ACTION_DRAG_ENDED:
                    v.setBackgroundColor(Color.WHITE);
                    v.invalidate();
                    return true;
                case DragEvent.ACTION_DROP:
                    // Dropped, reassign Tile to the target Layout
                    LetterTile tile = (LetterTile) event.getLocalState();
                    tile.moveToViewGroup((ViewGroup) v);
                    if (stackedLayout.empty()) {
                        TextView messageBox = findViewById(R.id.message_box);
                        messageBox.setText(String.format(getString(R.string.display_originals), word1, word2));
                        checkAnswer();
                    }
                    placedTiles.push(tile);
                    return true;
            }
            return false;
        }
    }

    public boolean onStartGame(View view) {
        // Clear the previous game (if any)
        LinearLayout word1LinearLayout = findViewById(R.id.word1);
        word1LinearLayout.removeAllViews();
        LinearLayout word2LinearLayout = findViewById(R.id.word2);
        word2LinearLayout.removeAllViews();
        stackedLayout.clear();
        placedTiles.clear();
        

        TextView messageBox = findViewById(R.id.message_box);
        messageBox.setText(R.string.game_start);

        // Pick two random words
        Random random = new Random();
        word1 = words.get(random.nextInt(words.size() - 1));
        word2 = words.get(random.nextInt(words.size() - 1));

        shuffleAndReverse();

        return true;
    }

    public boolean onUndo(View view) {
        if (!placedTiles.empty() && !stackedLayout.empty()) {
            LetterTile tile = placedTiles.pop();
            tile.moveToViewGroup(stackedLayout);
            return true;
        }
        else
            return false;
    }

    // Shuffles together the two strings, preserving their orders relative to their words
    // Returns the shuffled string in reverse order for pushing onto a stack
    // Called recursively until a sufficiently shuffled string is returned
    private void shuffleAndReverse() {
        // shuffle words into a single mixed string, with word orders preserved
        StringBuilder shuffled = new StringBuilder();
        int count1 = 0, count2 = 0;
        while (count1 != WORD_LENGTH && count2 != WORD_LENGTH) {
            shuffled.append(random.nextBoolean() ? word1.charAt(count1++) : word2.charAt(count2++));
        }
        while (count1 != WORD_LENGTH) {
            shuffled.append(word1.charAt(count1++));
        }
        while (count2 != WORD_LENGTH) {
            shuffled.append(word2.charAt(count2++));
        }

        // Check if shuffled
        if (shuffled.indexOf(word1) != -1 || shuffled.indexOf(word2) != -1) {
            shuffleAndReverse();
        }
        else {
            // Push each letter onto stackedLayout as a LetterTile in reverse order
            shuffled.reverse();
            for (int i = 0; i < shuffled.length(); i++) {
                LetterTile tile = new LetterTile(this, shuffled.charAt(i));
                stackedLayout.push(tile);
            }
        }
    }


    // tells the user if they found good words
    private void checkAnswer() {
        // Build answer strings from the viewgroups
        StringBuilder build1 = new StringBuilder();
        StringBuilder build2 = new StringBuilder();
        String answer1, answer2, toastText;

        ViewGroup v1 = findViewById(R.id.word1);
        ViewGroup v2 = findViewById(R.id.word2);

        for (int i = 0; i < v1.getChildCount(); i++) {
            build1.append(((LetterTile) v1.getChildAt(i)).getText());
        }
        for (int i = 0; i < v2.getChildCount(); i++) {
            build2.append(((LetterTile) v2.getChildAt(i)).getText());
        }

        answer1 = build1.toString();
        answer2 = build2.toString();

        // Check if the answers are in dictionary
        if ((answer1.compareTo(word1) == 0 || answer1.compareTo(word2) == 0)
                && (answer2.compareTo(word1) == 0 || answer2.compareTo(word2) == 0)) {
            toastText = "Perfect match!";
        }
        else if (fullDictionary.contains(answer1) && fullDictionary.contains(answer2)) {
            toastText = "Not the words we wanted, but the words we deserved.";
        }
        else {
            toastText = "Not even close!";
        }

        Toast toast = Toast.makeText(this, toastText, Toast.LENGTH_LONG);
        toast.show();
    }
}
