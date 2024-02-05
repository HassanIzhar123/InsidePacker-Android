package com.wireguard.insidepacker_android.activities;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.wireguard.insidepacker_android.R;
import com.wireguard.insidepacker_android.ViewModels.SignInViewModel;
import com.wireguard.insidepacker_android.models.BasicInformation.BasicInformation;
import com.wireguard.insidepacker_android.models.StateModel.StateData;

import org.json.JSONException;
import org.json.JSONObject;

public class SignInActivity extends AppCompatActivity {
    Button signInButton;
    EditText userNameEditText, passwordEditText;
    SignInViewModel signInViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        signInViewModel = new ViewModelProvider(SignInActivity.this).get(SignInViewModel.class);
        signInButton = findViewById(R.id.sign_in_btn);
        userNameEditText = findViewById(R.id.email_edittext);
        passwordEditText = findViewById(R.id.password_edittext);
        OnInitListener();
    }

    private Dialog showProgressDialog() {
        Dialog dialog = new Dialog(SignInActivity.this);
        dialog.requestWindowFeature(1);
        dialog.setContentView(R.layout.loading_process_dialog);
        dialog.setCancelable(false);
        dialog.show();
        return dialog;
    }

    private void OnInitListener() {
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = userNameEditText.getText().toString();
                String password = passwordEditText.getText().toString();
                if (username.isEmpty()) {
                    Toast.makeText(SignInActivity.this, "Please enter username", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (password.isEmpty()) {
                    Toast.makeText(SignInActivity.this, "Please enter password", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!username.matches("^[a-zA-Z0-9]+@[a-zA-Z0-9]+(\\.[a-zA-Z]{1,})?$")) {
                    Toast.makeText(SignInActivity.this, "Please enter valid username", Toast.LENGTH_SHORT).show();
                    return;
                }
                try {
                    String actualUserName = username.split("@")[0];
                    String demo = username.split("@")[1];
                    OnInit(actualUserName, demo, password);
                } catch (Exception e) {
                    Toast.makeText(SignInActivity.this, "Error Occurred!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void OnInit(String actualUserName, String demo, String password) {
        Dialog progressDialog = showProgressDialog();

        signInViewModel.getAccessToken(setBasicInformation(actualUserName, demo, password)).observe(SignInActivity.this, new Observer<StateData<?>>() {
            @Override
            public void onChanged(StateData<?> stateData) {
                switch (stateData.getStatus()) {
                    case SUCCESS:
                        String accessToken = (String) stateData.getData();
                        Toast.makeText(SignInActivity.this, "Success", Toast.LENGTH_SHORT).show();
                        break;
                    case ERROR:
                        try {
                            assert stateData.getError() != null;
                            JSONObject object = new JSONObject(stateData.getError());

                            Toast.makeText(SignInActivity.this, object.getString("detail"), Toast.LENGTH_SHORT).show();
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                        break;
                    case LOADING:
                        //TODO: Do Loading stuff
                        break;
                    case COMPLETE:
                        //TODO: Do complete stuff if necessary
                        break;
                }
                progressDialog.dismiss();
            }
        });
    }

    private BasicInformation setBasicInformation(String actualUserName, String demo, String password) {
        BasicInformation basicInformation = new BasicInformation();
        basicInformation.setUsername(actualUserName);
        basicInformation.setPassword(password);
        basicInformation.setTenantName(demo);
        return basicInformation;
    }
}