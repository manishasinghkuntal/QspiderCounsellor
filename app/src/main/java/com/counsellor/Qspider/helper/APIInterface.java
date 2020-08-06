package com.counsellor.Qspider.helper;



import com.counsellor.Qspider.Login.model.LoginRes;
import com.counsellor.Qspider.home.RecordingData;
import com.google.gson.JsonObject;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface APIInterface {

    @POST("/auth/token/login/")
    Call<LoginRes> createUser(@Body JsonObject user);

    @Headers("Authorization: Token 94735eb6a2ce7a7f23786d7f237aad6d3b60b72e")
    @Multipart
    @POST("/callrecord/call_record/")
    Call<RecordingData> PostRecording(@Part("number") RequestBody Number,
                                      @Part("time") RequestBody time,
                                      @Part("duration") RequestBody duration,
                                      @Part("call_type") RequestBody callType,
                                      @Part("main_status") RequestBody mainStatus,
                                      @Part("sub_status") RequestBody subStatus,
                                      @Part("comment") RequestBody comment,
                                      @Part("audio\"; filename=\"audio.mp4")
                                              RequestBody file);

}