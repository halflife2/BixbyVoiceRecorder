package com.example.bixbyvoicerecorder;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface Api {

    @Multipart
    @Headers({"Email:test@test.com"})
    @POST("/files/upload/fromapp")
    Call<MyResponse> upLoadFile(
            @Part MultipartBody.Part file
    );
}