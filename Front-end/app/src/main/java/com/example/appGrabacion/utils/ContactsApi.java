// 1) ContactsApi.java (Retrofit interface)
package com.example.appGrabacion.utils;

import com.example.appGrabacion.entities.ContactEntry;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;

// src/main/java/com/example/appGrabacion/utils/ContactsApi.java
public interface ContactsApi {
    @GET("api/contacts")
    Call<List<ContactEntry>> getContacts(@Header("Authorization") String bearer);

    @POST("api/contacts")
    Call<ContactEntry> addContact(
            @Header("Authorization") String bearer,
            @Body ContactEntry contact
    );

    @DELETE("api/contacts/{id}")
    Call<Void> deleteContact(
            @Header("Authorization") String bearer,
            @Path("id") int contactId    // ← de String a int
    );
}
