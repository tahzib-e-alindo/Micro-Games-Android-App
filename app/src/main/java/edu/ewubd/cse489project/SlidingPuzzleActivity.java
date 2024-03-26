package edu.ewubd.cse489project;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Pair;
import android.view.View;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Random;

public class SlidingPuzzleActivity extends AppCompatActivity {
    private final String MODE_NUMBER_TILES = "NumberTiles", MODE_DEFAULT_PICTURE_1 = "DefaultPicture1", MODE_DEFAULT_PICTURE_2 = "DefaultPicture2", MODE_CUSTOM_PICTURE = "CustomPicture";
    private String difficulty, pictureMode;
    private Bitmap customPicture = null;
    private int TOTAL_ROW = 3;
    private int TOTAL_COL = 3;
    private int EMPTY_CELL;
    final private Pair<Integer, Integer> ADJACENT[] = new Pair[]{new Pair<>(-1, 0), new Pair<>(0, 1), new Pair<>(1, 0), new Pair<>(0, -1)};
    int boardState[][];
    int moveCount = 0;
    Bitmap boardImage;
    Bitmap[][] boardImageGrid;
    GridLayout board;
    ArrayList<LinearLayout> llRows = new ArrayList<LinearLayout>(){};
    ImageView ivCell[][] = new ImageView[TOTAL_ROW][TOTAL_COL];
    TextView tvMoveCount, tvHighScore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Getting difficulty and picture mode from intent
        Intent intent = this.getIntent();
        Bundle bundle = intent.getExtras();
        difficulty = bundle.getString("difficulty");
        pictureMode = bundle.getString("pictureMode");
        if (pictureMode.equals(MODE_CUSTOM_PICTURE)) {
            // If picture mode is custom, get the picture from database

            String key = bundle.getString("customPictureKey");
            KeyValueDB db = new KeyValueDB(SlidingPuzzleActivity.this);
            String encodedPicture = db.getValueByKey(key);
            //decode base64 string to image
            byte[] byteArrayImage = Base64.decode(encodedPicture, Base64.DEFAULT);
            Bitmap decodedPicture = BitmapFactory.decodeByteArray(byteArrayImage, 0, byteArrayImage.length);
            db.close();

            // setting custom picture retrieved from database
            customPicture = decodedPicture;
        }

        if (difficulty.equals("Easy")) {
            TOTAL_ROW = TOTAL_COL = 3;
            setContentView(R.layout.activity_sliding_puzzle_easyboard);
        }
        else if (difficulty.equals("Medium")) {
            TOTAL_ROW = TOTAL_COL = 4;
            setContentView(R.layout.activity_sliding_puzzle_mediumboard);
        }
        else {
            TOTAL_ROW = TOTAL_COL = 5;
            setContentView(R.layout.activity_sliding_puzzle_hardboard);
        }

        // Empty cell is the last cell
        EMPTY_CELL = TOTAL_ROW * TOTAL_COL;

        // boardState[i][j] stores which piece is in this position.
        // Piece values are from 1 to number of cells.
        boardState = new int[TOTAL_ROW][TOTAL_COL];
        // ivCell is used to store the ImageViews in an array.
        ivCell = new ImageView[TOTAL_ROW][TOTAL_COL];

        tvMoveCount = findViewById(R.id.tvMoveCount);
        tvHighScore = findViewById(R.id.tvHighScore);

        board = findViewById(R.id.board);

        // initializing board. supposedValue refers which value is supposed to be in current position.
        int supposedValue = 1;
        for (int row = 0; row < TOTAL_ROW; row++) {
            for (int col = 0; col < TOTAL_COL; col++) {
                boardState[row][col] = supposedValue;
                supposedValue++;
            }
        }

        // Called to split the image into grid, and set it into the ImageViews.
        divideImageIntoGrid();

        findViewById(R.id.btnRestartGame).setOnClickListener(view -> {restartGame();});

        // Move count is initially 0.
        setMoveCount(0);

        // Getting high score from sp. Different high score picture mode and difficulty
        // and it is stored separately. For example: Highscore for Easy and NumberTiles is
        // stored in highScoreEasyNumberTiles
        SharedPreferences sp = getSharedPreferences("SlidingPuzzle", MODE_PRIVATE);
        String highScoreMode = "highScore" + difficulty + pictureMode;
        int highScore = sp.getInt(highScoreMode, -1);
        if (highScore != -1)
            tvHighScore.setText("High Score: " + String.valueOf(highScore));

        // Randomize the board to start game.
        randomizeBoard();

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

