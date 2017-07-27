
package com.example.topza.testfcmtoamata.dao;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class BoardThread {

    @SerializedName("id")
    @Expose
    private Integer id;
    @SerializedName("title")
    @Expose
    private String title;
    @SerializedName("title_th")
    @Expose
    private String titleTh;
    @SerializedName("description")
    @Expose
    private String description;
    @SerializedName("description_th")
    @Expose
    private String descriptionTh;
    @SerializedName("image")
    @Expose
    private String image;
    @SerializedName("allowedPosting")
    @Expose
    private Integer allowedPosting;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitleTh() {
        return titleTh;
    }

    public void setTitleTh(String titleTh) {
        this.titleTh = titleTh;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescriptionTh() {
        return descriptionTh;
    }

    public void setDescriptionTh(String descriptionTh) {
        this.descriptionTh = descriptionTh;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public Integer getAllowedPosting() {
        return allowedPosting;
    }

    public void setAllowedPosting(Integer allowedPosting) {
        this.allowedPosting = allowedPosting;
    }

}
