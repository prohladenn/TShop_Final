package com.example.tshop.t_shop;


import java.util.Map;

import retrofit2.Call;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.QueryMap;

public interface Api {
    @GET("/payment/rest/register.do")
    Call<Result> postOrder(@QueryMap Map<String,Object> params);
}
