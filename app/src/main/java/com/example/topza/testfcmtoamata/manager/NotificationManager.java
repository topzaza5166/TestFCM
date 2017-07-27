package com.example.topza.testfcmtoamata.manager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

/**
 * Created by topza on 7/27/2017.
 */

public class NotificationManager {

    private static final String AUTH_KEY = "key=AAAAeHhUqSU:APA91bFeajqq_ilkOhXA1RjJNd3DIj7t4CdALH4VWVkxeT_SkHhUsbQp-8t8z-g5r0-Jy3VNP-exaXn8-XHT9d_r-pnbUTfKQYRj_500Ib3TLU1YfAOEmz3BLuS9LJiC1k8u-uU-v4sh";

    public static String pushNotification(JsonBody jsonBody) {
        JSONObject jPayload = new JSONObject();
        JSONObject jNotification = new JSONObject();
        JSONObject jData = new JSONObject();

        try {
            jNotification.put("title", jsonBody.getTitle());
            jNotification.put("body", jsonBody.getText());
            jNotification.put("sound", "default");
            jNotification.put("icon", "ic_launcher");
            jData.put("picture", jsonBody.getData());

//            jNotification.put("badge", "1");
//            jNotification.put("click_action", "OPEN_ACTIVITY_1");

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

            jPayload.put("to", "/topics/" + jsonBody.getTopic());
            jPayload.put("priority", "high");
            jPayload.put("notification", jNotification);
            jPayload.put("data", jData);

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
            return e.getMessage();
        }
    }

    private static String convertStreamToString(InputStream is) {
        Scanner s = new Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next().replace(",", ",\n") : "";
    }

    public static class JsonBody {
        private String topic;
        private String title;
        private String text;
        private File data;

        public JsonBody() {
        }

        public String getTopic() {
            return topic;
        }

        public void setTopic(String topic) {
            this.topic = topic;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public File getData() {
            return data;
        }

        public void setData(File data) {
            this.data = data;
        }
    }
}
