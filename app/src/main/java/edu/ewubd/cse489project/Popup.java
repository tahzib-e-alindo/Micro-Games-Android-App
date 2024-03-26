package edu.ewubd.cse489project;

import android.graphics.drawable.ColorDrawable;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.TextView;

public class Popup {
    PopupWindow popupWindow;
    View decorView;
    protected Popup(View popupView, int width, int height, View decorView, ColorDrawable backgroundColor, String displayText, int textColor) {
        // Creating popupView
        popupWindow = new PopupWindow(popupView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        this.decorView = decorView;

        // Setting popup size
        popupWindow.setWidth(width);
        popupWindow.setHeight(height);

        popupWindow.setFocusable(true);
        popupWindow.setOutsideTouchable(true);

        // Setting background color
        popupWindow.setBackgroundDrawable(backgroundColor);

        // Setting popup text
        TextView tvWinner = popupView.findViewById(R.id.tvWinner);
        tvWinner.setTextColor(textColor);
        tvWinner.setText(displayText);

        // Touching popup dismisses
        popupView.findViewById(R.id.rlPopup).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                popupWindow.dismiss();
                return true;
            }
        });
    }
    protected void displayPopup() {
        popupWindow.showAtLocation(decorView, Gravity.CENTER, 0, 0);
    }
}
