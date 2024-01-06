package com.example.authapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {
    private EditText editTextRegisterFullName , editTextRegisterEmail ,editTextRegisterDob
            ,editTextRegisterPhone, editTextRegisterPwd ,editTextRegisterConfirmPwd;
    private ProgressBar progressBar;
    private RadioGroup radioGroupRegisterGender;
    private RadioButton radioButtonRegisterGenderSelected;
    private DatePickerDialog picker;

    public TextView signIn;
    private FirebaseAuth authProfile;
    private static final String TAG = "MainActivity";

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Sign Up");
        Toast.makeText(MainActivity.this ,"You can Sign up now",Toast.LENGTH_SHORT).show();
        editTextRegisterFullName = findViewById(R.id.editText_register_full_name);
        editTextRegisterEmail = findViewById(R.id.editText_register_email);
        editTextRegisterDob = findViewById(R.id.editText_register_dob);
        editTextRegisterPhone = findViewById(R.id.editText_register_mobile);
        editTextRegisterPwd = findViewById(R.id.editText_register_password);
        editTextRegisterConfirmPwd = findViewById(R.id.editText_register_confirm_password);
        progressBar = findViewById(R.id.progressBar);

        signIn = findViewById(R.id.sign_in);
        //Radio button for gender
        radioGroupRegisterGender = findViewById(R.id.radio_group_register_gender);
        radioGroupRegisterGender.clearCheck();
        authProfile = FirebaseAuth.getInstance();

        //Setting up date picker
        editTextRegisterDob.setOnClickListener(view -> {
            final Calendar calendar = Calendar.getInstance();
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            int month = calendar.get(Calendar.MONTH);
            int year = calendar.get(Calendar.YEAR);

            //Date picker
            picker = new DatePickerDialog(MainActivity.this, (view1, year1, month1, dayOfMonth) -> editTextRegisterDob.setText(dayOfMonth + "/" + (month1 +1) + "/" + year1),year,month,day);
            picker.show();
        });

        Button buttonRegister = findViewById(R.id.button_register);
        buttonRegister.setOnClickListener(v -> {
            //obtain the entered value
            int selectedGenderID = radioGroupRegisterGender.getCheckedRadioButtonId();
            radioButtonRegisterGenderSelected = findViewById(selectedGenderID);

            //Getting values from other fields
            String textFullName = editTextRegisterFullName.getText().toString();
            String textEmail = editTextRegisterEmail.getText().toString();
            String textDob = editTextRegisterDob.getText().toString();
            String textMobile = editTextRegisterPhone.getText().toString();
            String textPwd = editTextRegisterPwd.getText().toString();
            String textConfirmPwd = editTextRegisterConfirmPwd.getText().toString();
            String textGender ; // Can't obtain the value before verifying if any button was selected or not



            //validate mobile number using Matcher and Pattern (Regular Expression)
            String mobileRegex = "[6-9][0-9]{9}"; //First number can be {6,7,8} and rest 9 numbers can be any number
            Matcher mobileMatcher;
            Pattern mobilePattern = Pattern.compile(mobileRegex);
            mobileMatcher = mobilePattern.matcher(textMobile);

            if(TextUtils.isEmpty(textFullName)){
                Toast.makeText(MainActivity.this, "Please enter your Full Name", Toast.LENGTH_LONG).show();
                editTextRegisterFullName.setError("Full Name is required");
                editTextRegisterFullName.requestFocus();
            }else if(TextUtils.isEmpty(textEmail)){
                Toast.makeText(MainActivity.this,"Please enter your email",Toast.LENGTH_LONG).show();
                editTextRegisterEmail.setError("Email is required");
                editTextRegisterEmail.requestFocus();
            }else if(!Patterns.EMAIL_ADDRESS.matcher(textEmail).matches()){
                Toast.makeText(MainActivity.this,"Please re-enter your email",Toast.LENGTH_LONG).show();
                editTextRegisterEmail.setError("Valid email is required");
                editTextRegisterEmail.requestFocus();
            }else if(TextUtils.isEmpty(textDob)){
                Toast.makeText(MainActivity.this,"Please enter your date of birth",Toast.LENGTH_LONG).show();
                editTextRegisterDob.setError("Date of Birth is required");
                editTextRegisterDob.requestFocus();
            }else if(radioGroupRegisterGender.getCheckedRadioButtonId() == -1) {
                Toast.makeText(MainActivity.this,"Please select a gender",Toast.LENGTH_LONG).show();
                radioButtonRegisterGenderSelected.setError("Gender is required");
                radioButtonRegisterGenderSelected.requestFocus();
            }else if(TextUtils.isEmpty(textMobile)){
                Toast.makeText(MainActivity.this,"Please enter Mobile No.", Toast.LENGTH_LONG).show();
                editTextRegisterPhone.setError("Phone No. is required");
                editTextRegisterPhone.requestFocus();
            }else if(textMobile.length()!=10){
                Toast.makeText(MainActivity.this,"Please re-enter Mobile No.", Toast.LENGTH_LONG).show();
                editTextRegisterPhone.setError("Mobile No. should be 10 digits");
                editTextRegisterPhone.requestFocus();
            }else if (!mobileMatcher.find()) {
                Toast.makeText(MainActivity.this,"Please enter Mobile No.", Toast.LENGTH_LONG).show();
                editTextRegisterPhone.setError("Phone No. is not valid!!");
                editTextRegisterPhone.requestFocus();
            }else if(TextUtils.isEmpty(textPwd)){
                Toast.makeText(MainActivity.this,"Please enter your Password", Toast.LENGTH_LONG).show();
                editTextRegisterPwd.setError("Password is required");
                editTextRegisterPwd.requestFocus();
            }else if(textPwd.length()<6){
                Toast.makeText(MainActivity.this,"Password should be at least 6 characters long", Toast.LENGTH_LONG).show();
                editTextRegisterPwd.setError("Password is too short");
                editTextRegisterPwd.requestFocus();
            }else if(TextUtils.isEmpty(textConfirmPwd)){
                Toast.makeText(MainActivity.this,"Please confirm your Password", Toast.LENGTH_LONG).show();
                editTextRegisterConfirmPwd.setError("Password confirmation is required");
                editTextRegisterConfirmPwd.requestFocus();
            }else if (!textPwd.equals(textConfirmPwd)){
                Toast.makeText(MainActivity.this,"Please enter the same password", Toast.LENGTH_LONG).show();
                editTextRegisterConfirmPwd.setError("Password doesn't match");
                editTextRegisterConfirmPwd.requestFocus();
                //Clear entered passwords
                editTextRegisterPwd.clearComposingText();
                editTextRegisterConfirmPwd.clearComposingText();
            }else{
                textGender = radioButtonRegisterGenderSelected.getText().toString();
                progressBar.setVisibility(View.VISIBLE);
                registerUser(textFullName,textEmail,textDob ,textGender,textMobile,textPwd);
            }
        });
        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(),SignIn.class));
                finish();
            }
        });

    }
     void registerUser(String textFullName,String textEmail,String textDob ,String textGender,String textMobile,String textPwd){
        FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.createUserWithEmailAndPassword(textEmail,textPwd).addOnCompleteListener(MainActivity.this, task -> {
            if(task.isSuccessful()){

                FirebaseUser firebaseUser = auth.getCurrentUser();

                UserProfileChangeRequest profileChangeRequest = new UserProfileChangeRequest.Builder().setDisplayName(textFullName).build();
                assert firebaseUser != null;
                firebaseUser.updateProfile(profileChangeRequest);

                //Enter user details to FireBase realtime Database
                ReadWriteUserDetails writeUserDetails = new ReadWriteUserDetails(textDob,textGender,textMobile);

                //Extracting User reference from the Database for "Registered Users"
                DatabaseReference referenceProfile = FirebaseDatabase.getInstance().getReference("Registered Users");
                referenceProfile.child(firebaseUser.getUid()).setValue(writeUserDetails).addOnCompleteListener(task1 -> {

                    if(task1.isSuccessful()){
                        //send verification email
                        firebaseUser.sendEmailVerification();
                        Toast.makeText(MainActivity.this,"User registered successfully.Please verify your email" ,Toast.LENGTH_LONG).show();
                        // open user profile after successful registration

                        Intent intent = new Intent(MainActivity.this , UserProfileActivity.class);
                        //To prevent user from returning back to register activity on pressing back button after registration
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK |Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();   // to close the Register activity /Main activity
                    }else {
                        Toast.makeText(MainActivity.this,"User registration failed.Please try again" ,Toast.LENGTH_LONG).show();
                    }
                    progressBar.setVisibility(View.GONE);
                });

            }else{
                try {
                    throw Objects.requireNonNull(task.getException());
                }catch (FirebaseAuthWeakPasswordException e){
                    editTextRegisterPwd.setError("Password too weak. kindly use a mix of alphabets and numbers!! ");
                    editTextRegisterPwd.requestFocus();
                }catch(FirebaseAuthInvalidCredentialsException e){
                    editTextRegisterEmail.setError("Your email is invalid or already in use");
                    editTextRegisterEmail.requestFocus();
                }catch(FirebaseAuthUserCollisionException e){
                    editTextRegisterEmail.setError("User is already registered with this email. Use another email");
                    editTextRegisterEmail.requestFocus();
                }catch (Exception e){
                    Log.e(TAG ,e.getMessage());
                    Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                }
                progressBar.setVisibility(View.GONE);
            }
        });
    }
    //Check if the user is logged in or not


    @Override
    protected void onStart() {
        super.onStart();
        if(authProfile.getCurrentUser()!= null){
            Toast.makeText(MainActivity.this, "You are already logged in", Toast.LENGTH_LONG).show();
            startActivity(new Intent(MainActivity.this,UserProfileActivity.class));
            finish();
        }
    }
}
