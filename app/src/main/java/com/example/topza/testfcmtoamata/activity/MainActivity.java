package com.example.topza.testfcmtoamata.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.support.annotation.NonNull;
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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.yalantis.ucrop.UCrop;

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
    private static final String TAG = "main_activity";

    // Variable

    ActivityMainBinding binding;

    private customSpinnerAdapter spinnerAdapter;
    private Uri resultUri = null;
    private Uri downloadUrl = null;
    private Subscription subscription = Subscriptions.empty();
    private UploadTask uploadTask;
    private ProgressDialog dialog;
    private Observable<String> notificationObservable = Observable.empty();

    /*****************
     * Functions
     *****************/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        getBoardList();

        binding.buttonSend.setOnClickListener(listener);
        binding.imageView.setOnClickListener(chooseImage);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == UCrop.REQUEST_CROP) {
            if (resultCode == RESULT_OK) {
                resultUri = UCrop.getOutput(data);
                binding.imageView.setImageURI(resultUri);
            } else if (resultCode == UCrop.RESULT_ERROR) {
                Log.d(TAG, "Crop Error : " + UCrop.getError(data).getMessage());
            }
        }

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if (subscription != null && !subscription.isUnsubscribed())
            subscription.unsubscribe();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelable("imageUri", resultUri);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        if (savedInstanceState != null) {
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
                .subscribe(new Subscriber<BoardDao>() {
                    @Override
                    public void onCompleted() {
                        binding.spinnerTopic.setAdapter(spinnerAdapter);
                        Log.d(TAG, "Load Spinner Complete");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Toast.makeText(MainActivity.this, "Load Fail", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "BoardDao error" + e.getMessage());
                    }

                    @Override
                    public void onNext(BoardDao boardDao) {
                        if (boardDao.getResponse() == RESPONSE_CODE_SUCCESS && boardDao.getMessage().equals(RESPONSE_MESSAGE_SUCCESS)) {
                            if (boardDao.getData().getBoardThreads() != null) {
                                spinnerAdapter = new customSpinnerAdapter(boardDao);
                                Log.d(TAG, "Board is not Null");
                            }
                            Log.d(TAG, "Connection Complete");
                        } else Log.d(TAG, boardDao.getResponse() + boardDao.getMessage());
                    }
                });
    }

    private NotificationManager.JsonBody getJsonBody() {
        NotificationManager.JsonBody jsonBody = new NotificationManager.JsonBody();
        BoardThread board = (BoardThread) binding.spinnerTopic.getSelectedItem();
        String topic = "board_" + board.getTitle().replace(" ", "_").toLowerCase();
        Log.d(TAG, "Topic is " + topic);
        jsonBody.setTopic(topic);

        String title = binding.editTitle.getText().toString();
        jsonBody.setTitle(title);

        String message = binding.editMessage.getText().toString();
        jsonBody.setText(message);

        if (downloadUrl != null)
            jsonBody.setData(downloadUrl.toString());

        return jsonBody;
    }

    private void uploadImage() {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference().child("image/" + resultUri.getLastPathSegment());
        uploadTask = storageRef.putFile(resultUri);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                dialog.cancel();
                Log.d(TAG, "Upload Error : " + e.getMessage());
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                downloadUrl = taskSnapshot.getDownloadUrl();
                notificationObservable.subscribe();
                Log.d(TAG, "Upload Success Uri : " + downloadUrl.toString());
            }
        });
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
                    .withMaxResultSize(360, 360)
                    .start(MainActivity.this);
        }
    };

    View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            dialog = ProgressDialog.show(MainActivity.this, "", "Please Wait", true, false);
            notificationObservable = Observable.fromCallable(new Callable<String>() {
                @Override
                public String call() throws Exception {
                    return NotificationManager.pushNotification(getJsonBody());
                }
            }).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .unsubscribeOn(Schedulers.io())
                    .doOnNext(new Action1<String>() {
                        @Override
                        public void call(String s) {
                            Toast.makeText(MainActivity.this, s, Toast.LENGTH_SHORT).show();
                            dialog.cancel();
                            Log.d(TAG, "Send Notification Completed");
                        }
                    }).doOnError(new Action1<Throwable>() {
                        @Override
                        public void call(Throwable throwable) {
                            dialog.cancel();
                            Log.d(TAG, "Send Notification Error" + throwable.getMessage());
                        }
                    });

            if (resultUri != null)
                uploadImage();
            else notificationObservable.subscribe();
        }
    };

    View.OnClickListener chooseImage = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Choose from...")
                    .setItems(R.array.choose_image_item, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            subscription.unsubscribe();
                            subscription = RxPhoto.requestUri(MainActivity.this,
                                    which == 0 ? TypeRequest.CAMERA : TypeRequest.GALLERY)
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .doOnNext(uriAction)
                                    .subscribe();
                        }
                    }).show();
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
