package com.yxy.memo.httpservice;

import com.yxy.memo.bean.LoginResponse;
import com.yxy.memo.bean.getNoteByNoteidResponse;
import com.yxy.memo.bean.getNotesResponse;
import com.yxy.memo.bean.getUserInfoResponse;
import com.yxy.memo.bean.getUserNoteAllTagsResponse;
import com.yxy.memo.bean.saveNoteResponse;
import com.yxy.memo.bean.upLoadNotePhotoResponse;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;
import rx.Observable;

public interface retrofitRequest {
    //用户注册
    @FormUrlEncoded
    @POST("yxy/register")
    Observable<String> RegisterUser(
            @Field("uname") String name,
            @Field("upassword") String password
    );

    //用户登录
    @FormUrlEncoded
    @POST("yxy/login")
    Observable<LoginResponse> LoginUser(
            @Field("uname") String name,
            @Field("upassword") String password
    );

    //获取当前用户的所有信息
    @GET("yxy/getUserAllInfo")
    Observable<getUserInfoResponse> getUserAllInfo(
            @Header("Authorization") String authorization
    );

    //修改用户的信息
    @FormUrlEncoded
    @POST("yxy/UpdateUserInfo")
    Observable<String> UpdateUserInfo(
            @Header("Authorization") String authorization,
            @Field("uname") String uname,
            @Field("umobile") String umobile,
            @Field("gender") String gender
    );

    //删除用户的一个笔记
    @FormUrlEncoded
    @POST("yxy/deleteNote")
    Observable<String> deleteNote(
            @Header("Authorization") String authorization,
            @Field("noteid") String noteid
    );

    //获取当前用户的所有笔记
    @GET("yxy/getUserAllNotes")
    Observable<getNotesResponse> getUserAllNotes(
            @Header("Authorization") String authorization
    );

    //获取当前用户某标签的所有笔记
    @GET("yxy/getUserNotesByTag")
    Observable<getNotesResponse> getUserNotesByTag(
            @Header("Authorization") String authorization,
            @Query("tag") String tag
    );

    //获取某笔记的所有内容,根据笔记的noteid
    @GET("yxy/getNoteInfo")
    Observable<getNoteByNoteidResponse> getNoteInfo(
            @Header("Authorization") String authorization,
            @Query("noteid") String noteid
    );

    //GET，查询当前用户所有的标签
    @GET("yxy/getUserNoteAllTags")
    Observable<getUserNoteAllTagsResponse> getUserNoteAllTags(
            @Header("Authorization") String authorization
    );

    //上传用户的头像
    @Multipart
    @POST("yxy/uploadUserIcon")
    Observable<String> uploadUserIcon(
            @Header("Authorization") String authorization,
            @Part MultipartBody.Part userIcon
    );


    /**上传用户的笔记(当笔记内容不包含图片的时候才调用)**/
    @FormUrlEncoded
    @POST("yxy/saveNoteWithNoPhoto")
    Observable<saveNoteResponse> saveNoteWithNoPhoto(
            @Header("Authorization") String authorization,
            @Field("tag") String tag,
            @Field("content") String content,
            @Field("textSize") String textSize,
            @Field("textColor") String textColor,
            @Field("alarmtime") String alarmtime,
            @Field("noteid") String noteid,
            @Field("photoindex") String photoindex
    );

    //上传用户的笔记(当笔记内容包含图片的时候才调用)
    @Multipart
    @POST("yxy/saveNoteWithPhoto")
    Observable<saveNoteResponse> saveNoteWithPhoto(
            @Header("Authorization") String authorization,
            @Part("tag") RequestBody tag,
            @Part("content") RequestBody content,
            @Part("textSize") RequestBody textSize,
            @Part("textColor") RequestBody textColor,
            @Part("noteid") RequestBody noteid,
            @Part("photoindex") RequestBody photoindex,
            @Part("photolocalurl") RequestBody photolocalurl,
            @Part("alarmtime") RequestBody alarmtime,
            @Part MultipartBody.Part notePhoto
    );

    //设置笔记置顶
    @FormUrlEncoded
    @POST("yxy/setNoteAtTheTop")
    Observable<String> setNoteAtTheTop(
            @Header("Authorization") String authorization,
            @Field("noteid") String noteid,
            @Field("atTheTop") String atTheTop
    );

    //提交用户的反馈留言
    @FormUrlEncoded
    @POST("yxy/feedback")
    Observable<String> postFeedBack(
            @Header("Authorization") String authorization,
            @Field("content") String content,
            @Field("type") String type
    );
}
