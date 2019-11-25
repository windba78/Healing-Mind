package com.example.myapplication;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

public class In_Google_Activity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {
    SignInButton Google_Login;
    private static final int RC_SIGN_IN = 100;
    private static final String TAG = "phpquerytest", TAG_JSON = "webnautes", TAG_ID = "id", TAG_PASSWORD = "password", TAG_NAME = "name", TAG_BIRTH = "birth", TAG_PHONE = "phone", TAG_SEX = "sex", TAG_EMAIL = "email", TAG_FIRST = "first";
    private FirebaseAuth mAuth;
    private GoogleApiClient mGoogleApiClient;
    boolean auto;
    String IP, ID, EMAIL, NAME, WHAT = "구글";
    String mJsonString, PASSWORD, BIRTH, PHONE, SEX, FIRST;
    MyDBHelper myHelper;
    SQLiteDatabase sqlDB;

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseAuth.getInstance().signOut();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.in_google_activity);
        Intent get_intent = getIntent();
        IP = get_intent.getStringExtra("IP");
        auto = get_intent.getBooleanExtra("auto", auto);
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
        signOut();
        mAuth = FirebaseAuth.getInstance();
        Google_Login = findViewById(R.id.Google_Login);
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                GoogleSignInAccount account = result.getSignInAccount();
                mGoogleApiClient.disconnect();
                firebaseAuthWithGoogle(account);
            } else {
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (!task.isSuccessful()) {
                            finish();
                        } else {
                            String n1 = acct.getFamilyName();
                            String n2 = acct.getGivenName();
                            NAME = n2 + n1;
                            EMAIL = acct.getEmail();
                            ID = acct.getId();
                            mGoogleApiClient.disconnect();
                            Log.d("GNAME", NAME);
                            Log.d("GMAILE", acct.getEmail());
                            Log.d("GID", acct.getId());
                            GetData2 task_google = new GetData2();
                            task_google.execute();
                        }
                    }
                });
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }

    class GetData2 extends AsyncTask<String, Void, String> {
        String errorString = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            Log.d(TAG, "response - " + result);
            if (result != null) {
                mJsonString = result;
                showResult2();
            }
        }

        @Override
        protected String doInBackground(String... params) {
            String serverURL = "http://" + IP + "/login_main.php";
            String postParameters = "id=" + ID + "&password=" + ID;
            try {
                URL url = new URL(serverURL);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setReadTimeout(5000);
                httpURLConnection.setConnectTimeout(5000);
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoInput(true);
                httpURLConnection.connect();
                OutputStream outputStream = httpURLConnection.getOutputStream();
                outputStream.write(postParameters.getBytes("UTF-8"));
                outputStream.flush();
                outputStream.close();
                int responseStatusCode = httpURLConnection.getResponseCode();
                Log.d(TAG, "response code - " + responseStatusCode);
                InputStream inputStream;
                if (responseStatusCode == HttpURLConnection.HTTP_OK) {
                    inputStream = httpURLConnection.getInputStream();
                } else {
                    inputStream = httpURLConnection.getErrorStream();
                }
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    sb.append(line);
                }
                bufferedReader.close();
                return sb.toString().trim();
            } catch (Exception e) {
                Log.d(TAG, "InsertData: Error ", e);
                errorString = e.toString();
                return null;
            }
        }
    }

    private void showResult2() {
        try {
            JSONObject jsonObject = new JSONObject(mJsonString);
            JSONArray jsonArray = jsonObject.getJSONArray(TAG_JSON);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject item = jsonArray.getJSONObject(i);
                ID = item.getString(TAG_ID);
                PASSWORD = item.getString(TAG_PASSWORD);
                NAME = item.getString(TAG_NAME);
                BIRTH = item.getString(TAG_BIRTH);
                PHONE = item.getString(TAG_PHONE);
                SEX = item.getString(TAG_SEX);
                EMAIL = item.getString(TAG_EMAIL);
                FIRST = item.getString(TAG_FIRST);
                HashMap<String, String> hashMap = new HashMap<>();
                hashMap.put(TAG_ID, ID);
                hashMap.put(TAG_PASSWORD, PASSWORD);
                hashMap.put(TAG_NAME, NAME);
                hashMap.put(TAG_BIRTH, BIRTH);
                hashMap.put(TAG_PHONE, PHONE);
                hashMap.put(TAG_SEX, SEX);
                hashMap.put(TAG_EMAIL, EMAIL);
                hashMap.put(TAG_FIRST, FIRST);
            }
            if (auto) {
                myHelper = new MyDBHelper(this);
                Cursor login_cursor;
                sqlDB = myHelper.getReadableDatabase();
                login_cursor = sqlDB.rawQuery("SELECT *FROM IDcheck WHERE  checked ='" + "1" + "';", null);
                if (!login_cursor.moveToNext()) {
                    sqlDB = myHelper.getWritableDatabase();
                    sqlDB.execSQL("INSERT INTO IDcheck VALUES (NULL, '" + ID + "','" + PASSWORD + "','" + EMAIL + "','" + NAME + "','" + "1" + "');");
                }
                login_cursor.close();
                sqlDB.close();
            }
            Intent intent = new Intent(getApplicationContext(), In_Frame_Activity.class);
            intent.putExtra("IP", IP);
            intent.putExtra("ID", ID);
            intent.putExtra("EMAIL", EMAIL);
            intent.putExtra("PASSWORD", PASSWORD);
            intent.putExtra("NAME", NAME);
            startActivity(intent);
            finish();
        } catch (JSONException e) {
            Intent intent = new Intent(getApplicationContext(), Yun_Idcheck_Activity.class);
            intent.putExtra("IP", IP);
            intent.putExtra("ID", ID);
            intent.putExtra("NAME", NAME);
            intent.putExtra("EMAIL", EMAIL);
            intent.putExtra("PASSWORD", ID);
            intent.putExtra("what", WHAT);
            startActivity(intent);
            finish();
        }
    }

    public void signOut() {
        mGoogleApiClient.connect();
        mGoogleApiClient.registerConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
            @Override
            public void onConnected(@Nullable Bundle bundle) {
                mAuth.signOut();
                if (mGoogleApiClient.isConnected()) {
                    Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(new ResultCallback<Status>() {
                        @Override
                        public void onResult(@NonNull Status status) {
                            if (status.isSuccess()) {
                            } else {
                            }
                        }
                    });
                }
            }

            @Override
            public void onConnectionSuspended(int i) {
                Log.v("알림", "Google API Client Connection Suspended");
            }
        });
    }
}