package edu.ewubd.cse489project;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Pair;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class FourInARowActivity extends AppCompatActivity {
    final private int PLAYER_NONE = 0;
    final private int PLAYER_RED = 1;
    final private int PLAYER_BLUE = 2;
    final private int TOTAL_ROW = 6;
    final private int TOTAL_COL = 7;

    int curPlayer = PLAYER_RED;
    int boardState[][] = new int[TOTAL_ROW][TOTAL_COL];
    List<Pair<Integer, Integer>> winningPos = new ArrayList<>();

    ConstraintLayout llBg;
    ImageView ivNextMove;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_four_in_arow);

        llBg = findViewById(R.id.llBg);
        ivNextMove = findViewById(R.id.ivNextMove);

        // Setting up onClickListeners for all of the columns. columnClicked takes
        // index of the columns and performs actions on the boardState array.
        // No cell has onClickListeners, since column pressing is enough.
        findViewById(R.id.llColumn1).setOnClickListener(view -> {columnClicked(0);});
        findViewById(R.id.llColumn2).setOnClickListener(view -> {columnClicked(1);});
        findViewById(R.id.llColumn3).setOnClickListener(view -> {columnClicked(2);});
        findViewById(R.id.llColumn4).setOnClickListener(view -> {columnClicked(3);});
        findViewById(R.id.llColumn5).setOnClickListener(view -> {columnClicked(4);});
        findViewById(R.id.llColumn6).setOnClickListener(view -> {columnClicked(5);});
        findViewById(R.id.llColumn7).setOnClickListener(view -> {columnClicked(6);});

        findViewById(R.id.btnRestartGame).setOnClickListener(view -> {restartGame();});
    }

    @Override
    protected void onResume() {
        super.onResume();
//        startService(new Intent(this, BackgroundMusicService.class));
    }

    @Override
    protected void onPause() {
        super.onPause();
//        stopService(new Intent(this, BackgroundMusicService.class));
    }

    private void columnClicked(int col) {
        // winningPos stores the winning cells, if a game has ended
        // After game ends no moves will be taken.
        if (winningPos.size() > 0) { // game has ended
            return;
        }

        // Checking each row of the column (col) received in the function
        // from bottom (5th index) to top (0th index) since, last cell
        // will be filled first.
        for (int row = 5; row >= 0; row--) {
            if (boardState[row][col] == PLAYER_NONE) {
                // boardState[row][col] indicates which players piece is that cell.
                // Setting it to current player.
                boardState[row][col] = curPlayer;

                // Updating the display of the current cell.
                updateCellDisplay(row, col);

                // Checking if the game has ended. if true, displays winner and ends game.
                if (checkIfWon()) {
                    displayWinningPos();
                    displayPopup();
                    return;
                }

                // Flip current player
                changeCurPlayer();
                return;
            }
        }
    }

    private void changeCurPlayer() {
        if (curPlayer == PLAYER_BLUE)
            setCurPlayer(PLAYER_RED);
        else if (curPlayer == PLAYER_RED)
            setCurPlayer(PLAYER_BLUE);
    }
    private void setCurPlayer(final int PLAYER) {
        // Sets current player to PLAYER and updates display accordingly.
        curPlayer = PLAYER;
        updateDisplayByCurPlayer();
    }
    private void updateDisplayByCurPlayer() {
        // Updating background
        if (curPlayer == PLAYER_RED)
            llBg.setBackgroundColor(getResources().getColor(R.color.player_red_bg));
        else
            llBg.setBackgroundColor(getResources().getColor(R.color.player_blue_bg));

        // Updating next move imageview (current player piece above the board)
        if (curPlayer == PLAYER_RED)
            ivNextMove.setImageResource(R.drawable.fourinarow_redcircle);
        else if (curPlayer == PLAYER_BLUE)
            ivNextMove.setImageResource(R.drawable.fourinarow_bluecircle);
    }

    private void updateCellDisplay(int row, int col) {
        // Updates the display of the cell positioned at [row, col]
        // according to the value of the boardState array at that position

        // Generates xml id of that cell, from String
        String viewIdString = "btnCell"+(row+1)+(col+1);
        int viewId = getResources().getIdentifier(viewIdString, "id", getPackageName());
        TextView btnCellRowCol = findViewById(viewId);

        // Updates the cell display
        if (boardState[row][col] == PLAYER_RED)
            btnCellRowCol.setBackground(getResources().getDrawable(R.drawable.fourinarow_redcircle));
        else if (boardState[row][col] == PLAYER_BLUE)
            btnCellRowCol.setBackground(getResources().getDrawable(R.drawable.fourinarow_bluecircle));
        else
            btnCellRowCol.setBackground(getResources().getDrawable(R.drawable.fourinarow_emptycircle));
    }

    private boolean checkIfWon() {
        // Returns true if game has ended
        // If game has been won, inserting winning positions in winningPos list.

        // checking column-wise four match
        for (int row = 0; row < TOTAL_ROW; row++) {
            for (int col = 0; col < TOTAL_COL - 3; col++) {
                if (boardState[row][col] == PLAYER_NONE) continue;
                boolean win = true;
                for (int i = 0; i <= 3; i++)
                    win = (win && (col + i < TOTAL_COL && boardState[row][col] == boardState[row][col + i]));
                if (win) {
                    for (int i = 0; i <= 3; i++)
                        winningPos.add(new Pair<>(row, col + i));
                    return true;
                }
            }
        }
        // checking row-wise four match
        for (int row = 0; row < TOTAL_ROW - 3; row++) {
            for (int col = 0; col < TOTAL_COL; col++) {
                if (boardState[row][col] == PLAYER_NONE) continue;
                boolean win = true;
                for (int i = 0; i <= 3; i++)
                    win = (win && (row + i < TOTAL_ROW && boardState[row][col] == boardState[row + i][col]));
                if (win) {
                    for (int i = 0; i <= 3; i++)
                        winningPos.add(new Pair<>(row + i, col));
                    return true;
                }
            }
        }
        // checking right-diagonal-wise four match
        for (int row = 0; row < TOTAL_ROW - 3; row++) {
            for (int col = 0; col < TOTAL_COL - 3; col++) {
                if (boardState[row][col] == PLAYER_NONE) continue;
                boolean win = true;
                for (int i = 0; i <= 3; i++)
                    win = (win && (row + i < TOTAL_ROW && col + i < TOTAL_COL && boardState[row][col] == boardState[row + i][col + i]));
                if (win) {
                    for (int i = 0; i <= 3; i++)
                        winningPos.add(new Pair<>(row + i, col + i));
                    return true;
                }
            }
        }
        // checking left-diagonal-wise four match
        for (int row = 0; row < TOTAL_ROW - 3; row++) {
            for (int col = 3; col < TOTAL_COL; col++) {
                if (boardState[row][col] == PLAYER_NONE) continue;
                boolean win = true;
                for (int i = 0; i <= 3; i++)
                    win = (win && (row + i < TOTAL_ROW && col - i >= 0 && boardState[row][col] == boardState[row + i][col - i]));
                if (win) {
                    for (int i = 0; i <= 3; i++)
                        winningPos.add(new Pair<>(row + i, col - i));
                    return true;
                }
            }
        }
        // checking if draw
        boolean isDraw = true;
        for (int row = 0; row < TOTAL_ROW; row++)
            for (int col = 0; col < TOTAL_COL; col++)
                isDraw = (isDraw && (boardState[row][col] != PLAYER_NONE));

        return isDraw;
    }

    private void displayWinningPos() {
        for (Pair<Integer, Integer> pair : winningPos) {
            int row = pair.first;
            int col = pair.second;
            String viewIdString = "btnCell" + (row + 1) + (col + 1);
            int viewId = getResources().getIdentifier(viewIdString, "id", getPackageName());
            TextView btnCellRowCol = findViewById(viewId);
            if (boardState[row][col] == PLAYER_RED)
                btnCellRowCol.setBackground(getResources().getDrawable(R.drawable.fourinarow_redcircle_win));
            else if (boardState[row][col] == PLAYER_BLUE)
                btnCellRowCol.setBackground(getResources().getDrawable(R.drawable.fourinarow_bluecircle_win));
        }
    }
    private void displayPopup() {
        boolean isDraw = (winningPos.size() == 0);
        ColorDrawable backgroundColor;
        String displayText;

        if (isDraw)
            backgroundColor = new ColorDrawable(ContextCompat.getColor(FourInARowActivity.this, R.color.player_none_bg_semi_transparent));
        else if (curPlayer == PLAYER_RED)
            backgroundColor = new ColorDrawable(ContextCompat.getColor(FourInARowActivity.this, R.color.player_red_bg_semi_transparent));
        else
            backgroundColor = new ColorDrawable(ContextCompat.getColor(FourInARowActivity.this, R.color.player_blue_bg_semi_transparent));

        if (isDraw)
            displayText = "DRAW!";
        else if (curPlayer == PLAYER_BLUE)
            displayText = "BLUE\nWON!";
        else
            displayText = "RED\nWON!";

        // Setting popup size w.r.t. screen size
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        Popup popup = new Popup(getLayoutInflater().inflate(R.layout.win_popup, null),
                displayMetrics.widthPixels,
                displayMetrics.heightPixels,
                getWindow().getDecorView(),
                backgroundColor,
                displayText,
                getResources().getColor(R.color.white));
        popup.displayPopup();
    }
    private void restartGame() {
        for (int row = 0; row < TOTAL_ROW; row++) {
            for (int col = 0; col < TOTAL_COL; col++) {
                if (boardState[row][col] == PLAYER_NONE) continue;
                boardState[row][col] = PLAYER_NONE;
                updateCellDisplay(row, col);
            }
        }
        winningPos.clear();
        setCurPlayer(PLAYER_RED);
    }

    // To save state when orientation is changed
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putSerializable("boardState", boardState);
        savedInstanceState.putInt("curPlayer", curPlayer);
        super.onSaveInstanceState(savedInstanceState);
    }
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        if (savedInstanceState != null) {
            boardState = (int[][]) savedInstanceState.getSerializable("boardState");
            curPlayer = savedInstanceState.getInt("curPlayer");
            updateDisplayByWholeGameState();
        }
    }
    private void updateDisplayByWholeGameState() {
        for (int row = 0; row < TOTAL_ROW; row++) {
            for (int col = 0; col < TOTAL_COL; col++) {
                updateCellDisplay(row, col);
            }
        }
        setCurPlayer(curPlayer);
        if (checkIfWon()) {
            displayWinningPos();
        }
    }
}