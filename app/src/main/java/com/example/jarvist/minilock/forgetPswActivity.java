package com.example.jarvist.minilock;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.RequestPasswordResetCallback;

public class forgetPswActivity extends AppCompatActivity implements View.OnClickListener{


    private Button resetPwd;
    private EditText mailAdress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgetpsw);
        Toolbar toolBar=(Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolBar);
        ActionBar actionBar=getSupportActionBar();
        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.back_button);
        }
        resetPwd = (Button)findViewById(R.id.resetPwd);
        mailAdress = (EditText)findViewById(R.id.mail_adress);
        resetPwd.setOnClickListener(this);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {
            case android.R.id.home:
                Intent intent=new Intent(forgetPswActivity.this,LoginActivity.class);
                startActivity(intent);
               forgetPswActivity.this.finish();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId())
        {
            case R.id.resetPwd:
                String MailAdress = mailAdress.getText().toString();
                AVUser.requestPasswordResetInBackground(MailAdress, new RequestPasswordResetCallback() {
                    @Override
                    public void done(AVException e) {
                        if (e == null) {

                        } else {
                            e.printStackTrace();
                        }
                    }
                });
        }
    }
}
