package com.example.topza.testfcmtoamata.activity;

import android.app.ProgressDialog;
import android.databinding.DataBindingUtil;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.SpinnerAdapter;
import android.widget.Toast;

import com.example.topza.testfcmtoamata.manager.HttpManager;
import com.example.topza.testfcmtoamata.R;
import com.example.topza.testfcmtoamata.dao.BoardDao;
import com.example.topza.testfcmtoamata.dao.BoardResponseDao;
import com.example.topza.testfcmtoamata.databinding.ActivityMainBinding;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.Callable;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    private static final String AUTH_KEY = "key=AAAAeHhUqSU:APA91bFeajqq_ilkOhXA1RjJNd3DIj7t4CdALH4VWVkxeT_SkHhUsbQp-8t8z-g5r0-Jy3VNP-exaXn8-XHT9d_r-pnbUTfKQYRj_500Ib3TLU1YfAOEmz3BLuS9LJiC1k8u-uU-v4sh";

    ActivityMainBinding binding;

    private ArrayAdapter spinnerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        spinnerAdapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerTopic.setAdapter(spinnerAdapter);
        getBoardList();

        binding.buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final ProgressDialog dialog = ProgressDialog.show(MainActivity.this, "", "Please Wait", true, false);
                Observable.fromCallable(new Callable<String>() {
                    @Override
                    public String call() throws Exception {
                        String topic = (String) binding.spinnerTopic.getSelectedItem();
                        String title = binding.editTitle.getText().toString();
                        String message = binding.editMessage.getText().toString();
                        return pushNotification(topic, title, message);
                    }
                }).subscribeOn(Schedulers.io()).subscribe(new Action1<String>() {
                    @Override
                    public void call(String s) {
                        Toast.makeText(MainActivity.this, s, Toast.LENGTH_SHORT).show();
                        dialog.cancel();
                    }
                });
            }
        });
    }

    private void getBoardList() {
        String authHeader = "Basic " + Base64.encodeToString("admin@hotmail.com:admin".getBytes(), Base64.NO_WRAP);
        rx.Observable<BoardResponseDao> observable = HttpManager.getInstance().getService().getBoard(authHeader, Base64.NO_WRAP);
        observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io())
                .doOnNext(new Action1<BoardResponseDao>() {
                    @Override
                    public void call(BoardResponseDao boardResponseDao) {
                        spinnerAdapter.addAll(boardResponseDao.getData());
                        spinnerAdapter.notifyDataSetChanged();
                    }
                })
                .doOnError(new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Toast.makeText(MainActivity.this, "API ERROR", Toast.LENGTH_SHORT).show();
                    }
                })
                .subscribe();
    }

    private String pushNotification(String topic, String title, String body) {
        JSONObject jPayload = new JSONObject();
        JSONObject jNotification = new JSONObject();
//        JSONObject jData = new JSONObject();
        try {
            jNotification.put("title", title);
            jNotification.put("body", body);
            jNotification.put("sound", "default");
            jNotification.put("icon", "ic_launcher");
//            jNotification.put("badge", "1");
//            jNotification.put("click_action", "OPEN_ACTIVITY_1");
//            jData.put("picture_url", "http://opsbug.com/static/google-io.jpg");


//            switch(type) {
//                case "tokens":
//                    JSONArray ja = new JSONArray();
//                    ja.put("c5pBXXsuCN0:APA91bH8nLMt084KpzMrmSWRS2SnKZudyNjtFVxLRG7VFEFk_RgOm-Q5EQr_oOcLbVcCjFH6vIXIyWhST1jdhR8WMatujccY5uy1TE0hkppW_TSnSBiUsH_tRReutEgsmIMmq8fexTmL");
//                    ja.put(FirebaseInstanceId.getInstance().getToken());
//                    jPayload.put("registration_ids", ja);
//                    break;
//                case "topic":
//                    jPayload.put("to", "/topics/news");
//                    break;
//                case "condition":
//                    jPayload.put("condition", "'sport' in topics || 'news' in topics");
//                    break;
//                default:
//                    jPayload.put("to", FirebaseInstanceId.getInstance().getToken());
//            }
            jPayload.put("to", "/topics/" + topic);
            jPayload.put("priority", "high");
            jPayload.put("notification", jNotification);
//            jPayload.put("data", jData);

            URL url = new URL("https://fcm.googleapis.com/fcm/send");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", AUTH_KEY);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            // Send FCM message content.
            OutputStream outputStream = conn.getOutputStream();
            outputStream.write(jPayload.toString().getBytes());

            // Read FCM response.
            InputStream inputStream = conn.getInputStream();
            String resp = convertStreamToString(inputStream);

            return resp;

        } catch (JSONException | IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private String convertStreamToString(InputStream is) {
        Scanner s = new Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next().replace(",", ",\n") : "";
    }

}
