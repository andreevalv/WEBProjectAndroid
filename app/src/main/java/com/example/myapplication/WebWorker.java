package com.example.myapplication;

import android.content.Context;
import android.util.Log;
import android.util.Pair;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.here.oksse.OkSse;
import com.here.oksse.ServerSentEvent;
import com.star_zero.sse.EventHandler;
import com.star_zero.sse.EventSource;
import com.star_zero.sse.MessageEvent;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class WebWorker extends Worker{

    private String urlBase = "http://35.228.209.212:8080/";
    private String urlVideoBase = urlBase + "videos/";
    private String urlSseBase = urlBase + "sse/";

    public WebWorker(@NonNull Context context,
                     @NonNull WorkerParameters parameters){
        super(context, parameters);
    }

    private String getCsrf(){
        Request csrf_req = new Request.Builder()
                .url(urlBase + "get_csrf_api")
                .build();

        Response response = null;
        try {
            response = StnController.getInstance().getClient().newCall(csrf_req).execute();
            String respJsonData = response.body().string();
            String tokenVal = new JSONObject(respJsonData).getString("_csrf.token");
            return tokenVal;
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }

        return "";
    }

    private void adminSendPause(String time){
        String tokenVal = getCsrf();
        OkHttpClient client = StnController.getInstance().getClient();
        RequestBody pauseRequestBody = new FormBody.Builder()
                .add("_csrf", tokenVal)
                .build();

        Request pauseRequest = new Request.Builder()
                .url(urlSseBase + "pause/" + time)
                .post(pauseRequestBody)
                .build();

        try {
            StnController.getInstance().getClient().newCall(pauseRequest).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void adminSendPlay(String time){
        String tokenVal = getCsrf();
        RequestBody playReqBody = new FormBody.Builder()
                .add("_csrf", tokenVal)
                .build();
        Request playReq = new Request.Builder()
                .url(urlSseBase + "play/" + time)
                .post(playReqBody)
                .build();
        try {
            StnController.getInstance().getClient().newCall(playReq).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void adminSendSeek(String time){
        String tokenVal = getCsrf();
        RequestBody seekReqBody = new FormBody.Builder()
                .add("_csrf", tokenVal)
                .build();
        Request seekReq = new Request.Builder()
                .url(urlSseBase + "seek/" + time)
                .post(seekReqBody)
                .build();

        try {
            StnController.getInstance().getClient().newCall(seekReq).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void buildRoom(){
        getMeAdmin();
        getRoomName();
        updateSse();
        updateRoomVideo();
    }

    private void getRoomName() {
        Request roomNameReq = new Request.Builder()
                .url(urlBase + "what_roomname")
                .build();
        try {
            Response roomNameResp = StnController.getInstance().getClient().newCall(roomNameReq).execute();
            StnController.getInstance().setRoomName(roomNameResp.body().string());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void getMeAdmin() {
        Request meAdminRequest = new Request.Builder()
                .url(urlVideoBase + "meAdmin")
                .build();

        try {
            Response meAdminResp = StnController.getInstance().getClient().newCall(meAdminRequest).execute();
            StnController.getInstance().setMeAdmin(meAdminResp.body().string());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateSse() {
        boolean meAdmin = StnController.getInstance().getMeAdmin();
        if (meAdmin){ // not init sse if meAdmin
            if(! (StnController.getInstance().getSseClient() == null)){
                StnController.getInstance().getSseClient().close();
                StnController.getInstance().setSseClient(null);
            }
            return;
        }

        Map<String, String> header = new HashMap<>();
        header.put("Cookie", StnController.getInstance().getSessionCookie());

        EventSource eventSource = new EventSource(urlSseBase + "action_notify",
                header,
                new EventHandler() {
                    @Override
                    public void onOpen() {
                        Log.v("Tag", "onOPENED");
                    }

                    @Override
                    public void onMessage(@NonNull MessageEvent event) {
                        String eventType = event.getEvent();
                        String eventData = event.getData();
                        Pair<String, String> playerEventSet = new Pair<>(eventType, eventData);
                        StnController.getInstance().setPlayerAction(playerEventSet);
                    }

                    @Override
                    public void onError(@Nullable Exception e) {
                        Log.v("Tag", "onError");
                        StnController.getInstance().getSseClient().close();
                    }
                });

        StnController.getInstance().setSseClient(eventSource);
        eventSource.connect();
    }

    private void updateRoomVideo(){

        Request videoPathReq = new Request.Builder()
                .url(urlVideoBase + "some")
                .build();

        try {
            Response videoPathResp = StnController.getInstance().getClient().newCall(videoPathReq).execute();
            StnController.getInstance().setVideoPath(videoPathResp.body().string());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void loginWork(String username, String password){
        OkHttpClient client = StnController.getInstance().getClient();

        try {
            String tokenVal = getCsrf();
            Log.v("Tag", tokenVal);

            RequestBody login_body = new FormBody.Builder()
                    .add("username", username)
                    .add("password", password)
                    .add("_csrf", tokenVal)
                    .build();

            Request login_req = new Request.Builder()
                    .url(urlBase + "login")
                    .post(login_body)
                    .build();

            Response login_resp = client.newCall(login_req).execute();

            StnController.getInstance().setLoggedUser(username);

            buildRoom();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void changeRoomWork(String roomName){
        Log.v("Tag", "Change room");
        HttpUrl.Builder httpBuilder = HttpUrl.parse(urlBase + "join_invite").newBuilder();
        httpBuilder.addQueryParameter("join_invite", roomName);

        Request changeRoomReq = new Request.Builder()
                .url(httpBuilder.build()).build();

        OkHttpClient client = StnController.getInstance().getClient();

        try {
            Response changeRoomResp = client.newCall(changeRoomReq).execute();
            Log.v("Tag", String.valueOf(changeRoomResp.code()));
            buildRoom();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void adminPlayEventWork(String event, String time){
        switch (event){

            case "play":
                adminSendPlay(time);
                break;

            case "pause":
                adminSendPause(time);
                break;

            case "seek":
                adminSendSeek(time);
                break;
        }
    }

    private void createRoomWork() {
        HttpUrl.Builder createRoomBuilder = HttpUrl.parse(urlBase + "croom").newBuilder();
        createRoomBuilder.addQueryParameter("create_room", "");
        Request createRoomReq = new Request.Builder()
                .url(createRoomBuilder.build()).build();
        try {
            StnController.getInstance().getClient().newCall(createRoomReq).execute();
            buildRoom();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @NonNull
    @Override
    public Result doWork() {

        String action = getInputData().getString("Action");

        switch (action){
            case "login":
                String username = getInputData().getString("username");
                String password = getInputData().getString("password");
                loginWork(username, password);
                break;

            case "changeRoom":
                String roomName = getInputData().getString("changeRoom");
                changeRoomWork(roomName);
                break;

            case "playEvent":
                String event = getInputData().getString("playEvent");
                String time = getInputData().getString("time");
                adminPlayEventWork(event, time);
                break;

            case "createRoom":
                createRoomWork();
        }

        return Result.success();
    }
}