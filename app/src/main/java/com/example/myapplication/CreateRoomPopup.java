package com.example.myapplication;

import android.view.View;
import android.widget.Button;

public class CreateRoomPopup extends CustomPopup {
    @Override
    public void initPopup() {
        super.initPopup();
        Button yesButton = popupView.findViewById(R.id.sureToCreateButton);
        Button cancelButton = popupView.findViewById(R.id.cancelCreateButton);

        yesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                StnController.getInstance().createRoom();
                dismiss();
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
    }
}
