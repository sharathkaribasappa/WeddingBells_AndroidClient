package com.weddingbells_androidclient;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class RegistrationActivity extends AppCompatActivity {

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserRegisterTask mAuthTask = null;

    // UI references.
    private EditText mFirstNameView;
    private EditText mLastNameView;
    private EditText mEmailView;
    private EditText mPasswordView;
    private EditText mPhoneNumberView;
    private RadioGroup mGender;
    private View mRegisterFormView;
    private View mProgressView;
    private String mGenderSelected = "Male";

    private static String REGISTRATION_SUCCESSFUL = "Successfully Registered";
    private static String INVALID_EMAIL = "Email Not Valid";
    private static String WEAK_PASSWORD = "Password Weak";
    private static String USER_REGISTERED_ALREADY = "Email already Registered";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mFirstNameView = (EditText) findViewById(R.id.editText);
        mLastNameView = (EditText) findViewById(R.id.editText2);
        mEmailView = (EditText) findViewById(R.id.editText3);
        mPasswordView = (EditText) findViewById(R.id.editText5);
        mPhoneNumberView = (EditText) findViewById(R.id.editText4);

        mGender = (RadioGroup) findViewById(R.id.radioGroup);
        mGender.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RadioButton rb = (RadioButton)findViewById(checkedId);
                mGenderSelected = rb.getText().toString();
                Log.e("sharath", "*** button id:" + checkedId + " name:" + mGenderSelected);
            }
        });

        mRegisterFormView = findViewById(R.id.register_form);
        mProgressView = findViewById(R.id.register_progress);

        Button submit = (Button) findViewById(R.id.signupbutton);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptRegistration();
            }
        });
    }

    /**
     * Attempts to register the account specified by the registration form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual registration attempt is made.
     */
    private void attemptRegistration() {
        showProgress(true);

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);
        mFirstNameView.setError(null);
        mLastNameView.setError(null);
        mPhoneNumberView.setError(null);

        String emailtxt = mEmailView.getText().toString();
        String passwordtxt = mPasswordView.getText().toString();

        Map<String, String> stringMap = new HashMap<>();
        stringMap.put("email", emailtxt);
        stringMap.put("password", passwordtxt);
        stringMap.put("firstname", mFirstNameView.getText().toString());
        stringMap.put("lastname", mLastNameView.getText().toString());
        stringMap.put("phonenumber", mPhoneNumberView.getText().toString());
        stringMap.put("gender",mGenderSelected);

        Map<String, String> stringMap = new HashMap<>();
        stringMap.put("email", emailtxt);
        stringMap.put("password", passwordtxt);
        stringMap.put("firstname", mFirstNameView.getText().toString());
        stringMap.put("lastname", mLastNameView.getText().toString());
        stringMap.put("phonenumber", mPhoneNumberView.getText().toString());
        stringMap.put("gender",mGenderSelected);

        if (mAuthTask != null || !isEmailValid(emailtxt) || !isPasswordValid(passwordtxt)) {
            return;
        }

        mAuthTask = new UserRegisterTask(stringMap);
        mAuthTask.execute();
    }

    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserRegisterTask extends AsyncTask<Void, Void, String> {

        Map<String, String> mRegistrationMap;

        UserRegisterTask(Map<String, String> stringMap) {
            mRegistrationMap = stringMap;
        }

        @Override
        protected String doInBackground(Void... params) {
            // attempt authentication against a network service.
            String jsonstr = null;
            try {
                // Simulate network access.
                String requestBody = Util.build_Login_Registration_Parameters(mRegistrationMap);
                ServerRequest sr = new ServerRequest();
                JSONObject json = sr.getJSONFromUrl("http://WeddingBells.cloudapp.net:28017/register", requestBody, "POST");

                if(json != null){
                    try{
                        jsonstr = json.getString("response");
                    }catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                return null;
            }
            return jsonstr;
        }

        @Override
        protected void onPostExecute(final String jsonresponse) {
            mAuthTask = null;
            showProgress(false);

            if(jsonresponse != null) {
                if(REGISTRATION_SUCCESSFUL.equals(jsonresponse) || USER_REGISTERED_ALREADY.equals(jsonresponse)) {
                    Toast.makeText(getApplication(), jsonresponse, Toast.LENGTH_LONG).show();
                    Intent I = new Intent(RegistrationActivity.this,LoginActivity.class);
                    startActivity(I);

                    finish();
                }

                if(WEAK_PASSWORD.equals(jsonresponse)) {
                    mPasswordView.setError(getString(R.string.weak_password));
                    mPasswordView.requestFocus();
                }

                if(INVALID_EMAIL.equals(jsonresponse)) {
                    mEmailView.setError(getString(R.string.weak_password));
                    mEmailView.requestFocus();
                }
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mRegisterFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mRegisterFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mRegisterFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mRegisterFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

}
