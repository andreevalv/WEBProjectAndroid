package com.example.myapplication;

import android.view.View;
import android.widget.Button;

public class DeleteAccountPopup extends CustomPopup {
    @Override
    public void initPopup() {
        super.initPopup();

        Button okTodeleteButton = popupView.findViewById(R.id.yesDeleteButton);
        Button cancelDeleteButton = popupView.findViewById(R.id.cancelDelteButton);

        cancelDeleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        okTodeleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                StnController.getInstance().deleteAccount();
                dismiss();
            }
        });
    }
}
