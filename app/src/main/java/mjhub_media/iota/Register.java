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
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Register extends AppCompatActivity {


    private EditText email;
    private EditText password;
    private EditText confirmPassword;


    FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        firebaseAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);

        email = (EditText)findViewById(R.id.register_email);
        password = (EditText) findViewById(R.id.register_password);
        confirmPassword = (EditText)findViewById(R.id.register_conPassword);
        Button register = (Button) findViewById(R.id.register_button);


        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signUp(email.getText().toString(),password.getText().toString(),confirmPassword.getText().toString());
            }
        });




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

    public void move2logIn(View v){
        Intent i = new Intent(this,MainActivity.class);
        startActivity(i);
    }


    private void signUp(String email, String password, String confirmPassword){


        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {



            if(!email.equals("") && !password.equals("") && !confirmPassword.equals("")){
                if(email.length() < 60 && password.length() < 60 && confirmPassword.length() < 60){

                    if(password.equals(confirmPassword)){
                        progressDialog.setMessage(getString(R.string.creating_account));
                        progressDialog.show();

                        firebaseAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if(task.isSuccessful()){
                                    progressDialog.dismiss();
                                    Toast.makeText(Register.this,getString(R.string.registration_success),Toast.LENGTH_LONG).show();
                                    Intent i = new Intent(Register.this,MainActivity.class);
                                    startActivity(i);
                                }else{
                                    progressDialog.dismiss();
                                    Toast.makeText(Register.this,getString(R.string.registration_fail), Toast.LENGTH_LONG).show();
                                }
                            }
                        });


                    }else{
                        Toast.makeText(this,getString(R.string.password_misMatch),Toast.LENGTH_LONG).show();
                    }


                }else{
                    Toast.makeText(this,getString(R.string.tooLongInput),Toast.LENGTH_LONG).show();
                }
            }else{
                Toast.makeText(this,getString(R.string.incomplete_form),Toast.LENGTH_LONG).show();
            }

        } else{

            Toast.makeText(Register.this,"No Internet Connection",Toast.LENGTH_LONG).show();

        }



    }


    private void checkIfUserIsLoggedIn(){
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if(user != null){
            Intent i = new Intent(this, search.class);
            startActivity(i);
        }
    }

}
