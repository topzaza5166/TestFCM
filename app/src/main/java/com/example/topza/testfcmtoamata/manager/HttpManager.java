package com.example.topza.testfcmtoamata.manager;

import com.example.topza.testfcmtoamata.dao.BoardDao;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Path;
import rx.Observable;

/**
 * Created by topza on 7/27/2017.
 */

public class HttpManager {
    private static final HttpManager ourInstance = new HttpManager();

    private ApiService service;

    public static HttpManager getInstance() {
        return ourInstance;
    }

    private HttpManager() {

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://smartcity.amata.com/api/v1/")
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();

        service = retrofit.create(ApiService.class);
    }

    public ApiService getService() {
        return service;
    }

    public interface ApiService {
        @GET("board/{user_id}")
        Observable<BoardDao> getBoard(@Header("Authorization") String authHeader, @Path("user_id") int userId);
    }

}
