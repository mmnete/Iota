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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Forgot_password extends AppCompatActivity {


    FirebaseAuth firebaseAuth;

    private EditText email;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        firebaseAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);

        email = (EditText) findViewById(R.id.reset_Email);

    }



    @Override
    protected void onStart() {
        super.onStart();
        checkIfUserIsLoggedIn();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkIfUserIsLoggedIn();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        checkIfUserIsLoggedIn();
    }





    public void resetPassword(View v){

        if(!email.getText().toString().equals("")){

            if(email.getText().length() < 60){

                sendResetPassword(email.getText().toString());


            }else {
                Toast.makeText(this,getString(R.string.tooLongInput),Toast.LENGTH_LONG).show();
            }

        }else{
            Toast.makeText(this,getString(R.string.incomplete_form),Toast.LENGTH_LONG).show();
        }

    }



    private void sendResetPassword(String email){
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {


            progressDialog.setMessage("Sending reset Email...");
            progressDialog.show();

            FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            progressDialog.dismiss();
                            if (task.isSuccessful()) {
                                Toast.makeText(Forgot_password.this,"Check your email inbox.",Toast.LENGTH_LONG).show();
                            }else{
                                Toast.makeText(Forgot_password.this,"Couldn't send Reset Email.",Toast.LENGTH_LONG).show();
                            }
                        }
                    });


        } else{

            Toast.makeText(Forgot_password.this,"No Internet Connection",Toast.LENGTH_LONG).show();

        }


    }


    public void move2logIn(View v){
        Intent i = new Intent(this,MainActivity.class);
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
