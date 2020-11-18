package com.example.myapplication;

import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class SelectVideoPopup extends CustomPopup {
    @Override
    public void initPopup() {
        super.initPopup();

        ListView videosListView = popupView.findViewById(R.id.videosList);
        ArrayAdapter<String> listAdapter = new ArrayAdapter<>(popupView.getContext(), android.R.layout.simple_list_item_1, StnController.getInstance().getVideoList());
        videosListView.setAdapter(listAdapter);

        videosListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                StnController.getInstance().setNewVideo(((TextView)view).getText().toString());
                dismiss();
            }
        });

    }
}
