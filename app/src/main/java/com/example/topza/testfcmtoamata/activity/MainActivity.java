package com.example.topza.testfcmtoamata.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Toast;

import com.example.topza.testfcmtoamata.dao.BoardDao;
import com.example.topza.testfcmtoamata.dao.BoardThread;
import com.example.topza.testfcmtoamata.manager.HttpManager;
import com.example.topza.testfcmtoamata.R;
import com.example.topza.testfcmtoamata.databinding.ActivityMainBinding;
import com.example.topza.testfcmtoamata.manager.NotificationManager;
import com.example.topza.testfcmtoamata.view.SpinnerView;
import com.github.oliveiradev.lib.RxPhoto;
import com.github.oliveiradev.lib.shared.TypeRequest;
import com.yalantis.ucrop.UCrop;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Callable;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import rx.subscriptions.Subscriptions;

public class MainActivity extends AppCompatActivity {
    private static final int RESPONSE_CODE_SUCCESS = 200;
    private static final String RESPONSE_MESSAGE_SUCCESS = "Success";
    private static final String RESPONSE = "response";

    // Variable

    ActivityMainBinding binding;

    private customSpinnerAdapter spinnerAdapter;
    private Uri resultUri = null;
    private Subscription subscription = Subscriptions.empty();

    /*****************
     * Functions
     *****************/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        getBoardList();

        binding.buttonSend.setOnClickListener(listener);

        binding.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Choose from...")
                        .setItems(R.array.choose_image_item, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (which == 0) {
                                    subscription = RxPhoto.requestUri(MainActivity.this, TypeRequest.CAMERA)
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .doOnNext(uriAction)
                                            .subscribe();
                                } else if (which == 1) {
                                    subscription = RxPhoto.requestUri(MainActivity.this, TypeRequest.GALLERY)
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .doOnNext(uriAction)
                                            .subscribe();
                                }
                            }
                        }).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            resultUri = UCrop.getOutput(data);
            binding.imageView.setImageURI(resultUri);
            subscription.unsubscribe();
        } else if (resultCode == UCrop.RESULT_ERROR) {
            Log.d("Crop Error", UCrop.getError(data).getMessage());
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelable("imageUri", resultUri);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        if (savedInstanceState != null){
            resultUri = savedInstanceState.getParcelable("imageUri");
            binding.imageView.setImageURI(resultUri);
        }


    }

    private void getBoardList() {
        String authHeader = "Basic " + Base64.encodeToString("admin@hotmail.com:admin".getBytes(), Base64.NO_WRAP);
        Observable<BoardDao> observable = HttpManager.getInstance().getService().getBoard(authHeader, 1);
        observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io())
                .subscribe(subscriber);
    }

    private NotificationManager.JsonBody getJsonBody() {
        NotificationManager.JsonBody jsonBody = new NotificationManager.JsonBody();
        String topic = "board_" + ((BoardThread) binding.spinnerTopic.getSelectedItem()).getTitle();
        topic.replace(" ", "_");
        jsonBody.setTopic(topic);

        String title = binding.editTitle.getText().toString();
        jsonBody.setTitle(title);

        String message = binding.editMessage.getText().toString();
        jsonBody.setText(message);

//        if (binding.imageView.getDrawable() != null) {
//            Bitmap bitmap = ((BitmapDrawable) binding.imageView.getDrawable()).getBitmap();
//            ByteArrayOutputStream baos = new ByteArrayOutputStream();
//            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
//            byte[] imageInByte = baos.toByteArray();
//            jsonBody.setData(imageInByte);
//        }

        if (resultUri != null) {
            File file = new File(resultUri.getPath());
            jsonBody.setData(file);
        }

        return jsonBody;
    }

    /********************
     * Listener Zone
     ********************/

    Action1<Uri> uriAction = new Action1<Uri>() {
        @Override
        public void call(Uri uri) {
            SimpleDateFormat sdf = new SimpleDateFormat("HH_mm_ss");
            String currentDateandTime = sdf.format(new Date());
            UCrop.of(uri, Uri.fromFile(new File(getCacheDir(), currentDateandTime + ".jpg")))
                    .withAspectRatio(1, 1)
                    .start(MainActivity.this);
        }
    };

    Subscriber<BoardDao> subscriber = new Subscriber<BoardDao>() {
        @Override
        public void onCompleted() {
            binding.spinnerTopic.setAdapter(spinnerAdapter);
            Log.d(RESPONSE, "Load Spinner Complete");
        }

        @Override
        public void onError(Throwable e) {
            Toast.makeText(MainActivity.this, "Load Fail", Toast.LENGTH_SHORT).show();
            Log.d(RESPONSE, e.getMessage());
        }

        @Override
        public void onNext(BoardDao boardDao) {
            if (boardDao.getResponse() == RESPONSE_CODE_SUCCESS && boardDao.getMessage().equals(RESPONSE_MESSAGE_SUCCESS)) {
                if (boardDao.getData().getBoardThreads() != null) {
                    spinnerAdapter = new customSpinnerAdapter(boardDao);
                    Log.d(RESPONSE, "Board is not Null");
                }
                Log.d(RESPONSE, "Connection Complete");
            } else Log.d(RESPONSE, boardDao.getResponse() + boardDao.getMessage());
        }
    };

    View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            final ProgressDialog dialog = ProgressDialog.show(MainActivity.this, "", "Please Wait", true, false);
            Observable.fromCallable(new Callable<String>() {
                @Override
                public String call() throws Exception {
                    return NotificationManager.pushNotification(getJsonBody());
                }
            }).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .unsubscribeOn(Schedulers.io())
                    .subscribe(new Subscriber<String>() {
                        @Override
                        public void onCompleted() {

                        }

                        @Override
                        public void onError(Throwable e) {
                            e.printStackTrace();
                            dialog.cancel();
                            Log.d("Send Notification Error", e.getMessage());
                        }

                        @Override
                        public void onNext(String s) {
                            Toast.makeText(MainActivity.this, s, Toast.LENGTH_SHORT).show();
                            dialog.cancel();
                        }
                    });
        }
    };

    /*****************
     * Inner Class
     *****************/

    class customSpinnerAdapter extends BaseAdapter {

        BoardDao boardDao;

        public customSpinnerAdapter(BoardDao boardDao) {
            this.boardDao = boardDao;
        }

        @Override
        public int getCount() {
            if (boardDao == null)
                return 0;
            if (boardDao.getData() == null)
                return 0;
            if (boardDao.getData().getBoardThreads() == null)
                return 0;
            return boardDao.getData().getBoardThreads().size();
        }

        @Override
        public Object getItem(int position) {
            return boardDao.getData().getBoardThreads().get(position);
        }

        @Override
        public long getItemId(int position) {
            return boardDao.getData().getBoardThreads().get(position).getId();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            SpinnerView view;
            if (convertView == null) {
                view = new SpinnerView(parent.getContext());
            } else view = (SpinnerView) convertView;

            BoardThread boardThread = (BoardThread) getItem(position);
            view.setText(boardThread.getTitle());
            view.loadImage(boardThread.getImage());
            view.setDetail(boardThread.getDescription());

            return view;
        }
    }

}
