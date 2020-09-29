package com.yxy.memo.bean;

import java.util.List;

public class getNotesResponse {

    private List<ResponseBean> response;

    public List<ResponseBean> getResponse() {
        return response;
    }

    public void setResponse(List<ResponseBean> response) {
        this.response = response;
    }

    public static class ResponseBean {
        /**
         * id : 81
         * noteid : 20200120183649
         * uid : 20191222174338
         * atTheTop : no
         * tag : 工作
         * content : 插头<photo>
         * photourl : http://182.92.208.230:8080/image/20200120183649Photo2020_01_20_18_36_49.jpg
         * photolocalurl : /storage/emulated/0/Pictures/1579516606249.jpg
         * photoindex : 2
         * textSize : 15
         * textColor : -16777216
         * alarmtime : 2020-01-20 20:18
         * savetime : 2020-01-20 20:17:42
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
}
