package com.example.jarvist.minilock.Activity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.LogInCallback;
import com.example.jarvist.minilock.R;
import com.example.jarvist.minilock.Adapter.UserAdapter;
import com.example.jarvist.minilock.SQL.UserContract;
import com.example.jarvist.minilock.SQL.UserSQLHelper;

import java.util.Map;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener,AdapterView.OnItemClickListener {

    private EditText accountText;
    private EditText passwordText;
    private Button loginBtn;
    private TextView forgetBtn;
    private TextView registBtn;
    private String account;
    private String password;
    private String currentUserName;
    private String currentUserEmail;
    private ImageView drow_up;
    private ImageView drow_down;
    private PopupWindow popupWindow;
    private ListView userListview;
    private UserAdapter userAdapter;
    private UserSQLHelper sqlHelper;
    private TextView loginlast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Toolbar toolBar=(Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolBar);
        ActionBar actionBar=getSupportActionBar();
        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        //isLoginReady();
        Typeface typeFace = Typeface.createFromAsset(getAssets(), "fonts/幼圆.ttf");
        sqlHelper = new UserSQLHelper(LoginActivity.this);
        accountText = (EditText) findViewById(R.id.editAccount);
        accountText.setTypeface(typeFace);
        passwordText = (EditText) findViewById(R.id.editPassword);
        passwordText.setTypeface(typeFace);
        loginBtn = (Button) findViewById(R.id.loginBtn);
        forgetBtn = (TextView) findViewById(R.id.forgetView);
        registBtn = (TextView) findViewById(R.id.registView);
        forgetBtn.setTypeface(typeFace);
        registBtn.setTypeface(typeFace);
        loginBtn.setTypeface(typeFace);
        drow_up = (ImageView) findViewById(R.id.drow_up_btn);
        drow_down = (ImageView) findViewById(R.id.drow_down_btn);
        userAdapter = new UserAdapter(LoginActivity.this, R.layout.adapter_user_item, sqlHelper);
        View contentView = LayoutInflater.from(LoginActivity.this).inflate(R.layout.adapter_user_lv, null);
        userListview = (ListView) contentView.findViewById(R.id.user_lv);
        userListview.setAdapter(userAdapter);
        userListview.setOnItemClickListener(this);
        popupWindow = new PopupWindow(contentView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setFocusable(false);
        drow_up.setOnClickListener(this);
        drow_down.setOnClickListener(this);
        loginBtn.setOnClickListener(this);
        forgetBtn.setOnClickListener(this);
        registBtn.setOnClickListener(this);
        loginlast=(TextView)findViewById(R.id.loginLast);
        SpannableStringBuilder style =new SpannableStringBuilder("登录即代表阅读并同意服务条款");
        style.setSpan(new ForegroundColorSpan(Color.rgb(22,202,255)),10,14,Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        loginlast.setText(style);
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.loginBtn:
                account = accountText.getText().toString();
                password = passwordText.getText().toString();
                if (account == null || account.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "请输入用户名", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (password == null || password.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "请输入密码", Toast.LENGTH_SHORT).show();
                    return;
                }
                AVUser.logInInBackground(account, password, new LogInCallback<AVUser>() {
                    @Override
                    public void done(AVUser avUser, AVException e) {
                        if (e == null) {
                            if (!userAdapter.isExistData(account)) {
                                userAdapter.addData(account, password);
                            }
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(intent);
                            LoginActivity.this.finish();
                            Toast.makeText(LoginActivity.this, "登录成功", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }

                    }
                });
                break;
            case R.id.forgetView:
                Intent intent1 = new Intent(LoginActivity.this, forgetPswActivity.class);
                startActivity(intent1);
                LoginActivity.this.finish();
                break;
            case R.id.registView:
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
                break;
            case R.id.drow_up_btn:
                popupWindow.dismiss();
                drow_up.setVisibility(View.GONE);
                drow_down.setVisibility(View.VISIBLE);
                break;
            case R.id.drow_down_btn:
                popupWindow.showAsDropDown(accountText);
                drow_down.setVisibility(View.GONE);
                drow_up.setVisibility(View.VISIBLE);
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long l) {
        popupWindow.dismiss();
        Map<String, Object> item = (Map<String, Object>) userAdapter.getItem(position);
        accountText.setText((String) item.get(UserContract.UserTable.COLUMN_NAME_USERNAME));
        passwordText.setText((String) item.get(UserContract.UserTable.COLUMN_NAME_PASSWORD));
        accountText.setSelection(((String) item.get(UserContract.UserTable.COLUMN_NAME_USERNAME)).length());
        passwordText.setSelection(((String) item.get(UserContract.UserTable.COLUMN_NAME_PASSWORD)).length());
        drow_up.setVisibility(View.GONE);
        drow_down.setVisibility(View.VISIBLE);
    }

    public void isLoginReady() {
            AVUser currentUser = AVUser.getCurrentUser();
            if (currentUser != null) {
                // 跳转到首页
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                currentUserEmail = AVUser.getCurrentUser().getUsername();
                currentUserEmail = AVUser.getCurrentUser().getEmail();
                Bundle bundle = new Bundle();
                bundle.putString("currentUserName", currentUserName);
                bundle.putString("currentUserEmail", currentUserEmail);
                intent.putExtras(bundle);
                startActivity(intent);
            }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onResume() {
        super.onResume();
        isLoginReady();
    }
}


