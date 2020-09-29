package com.yxy.memo.bean;

/**
  *
  * @Package:        com.yxy.memo.bean
  * @ClassName:      ContentEntry
  * @CreateDate:     2019/12/19 19:58
  * @Description:    JavaBean类，备忘录的每一个条目的数据
 */
public class ContentEntry {
    private String content; //内容
    private String tag;  //内容所属标签
    private String date; //内容创建的时间
    private String noteid;//每个笔记的唯一标识
    private String notePhotoUrl;//每个笔记的图片的url
    private String noteAlarmTime;//每个笔记的提醒时间
    private String atTheTop;//是否置顶

    //构造方法
    public ContentEntry(String content, String tag, String date, String noteid, String notePhotoUrl,String noteAlarmTime,String atTheTop){
        this.content = content;
        this.tag = tag;
        this.date = date;
        this.noteid = noteid;
        this.notePhotoUrl = notePhotoUrl;
        this.noteAlarmTime = noteAlarmTime;
        this.atTheTop = atTheTop;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getNoteid() {
        return noteid;
    }

    public void setNoteid(String noteid) {
        this.noteid = noteid;
    }

    public String getNotePhotoUrl() {
        return notePhotoUrl;
    }

    public void setNotePhotoUrl(String notePhotoUrl) {
        this.notePhotoUrl = notePhotoUrl;
    }


    public String getNoteAlarmTime() {
        return noteAlarmTime;
    }

    public void setNoteAlarmTime(String noteAlarmTime) {
        this.noteAlarmTime = noteAlarmTime;
    }


    public String getAtTheTop() {
        return atTheTop;
    }

    public void setAtTheTop(String atTheTop) {
        this.atTheTop = atTheTop;
    }
}
