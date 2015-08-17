package org.ohmage.mobility;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

import io.smalldatalab.omhclient.DSUClient;

public class LoginActivity extends Activity {
    final static private String TAG = LoginActivity.class.getSimpleName();
    private TextView editDsuUrl;
    private DSUClient mDSUClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        this.findViewById(R.id.sign_in_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signinGoogle();

            }
        });
        this.findViewById(R.id.login_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signinOmh();
            }
        });
        // Allow user the change the DSU URL
        editDsuUrl = (TextView) this.findViewById(R.id.edit_dsu_url);
        editDsuUrl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                builder.setTitle("Server Address");

                // Set up the input
                final EditText input = new EditText(LoginActivity.this);
                input.setText(DSUHelper.getUrl(LoginActivity.this));
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                builder.setView(input);

                // Set up the buttons
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        DSUHelper.setUrl(LoginActivity.this, input.getText().toString());
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();
            }
        });
    }

    private void signinGoogle() {
        mDSUClient =
                new DSUClient(
                        DSUHelper.getUrl(this),
                        this.getString(R.string.dsu_client_id),
                        this.getString(R.string.dsu_client_secret),
                        this);
        new Thread() {
            @Override
            public void run() {
                try {
                    if (mDSUClient.blockingGoogleSignIn(LoginActivity.this) != null) {
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

    private void signinOmh() {
        final String username = ((EditText) this.findViewById(R.id.username)).getText().toString();
        final String password = ((EditText) this.findViewById(R.id.password)).getText().toString();

        if (username.isEmpty() || password.isEmpty()){
            showToast("You must enter a username and password.");
            return;
        }

        mDSUClient =
                new DSUClient(
                        DSUHelper.getUrl(this),
                        this.getString(R.string.dsu_client_id),
                        this.getString(R.string.dsu_client_secret),
                        this);
        new Thread() {
            @Override
            public void run() {
                try {
                    if (mDSUClient.blockingOmhSignIn(LoginActivity.this, username, password) != null) {
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
