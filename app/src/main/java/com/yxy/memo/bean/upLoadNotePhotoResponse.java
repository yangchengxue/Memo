package com.yxy.memo.bean;

public class upLoadNotePhotoResponse {

    /**
     * photourl : http://182.92.208.230:8080/image/20191227153014Photo2020_01_06_17_10_28.png
     * photoindex : 6
     * update : 2020_01_06_17_10_28
     */

    private String photourl;
    private String photoindex;
    private String update;

    public String getPhotourl() {
        return photourl;
    }

    public void setPhotourl(String photourl) {
        this.photourl = photourl;
    }

    public String getPhotoindex() {
        return photoindex;
    }

    public void setPhotoindex(String photoindex) {
        this.photoindex = photoindex;
    }

    public String getUpdate() {
        return update;
    }

    public void setUpdate(String update) {
        this.update = update;
    }
}
