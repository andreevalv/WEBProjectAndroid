package com.example.myapplication;

import android.util.Log;
import android.util.Pair;

import androidx.annotation.VisibleForTesting;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

import com.here.oksse.OkSse;
import com.here.oksse.ServerSentEvent;
import com.star_zero.sse.EventSource;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class StnController {

    private static final StnController instance = new StnController();
    private OkHttpClient client = new OkHttpClient().newBuilder()
            .cookieJar(
                    new CookieJar() {

                        private final HashMap<String, List<Cookie>> cookieStore = new HashMap<>();

                        @Override
                        public void saveFromResponse(@NotNull HttpUrl httpUrl, @NotNull List<Cookie> list) {
                            cookieStore.put(httpUrl.host(), list);
                            String session_cookie = list.get(0).toString().split(";")[0];
                            Log.v("TAG:COOKIE", session_cookie);
                            StnController.getInstance().setSessionCookie(session_cookie);

                        }

                        @NotNull
                        @Override
                        public List<Cookie> loadForRequest(@NotNull HttpUrl httpUrl) {
                            List<Cookie> cookies = cookieStore.get(httpUrl.host());
                            return cookies != null ? cookies : new ArrayList<Cookie>();
                        }
                    }
            ).build();

    private EventSource sseClient = null;
    private ServerSentEvent cliNotifyEvent;
    private String roomName;

    private StnController(){};
    public static StnController getInstance(){
        return instance;
    }

    public OkHttpClient getClient(){
        return client;
    }
    public EventSource getSseClient(){return sseClient;}
    public void setSseClient(EventSource setClient){sseClient = setClient;}

    private MutableLiveData<String> videoPath = new MutableLiveData<>();
    private MutableLiveData<Pair<String, String>> playerAction = new MutableLiveData<>();
    private String loggedUser;
    private String sessionCookie;
    private boolean meAdmin;

    public void setVideoPath(String val){
        videoPath.postValue(val);
    }

    public MutableLiveData<String> getVideoPath(){
        return videoPath;
    }

    public void setSessionCookie(String val){
        sessionCookie = val;
    }

    public String getSessionCookie(){
        return sessionCookie;
    }

    public void setSse(ServerSentEvent sseSet){cliNotifyEvent = sseSet;}
    public ServerSentEvent getCliNotifyEvent(){return cliNotifyEvent;}

    public void setPlayerAction(Pair<String, String> val){playerAction.postValue(val);}

    public void login(String username, String password){
        Data loginData = new Data.Builder()
                .putString("Action", "login")
                .putString("username", username)
                .putString("password", password)
                .build();

        WorkRequest WebLoginReq = new OneTimeWorkRequest.Builder(WebWorker.class)
                .setInputData(loginData)
                .build();

        WorkManager.getInstance().enqueue(WebLoginReq);
    }

    public MutableLiveData<Pair<String, String>> getPlayerAction() {
        return playerAction;
    }

    public void setLoggedUser(String username) {
        loggedUser = username;
    }

    public String getLoggedUser() {
        return loggedUser;
    }

    public void changeRoom(String roomName) {
        Data changeRoomData = new Data.Builder()
                .putString("Action", "changeRoom")
                .putString("changeRoom", roomName)
                .build();

        WorkRequest changeRoomRequest = new OneTimeWorkRequest.Builder(WebWorker.class)
                .setInputData(changeRoomData)
                .build();
        WorkManager.getInstance().enqueue(changeRoomRequest);
    }

    public void sendPlayEvent(String event, String time){
        Data playEvent = new Data.Builder()
                .putString("Action", "playEvent")
                .putString("playEvent", event)
                .putString("time", time)
                .build();

        WorkRequest sendPlayEventReq = new OneTimeWorkRequest.Builder(WebWorker.class)
                .setInputData(playEvent)
                .build();
        WorkManager.getInstance().enqueue(sendPlayEventReq);
    }

    public void setMeAdmin(String string) {
        meAdmin = string.equals("true");
    }
    public boolean getMeAdmin(){
        return meAdmin;
    }

    public void createRoom() {
        Data createRoomData = new Data.Builder()
                .putString("Action", "createRoom")
                .build();

        WorkRequest createRoomReq = new OneTimeWorkRequest.Builder(WebWorker.class)
                .setInputData(createRoomData)
                .build();
        WorkManager.getInstance().enqueue(createRoomReq);
    }

    public void setRoomName(String string) {
        roomName = string;
    }

    public String getRoomName(){
        return roomName;
    }
}
