package com.example.topza.testfcmtoamata.dao;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by topza on 7/27/2017.
 */

public class BoardDao implements Parcelable {


    protected BoardDao(Parcel in) {
    }

    public static final Creator<BoardDao> CREATOR = new Creator<BoardDao>() {
        @Override
        public BoardDao createFromParcel(Parcel in) {
            return new BoardDao(in);
        }

        @Override
        public BoardDao[] newArray(int size) {
            return new BoardDao[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
    }
}
