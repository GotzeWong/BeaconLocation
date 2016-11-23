package com.kyvlabs.beaconadvertiser.network.api;

import com.kyvlabs.beaconadvertiser.network.model.ForgotModel;
import com.kyvlabs.beaconadvertiser.network.model.Group;
import com.kyvlabs.beaconadvertiser.network.model.LoginModel;
import com.kyvlabs.beaconadvertiser.network.model.RegistrationModel;

import java.util.ArrayList;

import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.GET;
import retrofit.http.POST;
import rx.Observable;

public interface LoginService {

    @POST("/api/register/")
    @FormUrlEncoded
    Observable<RegistrationModel> register(@Field(value = "ClientUsers[email]") String email, @Field(value = "ClientUsers[password]") String pass, @Field(value = "ClientUsers[group_ids]") String jsonGroupsArray);

    @POST("/api/login/")
    @FormUrlEncoded
    Observable<LoginModel> login(@Field(value = "ClientUsers[email]") String email, @Field(value = "ClientUsers[password]") String pass, @Field(value = "ClientUsers[group_ids]") String jsonGroupsArray);

    @POST("/api/fb-auth/")
    @FormUrlEncoded
    Observable<LoginModel> loginFb(@Field(value = "ClientUsers[email]") String email, @Field(value = "ClientUsers[password]") String pass, @Field(value = "ClientUsers[auth_key]") String fbAuth);

    @POST("/api/password-restore/")
    @FormUrlEncoded
    Observable<ForgotModel> forgotPassword(@Field(value = "ClientUsers[email]") String email);

    @GET("smart-city/categories")
    Observable<ArrayList<Group>> getGroups();
}
