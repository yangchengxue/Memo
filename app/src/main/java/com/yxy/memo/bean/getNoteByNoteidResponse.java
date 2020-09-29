package com.yxy.memo.bean;

public class getNoteByNoteidResponse {

    /**
     * id : 26
     * noteid : 20191227151746
     * uid : 20191222174338
     * tag : 生活
     * content : 12.26日晚六点零
     * photourl : http://182.92.208.230:8080/image/20191227151746Photo2020_01_19_00_10_04.jpg
     * photolocalurl : /storage/emulated/0/DCIM/Camera/IMG_20191003_201711.jpg
     * photoindex : 12
     * textSize : 15
     * textColor : -16777216
     * alarmtime :
     * savetime : 2020-01-19 18:13:45
     */

    private int id;
    private String noteid;
    private String uid;
    private String atTheTop;
    private String tag;
    private String content;
    private String photourl;
    private String photolocalurl;
    private String photoindex;
    private String textSize;
    private String textColor;
    private String alarmtime;
    private String savetime;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNoteid() {
        return noteid;
    }

    public void setNoteid(String noteid) {
        this.noteid = noteid;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }


    public String getAtTheTop() {
        return atTheTop;
    }

    public void setAtTheTop(String atTheTop) {
        this.atTheTop = atTheTop;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getPhotourl() {
        return photourl;
    }

    public void setPhotourl(String photourl) {
        this.photourl = photourl;
    }

    public String getPhotolocalurl() {
        return photolocalurl;
    }

    public void setPhotolocalurl(String photolocalurl) {
        this.photolocalurl = photolocalurl;
    }

    public String getPhotoindex() {
        return photoindex;
    }

    public void setPhotoindex(String photoindex) {
        this.photoindex = photoindex;
    }

    public String getTextSize() {
        return textSize;
    }

    public void setTextSize(String textSize) {
        this.textSize = textSize;
    }

    public String getTextColor() {
        return textColor;
    }

    public void setTextColor(String textColor) {
        this.textColor = textColor;
    }

    public String getAlarmtime() {
        return alarmtime;
    }

    public void setAlarmtime(String alarmtime) {
        this.alarmtime = alarmtime;
    }

    public String getSavetime() {
        return savetime;
    }

    public void setSavetime(String savetime) {
        this.savetime = savetime;
    }
}