    private void divideImageIntoGrid() {
        // boardImage stores the image of the board.
        // Checking picture mode and setting it to boardImage.
        if (pictureMode.equals(MODE_NUMBER_TILES)) {
            if (difficulty.equals("Easy"))
                boardImage = BitmapFactory.decodeResource(getResources(), R.drawable.slidingpuzzle_easyboardimage);
            else if (difficulty.equals("Medium"))
                boardImage = BitmapFactory.decodeResource(getResources(), R.drawable.slidingpuzzle_mediumboardimage);
            else
                boardImage = BitmapFactory.decodeResource(getResources(), R.drawable.slidingpuzzle_hardboardimage);
        }
        else if (pictureMode.equals(MODE_DEFAULT_PICTURE_1))
            boardImage = BitmapFactory.decodeResource(getResources(), R.drawable.monalisa);
        else if (pictureMode.equals(MODE_DEFAULT_PICTURE_2))
            boardImage = BitmapFactory.decodeResource(getResources(), R.drawable.thestarrynight);
        else
            boardImage = customPicture;
        int width = boardImage.getWidth();
        int height = boardImage.getHeight();
        int pieceWidth = width / TOTAL_COL;
        int pieceHeight = height / TOTAL_ROW;

        // boardImageGrid stores image of each cell in respective indices.
        boardImageGrid = new Bitmap[TOTAL_ROW][TOTAL_COL];
        for (int row = 0; row < TOTAL_ROW; row++) {
            for (int col = 0; col < TOTAL_COL; col++) {
                // Dividing the image.
                boardImageGrid[row][col] = Bitmap.createBitmap(boardImage, col * pieceWidth, row * pieceHeight, pieceWidth, pieceHeight);
                // Setting up the ImageView of this cell.
                createCell(row, col);
            }
        }
    }
    private void createCell(int row, int col) {
        // Getting view id of this cell from String.
        String viewIdString = "ivCell"+(row+1)+(col+1);
        int viewId = getResources().getIdentifier(viewIdString, "id", getPackageName());
        // ivCell[row][cell] stores the ImageView of this cell.
        ivCell[row][col] = findViewById(viewId);
        // Setting up onClickListener
        ivCell[row][col].setOnClickListener(view -> {cellClicked(row, col);});

        // If not empty cell, set the respective image on this cell.
        if (boardState[row][col] != EMPTY_CELL)
            ivCell[row][col].setImageBitmap(boardImageGrid[row][col]);

        // This is done rounded corners.
        ivCell[row][col].setClipToOutline(true);
    }
    private void randomizeBoard() {
        // Finds the row and column of the empty cell.
        int emptyRow = 0, emptyCol = 0;
        for (int row = 0; row < TOTAL_ROW; row++) {
            for (int col = 0; col < TOTAL_COL; col++) {
                if (boardState[row][col] == EMPTY_CELL) {
                    emptyRow = row;
                    emptyCol = col;
                }
            }
        }
        Random random = new Random();
        int moves = 1000;
        while (moves > 0) {
            // Randomly moves the empty cell to adjacent cells
            int adjacentIndex = random.nextInt(4);
            int row = emptyRow + ADJACENT[adjacentIndex].first;
            int col = emptyCol + ADJACENT[adjacentIndex].second;
            if (0 <= row && row < TOTAL_ROW && 0 <= col && col < TOTAL_COL) {
                swapCells(emptyRow, emptyCol, row, col, false);
                emptyRow = row;
                emptyCol = col;
                moves--;
            }
        }
        // If current state is already solved, randomize again
        if (checkIfWon())
            randomizeBoard();
    }

    private void cellClicked(int row, int col) {
        // This is called when a cell is clicked.

        // If a game has ended, ignore click
        if (checkIfWon()) return;
        // If it is empty cell, ignore click
        if (boardState[row][col] == EMPTY_CELL) return;

        for (int i = 0; i < 4; i++) {
            int emptyRow = row + ADJACENT[i].first;
            int emptyCol = col + ADJACENT[i].second;
            // If position [emptyRow][emptyCol] is the empty cell,
            // swap with it and the move is done.
            if (0 <= emptyRow && emptyRow < TOTAL_ROW && 0 <= emptyCol && emptyCol < TOTAL_COL) {
                if (boardState[emptyRow][emptyCol] == EMPTY_CELL) {
                    setMoveCount(moveCount + 1);
                    swapCells(row, col, emptyRow, emptyCol, true);
                    return;
                }
            }
        }
    }

    private void setMoveCount(int value) {
        // sets moveCount to value and updates tvMoveCount
        moveCount = value;
        tvMoveCount.setText("MOVE: " + moveCount);
    }

