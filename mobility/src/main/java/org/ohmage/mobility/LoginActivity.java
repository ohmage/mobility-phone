package org.ohmage.mobility;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.io.IOException;

import io.smalldatalab.omhclient.DSUClient;

public class LoginActivity extends Activity {
    final static private String TAG = LoginActivity.class.getSimpleName();
    private DSUClient dsuClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dsuClient = new DSUClient(
                this.getString(R.string.dsu_client_url),
                this.getString(R.string.dsu_client_id),
                this.getString(R.string.dsu_client_secret),
                this);
        setContentView(R.layout.activity_login);
        this.findViewById(R.id.sign_in_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                login();
            }
        });
    }

    private void login() {
        new Thread() {
            @Override
            public void run() {
                try {
                    if (dsuClient.blockingGoogleSignIn(LoginActivity.this) != null) {
                        showToast("Sign In Succeeded");
                        setResult(RESULT_OK);
                        LoginActivity.this.finish();
                        return;
                    }
                } catch (IOException e) {
                    showToast("Sign In Failed. Please check your Internet connection");
                    Log.e(LoginActivity.class.getSimpleName(), "Network Error", e);


                } catch (Exception e) {
                    showToast("Sign In Failed. Unknown Error.");
                    Log.e(LoginActivity.class.getSimpleName(), "Sign In Failed", e);

                }

            }
        }.start();

    }

    private void showToast(final String msg) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(LoginActivity.this, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }


}
