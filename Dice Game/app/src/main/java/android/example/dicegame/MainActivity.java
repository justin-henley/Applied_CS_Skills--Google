package android.example.dicegame;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Random;

public class MainActivity extends AppCompatActivity {
    int userTotalScore = 0;
    int userTurnScore = 0;
    int computerTotalScore = 0;
    int computerTurnScore = 0;
    final int TURN_DELAY = 1000;
    final int MAX_COMP_TURN_SCORE = 20;
    final int MAX_SCORE = 100;

    // Handler for delaying computer turns
    Handler turnHandler = new Handler(Looper.myLooper());
    Runnable turnRunnable = new Runnable() {
        @Override
        public void run() {
            // Generate the first roll before entering loop
            int rollValue = dieValue();

            // NOTE: I know this loops and rolls a value one extra time, but this forces
            //       the app to delay before declaring a winner or end of turn

            // If computer has enough points to win, hold and display win screen
            if (computerTurnScore + computerTotalScore >= MAX_SCORE) {
                computerTotalScore += computerTurnScore;
                winScreen();
            }
            else if (computerTurnScore >= MAX_COMP_TURN_SCORE) {
                cleanComputerTurn();
            }
            else if (rollValue == 1) {
                // turn ends if roll is 1
                computerTurnScore = 0;
                cleanComputerTurn();
            }
            else {
                computerTurnScore += rollValue;
                updateScoreboard("Computer turn score: " + computerTurnScore);
                turnHandler.postDelayed(this, TURN_DELAY);
            }
            // i think this creates an infinite loop
            //turnHandler.postDelayed(this, 500);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        updateScoreboard("press ROLL to start");
    }

    public void rollDie(View view) {
        // Get a random die roll value and draw on screen
        int rollValue = dieValue();

        if (rollValue == 1) {
            // If user rolls a one, turn score is cleared
            userTurnScore = 0;

            // Update message
            updateScoreboard("Turn ended");

            // Pass control to computer's turn
            computerTurn();
        }
        else {
            // Else, roll is added to turn score
            userTurnScore += rollValue;

            // Update message
            updateScoreboard("Your turn score: " + userTurnScore);
        }
    }

    // Stores user score when user clicks hold button
    public void holdScore(View view) {
        // Add turn value to total user score and clear turn value
        userTotalScore += userTurnScore;
        userTurnScore = 0;

        // Pass to computer turn if user has not reached winning score
        if (userTotalScore >= MAX_SCORE) {
            // Declare winner and force reset to play next game
            winScreen();
        }
        else {
            // Update message
            updateScoreboard("User holds");

            // Pass control to computer's turn
            computerTurn();
        }
    }

    // Resets game to zero
    public void resetGame(View view) {
        // Stop computer turn if running, otherwise runnable squashes the reset and keeps going after scores are zeroed out
        turnHandler.removeCallbacks(turnRunnable);

        // Set all scores to zero
        userTotalScore = userTurnScore = computerTotalScore = computerTurnScore = 0;

        // Update message
        TextView textView = (TextView) findViewById(R.id.scoreView);
        String displayText = "Your score: " + userTotalScore + ", Computer score: " + computerTotalScore +
                ", Game reset";
        textView.setText(displayText);

        // Unlock buttons
        findViewById((R.id.roll_button)).setClickable(true);
        findViewById((R.id.hold_button)).setClickable(true);
    }

    // Computer's turn
    protected void computerTurn() {
        // Lock roll and hold buttons during computer turn
        findViewById((R.id.roll_button)).setClickable(false);
        findViewById((R.id.hold_button)).setClickable(false);

        // All turn management is done INSIDE the runnable called here
        turnHandler.postDelayed(turnRunnable, TURN_DELAY);
    }

    // Performs cleanup at end of computer turn
    protected void cleanComputerTurn() {
        // Update computer score
        if (computerTurnScore == 0) {
            updateScoreboard("Computer rolled a one");
        }
        else {
            computerTotalScore += computerTurnScore;
            updateScoreboard("Computer holds");
            computerTurnScore = 0;
        }

        // Unlock roll and hold buttons at end of computer turn
        findViewById((R.id.roll_button)).setClickable(true);
        findViewById((R.id.hold_button)).setClickable(true);
    }

    // Rolls a random number between 1 and 6, hardcoded;
    protected int dieValue() {
        // New random number generator
        Random random = new Random();

        int rollValue = 1 + random.nextInt(6);

        // A reference to the rolled drawable die image
        Drawable dieImg;

        // Choose the desired die face
        switch(rollValue) {
            case 1:
                dieImg = getResources().getDrawable(R.drawable.dice1, getTheme());
                break;
            case 2:
                dieImg = getResources().getDrawable(R.drawable.dice2, getTheme());
                break;
            case 3:
                dieImg = getResources().getDrawable(R.drawable.dice3, getTheme());
                break;
            case 4:
                dieImg = getResources().getDrawable(R.drawable.dice4, getTheme());
                break;
            case 5:
                dieImg = getResources().getDrawable(R.drawable.dice5, getTheme());
                break;
            case 6:
                dieImg = getResources().getDrawable(R.drawable.dice6, getTheme());
                break;
            default:
                // shouldn't reach here, what to do?
                dieImg = getResources().getDrawable(R.drawable.dice1, getTheme());
        }

        // Change image on screen
        ImageView imageView = findViewById(R.id.dieView);
        imageView.setImageDrawable(dieImg);

        // Generate a random value from 1 to 6
        return rollValue;
    }

    // Updates the score with a specified trailing message
    protected void updateScoreboard(String message) {
        TextView textView = (TextView) findViewById(R.id.scoreView);
        String displayText = "Your score: " + userTotalScore + ", Computer score: " + computerTotalScore +
                ", " + message;
        textView.setText(String.format((String) getString(R.string.scoreboard_message, userTotalScore, computerTotalScore, message)));
    }

    // Shows winner and forces reset for new game
    protected void winScreen() {
        // Lock roll and hold buttons to force player to press reset
        findViewById((R.id.roll_button)).setClickable(false);
        findViewById((R.id.hold_button)).setClickable(false);

        // Display winner
        if (userTotalScore >= MAX_SCORE) {
            updateScoreboard("YOU WIN!");
        }
        else if (computerTotalScore >= MAX_SCORE) {
            updateScoreboard("Computer won :(");
        }
        else {
            // should not end up here
            updateScoreboard("ERROR WHAT HAVE YOU DONE");
        }
    }
}