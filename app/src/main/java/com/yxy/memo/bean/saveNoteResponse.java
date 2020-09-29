package com.yxy.memo.bean;

public class saveNoteResponse {

    /**
     * result : Succeed
     * photourl : http://182.92.208.230:8080/image/20200107144452Photo2020-01-07 14:48:49.jpg
     * code : 400
     * photoindex : 5678
     * noteid : 20200107144452
     * savetime : 2020-01-07 14:48:49
     */

    private String result;
    private String photourl;
    private String code;
    private String photoindex;
    private String noteid;
    private String savetime;

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getPhotourl() {
        return photourl;
    }

    public void setPhotourl(String photourl) {
        this.photourl = photourl;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getPhotoindex() {
        return photoindex;
    }

    public void setPhotoindex(String photoindex) {
        this.photoindex = photoindex;
    }

    public String getNoteid() {
        return noteid;
    }

    public void setNoteid(String noteid) {
        this.noteid = noteid;
    }

    public String getSavetime() {
        return savetime;
    }

    public void setSavetime(String savetime) {
        this.savetime = savetime;
    }
}
