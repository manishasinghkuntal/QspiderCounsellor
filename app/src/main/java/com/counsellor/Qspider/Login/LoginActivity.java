package com.counsellor.Qspider.Login;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


import com.counsellor.Qspider.Login.model.LoginRes;
import com.counsellor.Qspider.home.MainActivity;
import com.counsellor.Qspider.R;
import com.counsellor.Qspider.helper.APIClient;
import com.counsellor.Qspider.helper.APIInterface;
import com.counsellor.Qspider.helper.PrefManager;
import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {
    LoginRes LoginResponsesData;
    EditText username, password;
    Button loginBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        username = (EditText) findViewById(R.id.username);
        password = (EditText) findViewById(R.id.password);
        loginBtn = (Button) findViewById(R.id.login);
        if (!new PrefManager(this).isUserLogedOut()) {
            startHomeActivity();
        }
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // validate the fields and call sign method to implement the api
                if (validate(username) && validate(password)) {
                    login(username.getText().toString().trim(), password.getText().toString().trim());
                }
            }
        });
    }

    private boolean validate(EditText editText) {
        // check the lenght of the enter data in EditText and give error if its empty
        if (editText.getText().toString().trim().length() > 0) {
            return true; // returns true if field is not empty
        }
        editText.setError("Please Fill This");
        editText.requestFocus();
        return false;
    }

    public void login(final String username, String password) {

        APIInterface apiInterface = APIClient.getClient().create(APIInterface.class);
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("username", username);
        jsonObject.addProperty("password", password);
        Call call = apiInterface.createUser(jsonObject);
        call.enqueue(new Callback<LoginRes>() {

            @Override
            public void onResponse(Call call, Response response) {
                Log.d("LoginData", response.code() + "" + "Msg:" + response.raw());
                if (response.isSuccessful()) {
                    LoginRes resource = (LoginRes) response.body();


                    new PrefManager(LoginActivity.this).saveLoginDetails(username, resource.getAuthToken());
                    startHomeActivity();
                } else {
                    Toast.makeText(LoginActivity.this, "Please enter correct credential", Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onFailure(Call call, Throwable t) {
                Log.d("LoginData", t.getMessage() + "");
                Toast.makeText(LoginActivity.this, "Please check your crendential", Toast.LENGTH_SHORT).show();

            }
        });

    }

    void startHomeActivity() {
        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);
        finish();
    }
}