package com.example.bixbyvoicerecorder;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class MyResponse {

    @SerializedName("result")
    @Expose    public Boolean result;

    public Boolean getResult() {
        return result;
    }

    public void setResult(Boolean result) {
        this.result = result;
    }

}
