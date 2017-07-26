package com.example.topza.testfcmtoamata.dao;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

/**
 * Created by topza on 7/27/2017.
 */

public class BoardResponseDao implements Parcelable {

    private String response;
    private String message;
    private List<BoardDao> data;


    protected BoardResponseDao(Parcel in) {
        response = in.readString();
        message = in.readString();
        data = in.createTypedArrayList(BoardDao.CREATOR);
    }

    public static final Creator<BoardResponseDao> CREATOR = new Creator<BoardResponseDao>() {
        @Override
        public BoardResponseDao createFromParcel(Parcel in) {
            return new BoardResponseDao(in);
        }

        @Override
        public BoardResponseDao[] newArray(int size) {
            return new BoardResponseDao[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(response);
        dest.writeString(message);
        dest.writeTypedList(data);
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<BoardDao> getData() {
        return data;
    }

    public void setData(List<BoardDao> data) {
        this.data = data;
    }
}
