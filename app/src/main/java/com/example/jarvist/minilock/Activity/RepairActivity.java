package com.example.jarvist.minilock.Activity;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVFile;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.SaveCallback;
import com.example.jarvist.minilock.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class RepairActivity extends AppCompatActivity implements View.OnClickListener{

    private EditText locateAddress;
    private EditText advice;
    private ImageView advice_photo1;
    private ImageView advice_photo2;
    private ImageView clear_photo1;
    private ImageView clear_photo2;
    private Button submitBtn;
    private Uri imageUri;
    private Boolean isPhoto1;//判断是否是第一张图片点击的
    private Boolean isPhoto2 = false;//判断第二张图片是否存在
    private Boolean isPhotoExist = false;
    private Boolean isTextExist = false;
    private AVFile photo1 = null;
    private AVFile photo2 = null;
    private AVUser currentUser;
    public static final int TAKE_PHOTO=1;
    public static final int CHOOSE_PHOTO=2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_repair);
        Toolbar toolbar=(Toolbar)findViewById(R.id.toolbar);
        toolbar.setTitle("故障报修");
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        Intent intent = getIntent();
        String address = intent.getExtras().getString("address");
        currentUser = getCurrentUser();
        submitBtn = (Button)findViewById(R.id.add_advice);
        locateAddress = (EditText)findViewById(R.id.address);
        locateAddress.setText(address);
        advice = (EditText)findViewById(R.id.advice);
        advice_photo1 = (ImageView)findViewById(R.id.repair_photo1);
        advice_photo2 = (ImageView)findViewById(R.id.repair_photo2);
        clear_photo1 = (ImageView)findViewById(R.id.clear_photo1);
        clear_photo2 = (ImageView)findViewById(R.id.clear_photo2);
        advice_photo1.setOnClickListener(this);
        advice_photo2.setOnClickListener(this);
        clear_photo1.setOnClickListener(this);
        clear_photo2.setOnClickListener(this);
        submitBtn.setOnClickListener(this);
        advice.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                isSubmitBtnClickable();
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
               isSubmitBtnClickable();
            }

            @Override
            public void afterTextChanged(Editable editable) {
                isSubmitBtnClickable();
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId())
        {
            case R.id.repair_photo1:
                isPhoto1 = true;
                showDialog();
                break;
            case R.id.repair_photo2:
                isPhoto1 = false;
                showDialog();
                break;
            case R.id.clear_photo1:
                if(isPhoto2)
                {
                    advice_photo2.setDrawingCacheEnabled(true);
                    Bitmap temp = Bitmap.createBitmap(advice_photo2.getDrawingCache());
                    advice_photo2.setDrawingCacheEnabled(false);
                    advice_photo1.setImageBitmap(temp);
                    advice_photo2.setImageResource(R.drawable.add_photo1);
                    advice_photo2.setVisibility(View.GONE);
                    clear_photo2.setVisibility(View.GONE);
                    isPhoto2 = false;
                    photo1 = photo2;
                    photo2 = null;
                }
                else
                {
                    advice_photo1.setImageResource(R.drawable.add_photo1);
                    clear_photo1.setVisibility(View.GONE);
                    isPhotoExist = false;
                    submitBtn.setClickable(false);
                    submitBtn.setEnabled(false);
                    photo1 = null;
                }
                break;
            case R.id.clear_photo2:
                advice_photo2.setImageResource(R.drawable.add_photo1);
                clear_photo2.setVisibility(View.GONE);
                isPhoto2 = false;
                photo2 = null;
                break;
            case R.id.add_advice:
                AVObject adviceHolder = new AVObject("SubmitAdvice");
                adviceHolder.put("advice",advice.getText().toString());
                adviceHolder.put("userID",currentUser.getObjectId());
                adviceHolder.put("position",locateAddress.getText().toString());
                adviceHolder.put("photo1",photo1);
                if(photo2 != null)
                {
                    adviceHolder.put("photo2",photo2);
                }
                adviceHolder.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(AVException e) {
                        if(e == null) {
                            Toast.makeText(RepairActivity.this, "故障信息上报成功", Toast.LENGTH_SHORT).show();
                        }
                        else{
                            Toast.makeText(RepairActivity.this, "上传失败", Toast.LENGTH_SHORT).show();
                        }

                    }
                });
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {
            case android.R.id.home:
                Intent intent = new Intent(RepairActivity.this, MainActivity.class);
                startActivity(intent);
                RepairActivity.this.finish();
                break;
        }
        return true;
    }

    private void showDialog() {

        View view = getLayoutInflater().inflate(R.layout.photo_choss_dialog, null);
        final Dialog dialog = new Dialog(this, R.style.transparentFrameWindowStyle);
        dialog.setContentView(view, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        Window window = dialog.getWindow();
        // 设置显示动画
        window.setWindowAnimations(R.style.main_menu_animstyle);
        WindowManager.LayoutParams wl = window.getAttributes();
        wl.x = 0;
        wl.y = getWindowManager().getDefaultDisplay().getHeight();
        // 以下这两句是为了保证按钮可以水平满屏
        wl.width = ViewGroup.LayoutParams.MATCH_PARENT;
        wl.height = ViewGroup.LayoutParams.WRAP_CONTENT;

        // 设置显示位置
        dialog.onWindowAttributesChanged(wl);
        // 设置点击外围解散
        dialog.setCanceledOnTouchOutside(true);
        Button imageGallery=(Button)view.findViewById(R.id.image_gallery);
        Button takePhoto=(Button)view.findViewById(R.id.take_photo);
        Button choseCancel=(Button)view.findViewById(R.id.chose_cancel);
        imageGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(ContextCompat.checkSelfPermission(RepairActivity.this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.
                        PERMISSION_GRANTED)
                {
                    ActivityCompat.requestPermissions(RepairActivity.this,new String[]{
                            Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
                }
                else
                {
                    openAlbum();
                }
                dialog.cancel();
            }
        });
        takePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                File file = new File(Environment.getExternalStorageDirectory().getPath()+"/wood/head/");
                //是否是文件夹，不是就创建文件夹
                if (!file.exists()) file.mkdirs();
                //指定保存路径
                String cameraPath = Environment.getExternalStorageDirectory().getPath()+"/wood/head/" +
                        "output_image.jpg";
                File imageFile = new File(cameraPath);
                try{
                    if(imageFile.exists()){
                        imageFile.delete();
                    }
                    imageFile.createNewFile();
                }
                catch (IOException e){
                    e.printStackTrace();
                }
                //创建一个图片保存的Uri
                imageUri = Uri.fromFile(imageFile);
                //启动相机程序
                Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
                intent.putExtra(MediaStore.EXTRA_OUTPUT,imageUri);
                startActivityForResult(intent,TAKE_PHOTO);
                dialog.cancel();

            }
        });
        choseCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.cancel();
            }
        });
        dialog.show();
    }

    private void openAlbum()
    {
        Intent open_album=new Intent("android.intent.action.GET_CONTENT");
        open_album.setType("image/*");
        startActivityForResult(open_album,CHOOSE_PHOTO);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode)
        {
            case 1:
                if(grantResults.length>0&&grantResults[0]==PackageManager.PERMISSION_GRANTED)
                {
                    openAlbum();
                }
                else
                {
                    Toast.makeText(this,"You denied the permission",Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;
        }
    }

    public void isSubmitBtnClickable()
    {
        if(!advice.getText().toString().equals(""))
        {
            isTextExist = true;
            if(isTextExist == true && isPhotoExist == true)
            {
                submitBtn.setEnabled(true);
                submitBtn.setClickable(true);
            }
            else
            {
                submitBtn.setEnabled(false);
                submitBtn.setClickable(false);
            }
        }
        else
        {
            isTextExist = false;
            submitBtn.setEnabled(false);
            submitBtn.setClickable(false);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode)
        {
            case TAKE_PHOTO:
                if(resultCode==RESULT_OK)
                {
                    try
                    {
                        //将拍摄的照片显示出来
                        Bitmap bitmap= BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri));
                        String imgPath = getFileAbsolutePath(RepairActivity.this,imageUri);

                        if (isPhoto1)
                        {
                            try {
                                photo1 = AVFile.withAbsoluteLocalPath(getPicNameFromPath(imgPath),imgPath);
                            }
                            catch(FileNotFoundException e){
                                e.printStackTrace();
                            }
                        }
                        else
                        {
                            try {
                                photo2 = AVFile.withAbsoluteLocalPath(getPicNameFromPath(imgPath),imgPath);
                            }
                            catch(FileNotFoundException e){
                                e.printStackTrace();
                            }
                        }



                        if(isPhoto1)
                        {
                            advice_photo1.setImageBitmap(bitmap);
                            clear_photo1.setVisibility(View.VISIBLE);
                            advice_photo2.setVisibility(View.VISIBLE);
                        }
                        else
                        {
                            advice_photo2.setImageBitmap(bitmap);
                            clear_photo2.setVisibility(View.VISIBLE);
                            isPhoto2 = true;
                        }
                        isPhotoExist = true;
                        if(isPhotoExist ==true && isTextExist == true)
                        {
                            submitBtn.setEnabled(true);
                            submitBtn.setClickable(true);
                        }
                    }
                    catch (FileNotFoundException e)
                    {
                        e.printStackTrace();
                    }
                }
                break;
            case CHOOSE_PHOTO:
                if(resultCode==RESULT_OK) {
                    //判断手机系统版本号
                    if (Build.VERSION.SDK_INT >= 19) {
                        //4.4及以上系统使用以下方法
                        handleImageOnKitKat(data);
                    } else {
                        //4.4及以下系统使用以下方法
                        handleImageBeforeKitKat(data);
                    }
                }
                break;
            default:
                break;
        }}

    @TargetApi(19)
    private void handleImageOnKitKat(Intent data)
    {
        String imagePath=null;
        Uri uri=data.getData();
        if(DocumentsContract.isDocumentUri(this,uri))
        {
            //如果是document类型的Uri,则通过document id处理
            String docId=DocumentsContract.getDocumentId(uri);
            if("com.android.providers.media.documents".equals(uri.getAuthority()))
            {
                String id=docId.split(":")[1];//解析出数字格式ID
                String selection=MediaStore.Images.Media._ID+"="+id;
                imagePath=getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,selection);
            }
            else if("com.android.providers.downloads.documents".equals(uri.getAuthority()))
            {
                Uri contentUri= ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"),Long.valueOf(docId));
                imagePath=getImagePath(contentUri,null);
            }
            else if("content".equalsIgnoreCase(uri.getScheme()))
            {
                //如果是content类型的Uri,则用普通方式处理
                imagePath=getImagePath(uri,null);
            }
            else if("file".equalsIgnoreCase(uri.getScheme()))
            {
                //如果是file类型的Uri,直接获取图片路径即可
                imagePath=uri.getPath();
            }
        }
        Log.d("ImagePath","选择了文件："+imagePath);
        displayImage(imagePath);
    }

    private void handleImageBeforeKitKat(Intent data)
    {
        Uri uri=data.getData();
        String imagePath=getImagePath(uri,null);
        displayImage(imagePath);
    }

    private String getImagePath(Uri uri,String selection)
    {
        String path=null;
        //通过Uri和selection来获取真实的图片路径
        Cursor cursor=getContentResolver().query(uri,null,selection,null,null);
        if(cursor!=null)
        {
            if(cursor.moveToFirst())
            {
                path=cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            }
            cursor.close();
        }
        return path;
    }

    private void displayImage(String imagePath)
    {
        if(imagePath!=null)
        {
            Log.d("ImagePath","选择了文件："+imagePath);
            Bitmap bitmap=BitmapFactory.decodeFile(imagePath);
            if(isPhoto1)
            {
                advice_photo1.setImageBitmap(bitmap);
                clear_photo1.setVisibility(View.VISIBLE);
                advice_photo2.setVisibility(View.VISIBLE);
            }
            else
            {
                advice_photo2.setImageBitmap(bitmap);
                clear_photo2.setVisibility(View.VISIBLE);
                isPhoto2 = true;
            }
            isPhotoExist = true;
            if(isPhotoExist ==true && isTextExist == true)
            {
                submitBtn.setEnabled(true);
                submitBtn.setClickable(true);
            }
            String imageName;
            imageName=getPicNameFromPath(imagePath);
            if(isPhoto1)
            {
                try {
                    photo1 = AVFile.withAbsoluteLocalPath(imageName,imagePath);
                }
                catch(FileNotFoundException e){
                    e.printStackTrace();

                }
            }
            else
            {
                try {
                    photo2 = AVFile.withAbsoluteLocalPath(imageName,imagePath);
                }
                catch(FileNotFoundException e){
                    e.printStackTrace();

                }
            }


    }}

    private String getFileName(Uri fileUri)
    {
        String[] filePathSplit = fileUri.getPath().split(":");
        String mFilePath;
        if (filePathSplit.length == 2) {
            //高版本系统内置文件选择器，只能从uri中得到相对路径，形式为：primary：文件相对路径
            String relativeFilePath = null;
            relativeFilePath = filePathSplit[1];
            mFilePath = Environment.getExternalStorageDirectory() + "/" + relativeFilePath;
        } else {
            //低版本系统文件选择器，可以直接从uri中得到绝对路径
            mFilePath = fileUri.getPath();
        }
        String mFileName;
        String[] folders = mFilePath.split("/");
        mFileName = folders[folders.length-1];

        Log.d("saved", "选择了文件:" + mFilePath);
        return mFileName;
    }

    public static String getPicNameFromPath(String picturePath){
        String temp[] = picturePath.replaceAll("\\\\","/").split("/");
        String fileName = "";
        if(temp.length > 1){
            fileName = temp[temp.length - 1];
        }
        return fileName;
    }


    //通过uri获取文件绝对路径
    @TargetApi(19)
    public static String getFileAbsolutePath(Activity context, Uri fileUri) {
        if (context == null || fileUri == null)
            return null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT && DocumentsContract.isDocumentUri(context, fileUri)) {
            if (isExternalStorageDocument(fileUri)) {
                String docId = DocumentsContract.getDocumentId(fileUri);
                String[] split = docId.split(":");
                String type = split[0];
                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }
            } else if (isDownloadsDocument(fileUri)) {
                String id = DocumentsContract.getDocumentId(fileUri);
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
                return getDataColumn(context, contentUri, null, null);
            } else if (isMediaDocument(fileUri)) {
                String docId = DocumentsContract.getDocumentId(fileUri);
                String[] split = docId.split(":");
                String type = split[0];
                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }
                String selection = MediaStore.Images.Media._ID + "=?";
                String[] selectionArgs = new String[] { split[1] };
                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        } // MediaStore (and general)
        else if ("content".equalsIgnoreCase(fileUri.getScheme())) {
            // Return the remote address
            if (isGooglePhotosUri(fileUri))
                return fileUri.getLastPathSegment();
            return getDataColumn(context, fileUri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(fileUri.getScheme())) {
            return fileUri.getPath();
        }
        return null;
    }

    //获取当前用户
    public static AVUser getCurrentUser() {
        AVUser currentUser = AVUser.getCurrentUser();
        return currentUser;
    }

    public static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        Cursor cursor = null;
        String[] projection = { MediaStore.Images.Media.DATA };
        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                int index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    /**
     * @param uri
     *            The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri
     *            The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri
     *            The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri
     *            The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }
}
