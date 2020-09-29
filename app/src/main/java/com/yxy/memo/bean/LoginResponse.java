package com.yxy.memo.bean;

/**
  *
  * @Package:        com.yxy.memo.bean
  * @ClassName:      LoginResponse
  * @CreateDate:     2019/12/25 17:18
  * @Description:    用于解析登录成功返回的json数据
 */
public class LoginResponse {

    /**
     * msg : 登录成功
     * uid : 20191222174338
     * uname : Ycx
     * token : eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJCZWFyZXI6MjAxOTEyMjIxNzQzMzgiLCJpYXQiOjE1NzcyNjUyMzEsImV4cCI6MTU3NzI2NTgzMX0.CMCuiLJD6S3H4FGUV0JKlYYbmCtVi6z7viAnrJwnbAw
     */

    private String msg;
    private String uid;
    private String uname;
    private String token;

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
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

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
