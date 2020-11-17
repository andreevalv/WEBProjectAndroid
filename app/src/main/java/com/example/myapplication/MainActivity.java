package com.example.myapplication;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.VideoView;

import com.google.android.exoplayer2.ExoPlayerLibraryInfo;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.upstream.HttpDataSource;

import org.w3c.dom.Text;

import java.util.EventListener;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Headers;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        String urlBase = "http://35.228.209.212:8080/";
        final String videoUrlBase = urlBase + "videos/";
        StnController controller = StnController.getInstance();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Build a HttpDataSource.Factory with cross-protocol redirects enabled.
        HttpDataSource.Factory httpDataSourceFactory =
                new DefaultHttpDataSourceFactory(
                        ExoPlayerLibraryInfo.DEFAULT_USER_AGENT,
                        DefaultHttpDataSource.DEFAULT_CONNECT_TIMEOUT_MILLIS,
                        DefaultHttpDataSource.DEFAULT_READ_TIMEOUT_MILLIS,
                        /* allowCrossProtocolRedirects= */ true);

        DataSource.Factory dataSourceFactory = () -> {
            HttpDataSource dataSource = httpDataSourceFactory.createDataSource();
            // Set a custom authentication request header.
            dataSource.setRequestProperty("Cookie", "SESSION=123456");
            return dataSource;
        };

        SimpleExoPlayer player = new SimpleExoPlayer.Builder(this)
                .setMediaSourceFactory(new DefaultMediaSourceFactory(dataSourceFactory))
                .build();
        PlayerView playerView = findViewById(R.id.video_view);
        playerView.setPlayer(player);

        final MutableLiveData<String> videoPath = controller.getVideoPath();
        final MutableLiveData<Pair<String, String>> playerActionNotify = controller.getPlayerAction();
        final Player.EventListener playerListener = new Player.EventListener() {
            @Override
            public void onIsPlayingChanged(boolean isPlaying) {
                Log.v("Tag", String.valueOf(isPlaying));
                long curPos = player.getCurrentPosition();
                Double posInSec = Double.valueOf(curPos / 1000);
                if (isPlaying){
                    StnController.getInstance().sendPlayEvent("play", String.valueOf(posInSec));
                } else {
                    StnController.getInstance().sendPlayEvent("pause", String.valueOf(posInSec));
                }
            }
        };

        playerActionNotify.observe(this,
                new Observer<Pair<String, String>>() {
                    @Override
                    public void onChanged(Pair<String, String> stringStringPair) {
                        String action = stringStringPair.first;
                        String data = stringStringPair.second;
                        Long position = (long) Double.parseDouble(data) * 1000;
                        Log.v(action, data);
                        Log.v(action, String.valueOf(position));

                        switch (action){
                            case "played":
                                player.seekTo(position);
                                player.play();
                                break;

                            case "paused":
                                player.pause();
                                player.seekTo(position);
                                break;

                            case "seeked":
                                player.seekTo(position);
                                break;
                        }
                    }
                });

        videoPath.observe(this, new Observer<String>() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onChanged(String s) {

                TextView usernameTextView = findViewById(R.id.userNameTextView);
                usernameTextView.setText("Hello, " + controller.getLoggedUser());

                MediaItem mediaItem = MediaItem.fromUri(videoUrlBase + s);
                player.setMediaItem(mediaItem);
                player.prepare();

                Log.v("Tag_VIDEO", s);

                TextView roomNameText = findViewById(R.id.roomNameTextView);
                roomNameText.setText(controller.getRoomName());

                boolean meAdmin = controller.getMeAdmin();
                player.removeListener(playerListener);
                if(meAdmin){

                    player.addListener(playerListener);
                }
            }
        });

        controller.login("q", "q");

        Button joinBtn = findViewById(R.id.changeRoomButton);
        joinBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText roomNameEditText = findViewById(R.id.roomNameText);
                String roomName = roomNameEditText.getText().toString();
                Log.v("Tag", roomName);
                controller.changeRoom(roomName);
            }
        });

        Button createRoomBtn = findViewById(R.id.createRoomBtn);
        createRoomBtn.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        controller.createRoom();
                    }
                }
        );
    }
}