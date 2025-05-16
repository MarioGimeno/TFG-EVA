package com.example.appGrabacion;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.appGrabacion.models.ContactEntry;
import com.example.appGrabacion.screens.CategoriasActivity;
import com.example.appGrabacion.screens.ContactsActivity;
import com.example.appGrabacion.screens.EntidadesActivity;
import com.example.appGrabacion.screens.FolderActivity;
import com.example.appGrabacion.screens.LoginActivity;
import com.example.appGrabacion.screens.RecursosActivity;
import com.example.appGrabacion.utils.ContactManager;
import com.example.appGrabacion.utils.ContactsApi;
import com.example.appGrabacion.utils.MyFirebaseMessagingService;
import com.example.appGrabacion.utils.RetrofitClient;
import com.example.appGrabacion.utils.SessionManager;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final int REQ_PERMISSIONS = 100;
    // Aquí listamos TODOs los permisos que usa la app:
    private static final String[] REQUIRED_PERMISSIONS = {
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.ACCESS_FINE_LOCATION,
            // POST_NOTIFICATIONS solo existe en API 33+
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                    ? Manifest.permission.POST_NOTIFICATIONS
                    : Manifest.permission.ACCESS_FINE_LOCATION // dummy para no dejar array vacío
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.Theme_Calculadora_Front);
        setContentView(R.layout.activity_main);
        requestAllPermissions();

        Button btnGoLogin = findViewById(R.id.btnGoLogin);
        btnGoLogin.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
        });

        Button btnGoFolder = findViewById(R.id.btnGoFolder);
        btnGoFolder.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, FolderActivity.class));
        });
        Button btnGoEntidades = findViewById(R.id.btnGoEntidades);
        btnGoEntidades.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, EntidadesActivity.class));
        });
        Button btnGoRecursos = findViewById(R.id.btnGoRecursos);
        btnGoRecursos.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, RecursosActivity.class));
        });
        Button btnGoCategorias = findViewById(R.id.btnGoCategorias);
        btnGoCategorias.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, CategoriasActivity.class))
        );

        findViewById(R.id.btnGoContacts).setOnClickListener(v ->
                startActivity(new Intent(this, ContactsActivity.class))
        );

        syncContacts();

        FirebaseMessaging.getInstance()
                .getToken()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String token = task.getResult();
                        Log.d(TAG, "Token forzado: " + token);
                        MyFirebaseMessagingService.registerTokenWithServer(
                                MainActivity.this, token
                        );
                    } else {
                        Log.w(TAG, "Fetching FCM token failed", task.getException());
                    }
                });
    }

    /**
     * Comprueba y solicita en runtime todos los permisos necesarios.
     */
    private void requestAllPermissions() {
        List<String> toRequest = new ArrayList<>();
        for (String perm : REQUIRED_PERMISSIONS) {
            // Saltarse el dummy del array en API<33
            if (perm == null) continue;
            if (ContextCompat.checkSelfPermission(this, perm)
                    != PackageManager.PERMISSION_GRANTED) {
                toRequest.add(perm);
            }
        }
        if (!toRequest.isEmpty()) {
            ActivityCompat.requestPermissions(
                    this,
                    toRequest.toArray(new String[0]),
                    REQ_PERMISSIONS
            );
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_PERMISSIONS) {
            for (int i = 0; i < permissions.length; i++) {
                String perm = permissions[i];
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    Log.w(TAG, "Permiso denegado: " + perm);
                    Toast.makeText(this,
                            "Necesito " + perm + " para funcionar correctamente",
                            Toast.LENGTH_LONG).show();
                } else {
                    Log.d(TAG, "Permiso concedido: " + perm);
                }
            }
        }
    }

    private void syncContacts() {
        String bearer = "Bearer " + SessionManager.getToken(this);
        ContactsApi api = RetrofitClient
                .getRetrofitInstance(this)
                .create(ContactsApi.class);

        api.getContacts(bearer).enqueue(new Callback<List<ContactEntry>>() {
            @Override
            public void onResponse(Call<List<ContactEntry>> call,
                                   Response<List<ContactEntry>> resp) {
                if (resp.isSuccessful() && resp.body() != null) {
                    ContactManager mgr = new ContactManager(MainActivity.this);
                    mgr.saveContacts(resp.body());
                    Log.d(TAG, "Contacts synced: " + resp.body().size());
                } else {
                    Log.e(TAG, "Error fetching contacts: " + resp.code());
                }
            }

            @Override
            public void onFailure(Call<List<ContactEntry>> call, Throwable t) {
                Log.e(TAG, "Failed to sync contacts", t);
            }
        });
    }
}
