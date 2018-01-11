package com.example.jarvist.minilock.Activity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.RequestEmailVerifyCallback;
import com.avos.avoscloud.SignUpCallback;
import com.example.jarvist.minilock.R;

public class RegisterActivity extends AppCompatActivity {

    private EditText numberText;
    private EditText passwordText;
    private EditText mailText;
    private String nickName;
    private String password;
    private String email;
    private Button registerEnsure;
    private TextView sendMail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        Toolbar toolBar=(Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolBar);
        ActionBar actionBar=getSupportActionBar();
        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.back1);
        }
        numberText = (EditText)findViewById(R.id.editnum);
        mailText = (EditText)findViewById(R.id.mail) ;
        passwordText = (EditText)findViewById(R.id.password);
        registerEnsure = (Button)findViewById(R.id.registerEnsure);
        sendMail = (TextView)findViewById(R.id.sendMailTextView) ;
        registerEnsure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nickName = numberText.getText().toString();
                password = passwordText.getText().toString();
                email = mailText.getText().toString();

                AVUser user = new AVUser();
                user.setUsername(nickName);
                user.setPassword(password);
                user.setEmail(email);
                user.signUpInBackground(new SignUpCallback() {
                    @Override
                    public void done(AVException e) {
                        if(e == null){
                            Intent intent = new Intent(RegisterActivity.this,MainActivity.class);
                            startActivity(intent);
                            RegisterActivity.this.finish();
                            Toast.makeText(RegisterActivity.this,"注册成功……即将跳转到主页面",Toast.LENGTH_SHORT).show();
                        }
                        else{
                            Toast.makeText(RegisterActivity.this,e.getMessage(),Toast.LENGTH_LONG).show();
                        }
                    }
                });

            }
        });

        sendMail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AVUser.requestEmailVerifyInBackground(email,new RequestEmailVerifyCallback(){

                    @Override
                    public void done(AVException e){
                        if(e == null){
                            Toast.makeText(RegisterActivity.this,"发送邮件成功，请前往邮箱进行激活",Toast.LENGTH_SHORT).show();
                        }
                        else{
                            Toast.makeText(RegisterActivity.this,"邮件发送失败，请点击重新发送邮件",Toast.LENGTH_SHORT).show();
                        }

                    }
                });
            }
        });
        SpannableStringBuilder style =new SpannableStringBuilder("没有收到邮件？点击重新发送邮件");
        style.setSpan(new ForegroundColorSpan(Color.rgb(22,202,255)),7,15,Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        sendMail.setText(style);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {
            case android.R.id.home:
                Intent intent=new Intent(RegisterActivity.this,LoginActivity.class);
                startActivity(intent);
                RegisterActivity.this.finish();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

}
