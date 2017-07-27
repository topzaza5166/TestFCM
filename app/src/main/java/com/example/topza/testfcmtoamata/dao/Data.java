
package com.example.topza.testfcmtoamata.dao;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Data {

    @SerializedName("boardThreads")
    @Expose
    private List<BoardThread> boardThreads = null;

    public List<BoardThread> getBoardThreads() {
        return boardThreads;
    }

    public void setBoardThreads(List<BoardThread> boardThreads) {
        this.boardThreads = boardThreads;
    }

}
