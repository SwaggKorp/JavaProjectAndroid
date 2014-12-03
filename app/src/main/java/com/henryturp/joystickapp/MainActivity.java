package com.henryturp.joystickapp;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;

import java.util.List;

public class MainActivity extends Activity {

    /* File Manager bugs like hell. To do with setupGrid? Review gameGrid resizing techniques above all. After fix file manager
    * Spinner response also seems dodgy at times.*/

    RelativeLayout joystickLayout;
    RelativeLayout menuLayout;
    GridLayout gameGridLayout;
    RelativeLayout mainLayout;
    ImageView menu_button;
    Button resetButton, saveButton, deleteButton;
    EditText saveEditText;
    Spinner fileSpinner;

    GameGrid gameGrid;
    FileManager fileManager;
    Joystick joystick;

    public final static int gridColumnNumber = 20; // FIXED GRID SIZE. We chose 20, feels accurate enough.
    public final static String FIELD_COLOUR = "#ff905358";
    public final static String WALL_COLOUR = "#ff372a3b";
    public final static String BACKGROUND_COLOUR = "#ff372a3b";


    boolean firstOpen = true;    //To handle OnItemSelectedListener problem.. Awful, but works :/
    boolean spinnerModified = false; //To handle OnItem... when spinner is modified. Also bullshit, cba to sort

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mainLayout = (RelativeLayout) findViewById(R.id.main_layout);
        mainLayout.setBackgroundColor(Color.parseColor(BACKGROUND_COLOUR));

        joystickLayout = (RelativeLayout) findViewById(R.id.layout_joystick);
        gameGridLayout = (GridLayout) findViewById(R.id.layout_grid);

        menuLayout = (RelativeLayout) findViewById(R.id.menuLayout);
        menu_button = (ImageView) findViewById(R.id.menu_icon);

        resetButton = (Button) findViewById(R.id.resetButton);
        saveButton = (Button) findViewById(R.id.saveButton);
        deleteButton = (Button) findViewById(R.id.deleteButton);

        saveEditText = (EditText) findViewById(R.id.saveEditText);

        joystick = new Joystick(getApplicationContext(),joystickLayout,R.drawable.image_button);
        joystick.setMinDistance(80);
        joystick.setOffset(50);
        joystick.setLayoutAlpha(255);
        joystick.setStickAlpha(255);
        joystick.setStickSize(100,100);
        joystick.setLayoutSize(250,250);

        // Max columns = 30 looks good

        gameGrid = new GameGrid(getApplicationContext(),gameGridLayout,gridColumnNumber,984,1295);
        fileManager= new FileManager(gameGrid.getBlocks(),getApplicationContext());

        addItemsToSpinner(fileManager.getFileNames());

        /********************** LISTENERS FROM HERE ON ******************/

        joystickLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                joystick.drawStick(event);
                String direction = "";

                if (event.getAction() == MotionEvent.ACTION_MOVE || event.getAction() == MotionEvent.ACTION_DOWN) {
                    int directionNum = joystick.getDirection();
                    if (directionNum == 0)
                        direction = "";
                    else if (directionNum == 1)
                        direction = "Right!";
                    else if (directionNum == 2)
                        direction = "Down!";
                    else if (directionNum == 3)
                        direction = "Left!";
                    else if (directionNum == 4)
                        direction = "Up!";
                }
                return true;
            }
        });

        menu_button.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    showMenu();
                }
                return true;
            }
        });

        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gameGrid.resetGrid();
                showMenu();
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fileManager.saveFile(saveEditText.getText().toString());
                hideSaveEditTextKeyBoard();
                addItemsToSpinner(fileManager.getFileNames());
                saveEditText.setText("");
                spinnerModified = true;
            }
        });

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(fileManager.deleteFile(saveEditText.getText().toString())) {
                    hideSaveEditTextKeyBoard();
                    addItemsToSpinner(fileManager.getFileNames());
                    spinnerModified = true;
                    saveEditText.setText("");
                }
            }
        });

        fileSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(firstOpen)
                    firstOpen = false;
                else if(spinnerModified)
                    spinnerModified = false;
                else {
                    if(fileManager.readFile((String) parent.getItemAtPosition(position)))
                        showMenu();
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void showMenu(){
        if (menuLayout.getVisibility() == View.VISIBLE)
            menuLayout.setVisibility(View.GONE);
        else {
            menuLayout.setVisibility(View.VISIBLE);
            menu_button.bringToFront();
        }
    }

    private void hideSaveEditTextKeyBoard(){
        InputMethodManager inputMethodManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(saveEditText.getWindowToken(), 0);
    }

    private void addItemsToSpinner(List<String> fileNames){
        fileSpinner = (Spinner) findViewById(R.id.fileSpinner);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,R.layout.spinner_textview,fileNames);
        adapter.setDropDownViewResource(R.layout.spinner_textview);

        fileSpinner.setAdapter(adapter);
    }
}