    private void swapCells(int row1, int col1, int row2, int col2, boolean willCheckResult) {
        // Swaps two cells.

        // Swap the values in boardState, which indicates which piece is currently placed in it.
        int state1 = boardState[row1][col1];
        int state2 = boardState[row2][col2];
        boardState[row1][col1] = state2;
        boardState[row2][col2] = state1;

        // Swap the images.
        Drawable drawable1 = ivCell[row1][col1].getDrawable();
        Drawable drawable2 = ivCell[row2][col2].getDrawable();
        ivCell[row1][col1].setImageDrawable(drawable2);
        ivCell[row2][col2].setImageDrawable(drawable1);

        // Swap the stroke, which is an attribute of the parent layout
        Drawable background1 = ((View) ivCell[row1][col1].getParent()).getBackground();
        Drawable background2 = ((View)ivCell[row2][col2].getParent()).getBackground();
        ((View) ivCell[row1][col1].getParent()).setBackground(background2);
        ((View) ivCell[row2][col2].getParent()).setBackground(background1);

        // If board is solved and result should be checked,
        // display winner accordingly.
        if (willCheckResult && checkIfWon()) {
            initiateWin();
            return;
        }
    }

    private void initiateWin(){
        // Getting and updating high score from sp.
        SharedPreferences sp = getSharedPreferences("SlidingPuzzle", MODE_PRIVATE);
        String highScoreMode = "highScore" + difficulty + pictureMode;
        int highScore = sp.getInt(highScoreMode, -1);
        if (highScore == -1 || highScore > moveCount) {
            highScore = moveCount;
            tvHighScore.setText("High Score: " + String.valueOf(highScore));

            SharedPreferences.Editor spEditor = sp.edit();
            spEditor.putInt(highScoreMode, highScore);
            spEditor.apply();
        }

        displayPopup();
        tvMoveCount.setText("YOU WON WITH MOVE: " + moveCount);
    }

    private boolean checkIfWon() {
        // supposedValue refers which value is supposed to be in current position.
        int supposedValue = 1;
        boolean win = true;
        for (int row = 0; row < TOTAL_ROW; row++) {
            for (int col = 0; col < TOTAL_COL; col++) {
                win = win && (boardState[row][col] == supposedValue);
                supposedValue++;
            }
        }
        // If all piece is in correct position, player won.
        return win;
    }

    private void displayPopup() {
        // Setting popup size w.r.t. screen size
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        Popup popup = new Popup(getLayoutInflater().inflate(R.layout.win_popup, null),
                displayMetrics.widthPixels,
                displayMetrics.heightPixels,
                getWindow().getDecorView(),
                new ColorDrawable(ContextCompat.getColor(SlidingPuzzleActivity.this, R.color.player_none_bg_semi_transparent)),
                "You Won\nIn " + moveCount + (moveCount == 1 ? " Move!" : " Moves!"),
                getResources().getColor(R.color.white));
        popup.displayPopup();
    }

    private void restartGame() {
        solveGame();
        randomizeBoard();
        setMoveCount(0);
    }

    private void solveGame() {
        int supposedValue = 1;
        for (int row1 = 0; row1 < TOTAL_ROW; row1++) {
            for (int col1 = 0; col1 < TOTAL_COL; col1++) {
                if (boardState[row1][col1] != supposedValue) {
                    for (int row2 = 0; row2 < TOTAL_ROW; row2++) {
                        for (int col2 = 0; col2 < TOTAL_COL; col2++) {
                            if (boardState[row2][col2] == supposedValue) {
                                swapCells(row1, col1, row2, col2, false);
                            }
                        }
                    }
                }
                supposedValue++;
            }
        }
    }

    // To save state when orientation is changed
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putSerializable("boardState", boardState);
        savedInstanceState.putInt("moveCount", moveCount);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        if (savedInstanceState != null) {
            int[][] targetBoardState = (int[][]) savedInstanceState.getSerializable("boardState");
            moveCount = savedInstanceState.getInt("moveCount");
            updateDisplayByWholeGameState(targetBoardState);
        }
    }
    private void updateDisplayByWholeGameState(int[][] targetBoardState) {
        // updates board and move count based on current values
        setMoveCount(moveCount);
        for (int row1 = 0; row1 < TOTAL_ROW; row1++) {
            for (int col1 = 0; col1 < TOTAL_COL; col1++) {
                if (boardState[row1][col1] == targetBoardState[row1][col1]) continue;
                for (int row2 = 0; row2 < TOTAL_ROW; row2++) {
                    for (int col2 = 0; col2 < TOTAL_COL; col2++) {
                        if (boardState[row2][col2] == targetBoardState[row1][col1]) {
                            swapCells(row1, col1, row2, col2, false);
                        }
                    }
                }
            }
        }
    }
}