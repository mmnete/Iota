package mjhub_media.iota;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity  {



    /*
    Get the views in order to implenment them...
     */
    private EditText email;
    private EditText password;


    FirebaseAuth firebaseAuth;

    private ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        firebaseAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);

        email = (EditText) findViewById(R.id.logIn_email);
        password = (EditText) findViewById(R.id.logIn_password);


    }


    @Override
    protected void onStart() {
        super.onStart();
        checkIfUserIsLoggedIn();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        checkIfUserIsLoggedIn();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkIfUserIsLoggedIn();
    }


    public void logInNow(View v) {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {

            logIn(email.getText().toString(),password.getText().toString());

        } else{

            Toast.makeText(MainActivity.this,"No Internet Connection",Toast.LENGTH_LONG).show();

    }

    }

    public void move2register(View v){
        Intent i = new Intent(this, Register.class);
        startActivity(i);
    }


    public void move2forgotPassword(View v){
        Intent i = new Intent(this, Forgot_password.class);
        startActivity(i);
    }

    private void logIn(String email, String password){

        if(!email.equals("") && !password.equals("")){

            if(email.length() < 60 && password.length() < 60){

                progressDialog.setMessage(getString(R.string.logging_in));
                progressDialog.show();
                 firebaseAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                     @Override
                     public void onComplete(@NonNull Task<AuthResult> task) {
                       progressDialog.dismiss();
                       if(task.isSuccessful()){
                           Toast.makeText(MainActivity.this,"Welcome back!",Toast.LENGTH_LONG).show();
                           userJustLoggedIn();
                       }else{
                           Toast.makeText(MainActivity.this,"Log In failed, please check your credentials",Toast.LENGTH_LONG).show();
                       }

                     }
                 });




            }else{
                Toast.makeText(this,getString(R.string.tooLongInput),Toast.LENGTH_LONG).show();
            }
        }else{
            Toast.makeText(this,getString(R.string.incomplete_form),Toast.LENGTH_LONG).show();
        }






    }


    private void userJustLoggedIn(){
        Intent i = new Intent(this, search.class);
        startActivity(i);
    }


    private void checkIfUserIsLoggedIn(){
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if(user != null){
            Intent i = new Intent(this, search.class);
            startActivity(i);
        }
    }










}
