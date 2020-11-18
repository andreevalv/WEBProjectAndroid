package com.example.myapplication;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class ChangePasswordPopup extends CustomPopup{
    @Override
    public void initPopup() {
        super.initPopup();

        Button submitPasswordChangeButton = popupView.findViewById(R.id.submitChangePassword);
        submitPasswordChangeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String oldPassword = ((EditText) popupView.findViewById(R.id.oldPasswordText)).getText().toString();
                String newPassword =  ((EditText) popupView.findViewById(R.id.newPasswordText)).getText().toString();
                String repeatNewPassword = ((EditText) popupView.findViewById(R.id.repitNewPasswordText)).getText().toString();
                StnController.getInstance().changePassword(oldPassword, newPassword, repeatNewPassword);
                dismiss();
            }
        });
    }
}
