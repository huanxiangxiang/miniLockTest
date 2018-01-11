package com.example.jarvist.minilock.Activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.example.jarvist.minilock.R;
import com.tencent.mm.opensdk.modelmsg.SendMessageToWX;
import com.tencent.mm.opensdk.modelmsg.WXMediaMessage;
import com.tencent.mm.opensdk.modelmsg.WXWebpageObject;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import java.io.ByteArrayOutputStream;

public class ShareActivity extends AppCompatActivity implements View.OnClickListener{

    ImageView shareWxFriend ;
    ImageView shareWxScene;
    private static final String APP_ID = "wx9a85a1f465a2f287";
    private IWXAPI api;
    private final int Type_WXSceneTimeline = 0;
    private final int Type_WXSceneSession = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share);
        api = WXAPIFactory.createWXAPI(this,APP_ID,true);
        api.registerApp(APP_ID);
        Toolbar toolbar=(Toolbar)findViewById(R.id.toolbar);
        toolbar.setTitle("分享给好友");
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.back1);
        }
        shareWxFriend = (ImageView)findViewById(R.id.shareWXFriend);
        shareWxScene = (ImageView)findViewById(R.id.shareWXScene);
        shareWxScene.setOnClickListener(this);
        shareWxFriend.setOnClickListener(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {
            case android.R.id.home:
                Intent intent = new Intent(ShareActivity.this, MainActivity.class);
                startActivity(intent);
                ShareActivity.this.finish();
                break;
        }
        return true;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId())
        {
            case R.id.shareWXFriend:
                share(Type_WXSceneSession);
                break;
            case R.id.shareWXScene:
                share(Type_WXSceneTimeline);
                break;
            default:
                break;
        }
    }

    private void share(int type) {
        WXWebpageObject webpageObject = new WXWebpageObject();
        webpageObject.webpageUrl = "http://www.initobject.com/";
        WXMediaMessage msg = new WXMediaMessage(webpageObject);
        msg.title = "Hi,MiniLock";
        msg.description = "这是一个智能锁应用";
        Bitmap thumb = BitmapFactory.decodeResource(getResources(), R.drawable.minilock16);
        msg.thumbData = bmpToByteArray(thumb, true);
        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = buildTransaction("Req");
        req.message = msg;
        switch (type) {
            case Type_WXSceneSession:
                req.scene = SendMessageToWX.Req.WXSceneSession;
                break;
            case Type_WXSceneTimeline:
                req.scene = SendMessageToWX.Req.WXSceneTimeline;
                break;
        }
        api.sendReq(req);
        finish();
    }

    private String buildTransaction(final String type) {
        return (type == null) ? String.valueOf(System.currentTimeMillis()) : type + System.currentTimeMillis();
    }

    public static byte[] bmpToByteArray(final Bitmap bmp,
                                        final boolean needRecycle) {
        int i;
        int j;
        if (bmp.getHeight() > bmp.getWidth()) {
            i = bmp.getWidth();
            j = bmp.getWidth();
        } else {
            i = bmp.getHeight();
            j = bmp.getHeight();
        }

        Bitmap localBitmap = Bitmap.createBitmap(i, j, Bitmap.Config.RGB_565);
        Canvas localCanvas = new Canvas(localBitmap);

        while (true) {
            localCanvas.drawBitmap(bmp, new Rect(0, 0, i, j), new Rect(0, 0, i,
                    j), null);
            if (needRecycle)
                bmp.recycle();
            ByteArrayOutputStream localByteArrayOutputStream = new ByteArrayOutputStream();
            localBitmap.compress(Bitmap.CompressFormat.JPEG, 100,
                    localByteArrayOutputStream);
            localBitmap.recycle();
            byte[] arrayOfByte = localByteArrayOutputStream.toByteArray();
            try {
                localByteArrayOutputStream.close();
                return arrayOfByte;
            } catch (Exception e) {
                // F.out(e);
            }
            i = bmp.getHeight();
            j = bmp.getHeight();
        }
    }


    @Override
    protected void onPause() {
        overridePendingTransition(0,0);
           super.onPause();
        }

}
