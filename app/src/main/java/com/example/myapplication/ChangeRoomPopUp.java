package com.example.myapplication;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class ChangeRoomPopUp extends CustomPopup {
    @Override
    public void initPopup() {
        super.initPopup();

        Button changeRoomBtn = popupView.findViewById(R.id.changeRoomButton);

        changeRoomBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText newRoomText = popupView.findViewById(R.id.newRoomText);
                String newRoomID = newRoomText.getText().toString();
                StnController.getInstance().changeRoom(newRoomID);
                dismiss();
            }
        });
    }
}
