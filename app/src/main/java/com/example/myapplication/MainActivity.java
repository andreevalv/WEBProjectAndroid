package com.example.myapplication;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.exoplayer2.ExoPlayerLibraryInfo;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.upstream.HttpDataSource;

public class MainActivity extends AppCompatActivity {

    final String urlBase = "http://35.228.209.212:8080/";
    final String videoUrlBase = urlBase + "videos/";
    SimpleExoPlayer player;
    Boolean needRelogin = true;

    private void initPlayer(){

        StnController controller = StnController.getInstance();
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

        if(player != null)
            player.release();

        player = new SimpleExoPlayer.Builder(this)
                .setMediaSourceFactory(new DefaultMediaSourceFactory(dataSourceFactory))
                .build();
        PlayerView playerView = findViewById(R.id.video_view);
        playerView.setPlayer(player);

        final Player.EventListener playerListener = new Player.EventListener() {
            @Override
            public void onIsPlayingChanged(boolean isPlaying) {
                long curPos = player.getCurrentPosition();
                Log.v("Tag", String.valueOf(curPos / 1000));
                Double posInSec = Double.valueOf(curPos / 1000);
                if (isPlaying){
                    StnController.getInstance().sendPlayEvent("play", String.valueOf(posInSec));
                } else {
                    StnController.getInstance().sendPlayEvent("pause", String.valueOf(posInSec));
                }
            }
        };


        final MutableLiveData<Pair<String, String>> playerActionNotify = StnController.getInstance().getPlayerAction();

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

        MediaItem mediaItem = MediaItem.fromUri(videoUrlBase + controller.getVideoPath());
        player.setMediaItem(mediaItem);
        player.prepare();

        Log.v("Tag_VIDEO", controller.getVideoPath());

        TextView roomNameText = findViewById(R.id.roomNameTextView);
        roomNameText.setText(controller.getRoomName());

        boolean meAdmin = controller.getMeAdmin();
        player.removeListener(playerListener);
        if(meAdmin){
            player.addListener(playerListener);
        }
    }
    private void loginHandle(){
        MutableLiveData<Boolean> loggedIn = StnController.getInstance().getIsLoggedIn();
        loggedIn.observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if (aBoolean){
                    initRoom();
                } else {
                    if(needRelogin)
                        initLogin();
                }
            }
        });
    }
    private void initLogin(){
        setContentView(R.layout.login_layout);
        Button loginButton = findViewById(R.id.loginButton);
        needRelogin = false;
        loginHandle();
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText loginText = findViewById(R.id.loginText);
                EditText passordText = findViewById(R.id.passwordText);

                String username = loginText.getText().toString();
                String password = passordText.getText().toString();

                StnController.getInstance().login(username, password);
            }
        });
        Button registerButton = findViewById(R.id.registerFromLoginButton);
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                initRegister();
            }
        });
    }

    private void initRegister() {
        needRelogin = true;
        setContentView(R.layout.register_layout);
        Button registerButton = findViewById(R.id.registerButton);
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String username = ((EditText) findViewById(R.id.newUserNameText)).getText().toString();
                String password = ((EditText) findViewById(R.id.newPasswordText)).getText().toString();
                String repeatPassword = ((EditText) findViewById(R.id.repeatNewPasswordText)).getText().toString();

                StnController.getInstance().registerUser(username, password, repeatPassword);

            }
        });
    }

    private void initRoom(){
        needRelogin = true;
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        StnController controller = StnController.getInstance();

        TextView usernameTextView = findViewById(R.id.userNameTextView);
        String userRole = controller.getMeAdmin() ? " (ADMIN)" : " (USER)";

        usernameTextView.setText("Hello, " + controller.getLoggedUser() + userRole);

        initPlayer();
    }
    private void initChangeRoom() {
        ChangeRoomPopUp changeRoomPopUp = new ChangeRoomPopUp();
        changeRoomPopUp.showPopupWindow(findViewById(R.id.video_view), R.layout.change_room_popup);
    }
    private void initCreateRoom() {
        CreateRoomPopup createRoomPopup = new CreateRoomPopup();
        createRoomPopup.showPopupWindow(findViewById(R.id.video_view), R.layout.create_room_popup);
    }
    private void initSelectVideo() {
        if(StnController.getInstance().getMeAdmin()){
            SelectVideoPopup selectVideoPopup = new SelectVideoPopup();
            selectVideoPopup.showPopupWindow(findViewById(R.id.video_view), R.layout.select_video_popup);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        StnController controller = StnController.getInstance();

        super.onCreate(savedInstanceState);
        initLogin();

       /* Button joinBtn = findViewById(R.id.changeRoomButton);
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
        );*/
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.userProfile:
                initChangePassword();
                return true;

            case R.id.changeRoom:
                initChangeRoom();
                return true;

            case R.id.createRoom:
                initCreateRoom();
                return true;

            case R.id.logoutItem:
                StnController.getInstance().logout();
                return true;

            case R.id.selectVideo:
                initSelectVideo();
                return true;

            case R.id.deleteUser:
                initDeleteUser();
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    private void initChangePassword() {
        ChangePasswordPopup changePasswordPopup = new ChangePasswordPopup();
        changePasswordPopup.showPopupWindow(findViewById(R.id.video_view), R.layout.change_password_popup);
    }

    private void initDeleteUser() {
        DeleteAccountPopup deleteAccountPopup = new DeleteAccountPopup();
        deleteAccountPopup.showPopupWindow(findViewById(R.id.video_view), R.layout.delete_account_popup);
    }

}