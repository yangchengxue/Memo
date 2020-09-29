package com.yxy.memo.bean;

/**
  *
  * @Package:        com.yxy.memo.bean
  * @ClassName:      getUserInfoResponse
  * @CreateDate:     2019/12/25 17:34
  * @Description:    用于解析获取用于信息成功后返回的json数据
 */
public class getUserInfoResponse {


    /**
     * id : 3
     * uid : 20191222174338
     * uname : Ycx
     * uicon : /usr/java/image/20191222174338Icon2020_01_06_16_44_02.jpg
     * umobile : null
     * gender : null
     * lastlogintime : 2020-01-15 13:28:39
     */

    private int id;
    private String uid;
    private String uname;
    private String uicon;
    private String umobile;
    private String gender;
    private String lastlogintime;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getUname() {
        return uname;
    }

    public void setUname(String uname) {
        this.uname = uname;
    }

    public String getUicon() {
        return uicon;
    }

    public void setUicon(String uicon) {
        this.uicon = uicon;
    }

    public String getUmobile() {
        return umobile;
    }

    public void setUmobile(String umobile) {
        this.umobile = umobile;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getLastlogintime() {
        return lastlogintime;
    }

    public void setLastlogintime(String lastlogintime) {
        this.lastlogintime = lastlogintime;
    }
}
