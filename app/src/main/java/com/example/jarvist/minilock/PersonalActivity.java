package com.example.jarvist.minilock;

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
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;



import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVFile;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.DeleteCallback;
import com.avos.avoscloud.GetCallback;
import com.avos.avoscloud.GetDataCallback;
import com.avos.avoscloud.ProgressCallback;
import com.avos.avoscloud.SaveCallback;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class PersonalActivity extends AppCompatActivity {

    //private CardView cardView;
    private List<Person> personList=new ArrayList<>();
    public static final int TAKE_PHOTO=1;
    public static final int CHOOSE_PHOTO=2;
    private CircleImageView circleImage;
    private Uri imageUri;
    private ListView listView;
    private AVFile file;
    private AVUser currentUser;
    private String url1;
    private String authorityId;
    private TextView authorityShow;
    private String[] AdministratorId={"2015212151","2015212150","2015212153","2015212154","2015212152"};
    PersonAdapter personAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.personcenter_listview);
        //cardView = (CardView)findViewById(R.id.card_view);
        //cardView.setRadius(30);//设置图片圆角的半径大小
        //cardView.setCardElevation(8);//设置阴影部分大小
        //cardView.setContentPadding(5,5,5,5);//设置图片距离阴影大小

        Toolbar toolbar=(Toolbar)findViewById(R.id.toolbar);
        toolbar.setTitle("个人中心");
        setSupportActionBar(toolbar);
        currentUser=getCurrentUser();
        authorityId=currentUser.getString("authority");
        Toast.makeText(PersonalActivity.this,authorityId,Toast.LENGTH_SHORT).show();
        //Toast.makeText(PersonalActivity.this,authorityId,Toast.LENGTH_SHORT).show();
        toolbar.setNavigationIcon(R.drawable.back_button);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent goToMainActivity=new Intent(PersonalActivity.this,MainActivity.class);
                startActivity(goToMainActivity);
                PersonalActivity.this.finish();
            }
        });
        initPersons(authorityId);
        personAdapter = new PersonAdapter(PersonalActivity.this,R.layout.personal_center_item,personList);
        listView=(ListView)findViewById(R.id.list_view);
        listView.setAdapter(personAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Person person=personList.get(position);
                switch (position)
                {
                    case 0:
                        circleImage=(CircleImageView) listView.getChildAt(0).findViewById(R.id.personImage);
                        //circleImage.setImageResource(R.drawable.lock);
                        showDialog(circleImage);
                        break;
                    case 1:
                        authorityShow=(TextView)listView.getChildAt(1).findViewById(R.id.personCenter_content);
                        showAuthority();
                        break;
                }
            }
        });

        if(currentUser.getString("ImageId")!=null)
        {
            AVQuery<AVObject> query = new AVQuery<>("_File");
            query.getInBackground(currentUser.getString("ImageId"), new GetCallback<AVObject>() {
                @Override
                public void done(AVObject avObject, AVException e) {
                    if (e == null) {
                        Log.d("saved", "文件找到了");
                        AVFile file = new AVFile("test.txt", avObject.getString("url"), new HashMap<String, Object>());
                        file.getDataInBackground(new GetDataCallback() {
                            @Override
                            public void done(byte[] bytes, AVException e) {
                                if (e == null) {
                                    Log.d("saved", "文件大小" + bytes.length);
                                } else {
                                    Log.d("saved", "出错了" + e.getMessage());
                                }
                                File downloadedFile = new File(Environment.getExternalStorageDirectory() + "/test.png");
                                FileOutputStream fout = null;
                                try {
                                    fout = new FileOutputStream(downloadedFile);
                                    fout.write(bytes);
                                    Log.d("saved", "文件写入成功.");
                                    fout.close();
                                } catch (FileNotFoundException e1) {
                                    e1.printStackTrace();
                                    Log.d("saved", "文件找不到.." + e1.getMessage());
                                } catch (IOException e1) {
                                    Log.d("saved", "文件读取异常.");
                                }
                                personList.get(0).setImagePath(downloadedFile.getAbsolutePath());
                                personAdapter.notifyDataSetChanged();
                            }
                        }, new ProgressCallback() {
                            @Override
                            public void done(Integer integer) {
                                Log.d("saved", "文件下载进度" + integer);
                            }
                        });
                    } else {
                        Log.d("saved", "出错了" + e.getMessage());
                    }
                }
            });
        }

    }

    private void initPersons(String authorityId)
    {
        Person photo = new Person("头像", R.drawable.account, "XX","");
        personList.add(photo);
        Person authority=new Person("权限",R.drawable.account,authorityId,"");
        personList.add(authority);
    }

    private void showDialog(CircleImageView circleImageView) {

        View view = getLayoutInflater().inflate(R.layout.photo_choss_dialog, null);
        final Dialog dialog = new Dialog(this, R.style.transparentFrameWindowStyle);
        dialog.setContentView(view, new LayoutParams(LayoutParams.FILL_PARENT,
                LayoutParams.WRAP_CONTENT));
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
                if(ContextCompat.checkSelfPermission(PersonalActivity.this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.
                        PERMISSION_GRANTED)
                {
                    ActivityCompat.requestPermissions(PersonalActivity.this,new String[]{
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
                /*File outputImage=new File(getExternalCacheDir(),"output_image.jpg");
                try{
                    if(outputImage.exists()){
                        outputImage.delete();
                    }
                    outputImage.createNewFile();
                }
                catch (IOException e){
                    e.printStackTrace();
                }
                if(Build.VERSION.SDK_INT>=24)
                {
                    imageUri= FileProvider.getUriForFile(PersonalActivity.this,
                            "com.example.cameraalbumtest.fileprovider",outputImage);
                }
                else
                {
                    imageUri= Uri.fromFile(outputImage);
                }*/

                //启动相机程序
                Intent intent=new Intent("android.media.action.IMAGE_CAPTURE");
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

    private void showAuthority()
    {
        final EditText editText = new EditText(PersonalActivity.this);
        final AlertDialog inputDialog = new AlertDialog.Builder(PersonalActivity.this)
                .setTitle("请输入管理员ID:")
                .setView(editText)
                .setPositiveButton("确定", null)
                .setNegativeButton("取消",null)
                .create();
        inputDialog.show();
        inputDialog.getButton(inputDialog.BUTTON_POSITIVE).setTextColor(Color.BLACK);
        inputDialog.getButton(inputDialog.BUTTON_NEGATIVE).setTextColor(Color.BLACK);
        inputDialog.getButton(inputDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String Id1=editText.getText().toString();
                if (Id1.equals("")) {
                    Toast.makeText(PersonalActivity.this, "管理员ID不能为空"+AdministratorId[0]+AdministratorId[1],
                            Toast.LENGTH_SHORT).show();
                } else {
                    int j=0;
                    for (j = 0; j < AdministratorId.length; j++) {
                        if (Id1.equals(AdministratorId[j])) {
                            changeAuthority("管理员");
                            currentUser.put("authority", "管理员");
                            currentUser.saveInBackground();
                            Toast.makeText(PersonalActivity.this, "修改成功", Toast.LENGTH_SHORT).show();
                            inputDialog.cancel();
                            break;
                        }
                    }
                    if(j==AdministratorId.length)
                    Toast.makeText(PersonalActivity.this, "管理员ID有误,请重新输入", Toast.LENGTH_SHORT).show();
                }
            }
        });
        inputDialog.getButton(inputDialog.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                inputDialog.cancel();
            }
        });
    }

    private void changeAuthority(String arg)
    {
        personList.get(1).setInformationContent(arg);
        personAdapter.notifyDataSetChanged();
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
                        String imgPath = getFileAbsolutePath(PersonalActivity.this,imageUri);
                        try {
                            if(currentUser.getString("ImageId")==null)
                                file = AVFile.withAbsoluteLocalPath(getPicNameFromPath(imgPath),imgPath);
                            else
                            {
                                AVQuery<AVObject> query = new AVQuery<>("_File");
                                query.whereEqualTo("objectId",currentUser.getString("ImageId"));
                                query.deleteAllInBackground(new DeleteCallback() {
                                    @Override
                                    public void done(AVException e) {

                                    }
                                });
                                file = AVFile.withAbsoluteLocalPath(getPicNameFromPath(imgPath),imgPath);
                            }
                        }
                        catch(FileNotFoundException e){
                            e.printStackTrace();

                        }

                        file.saveInBackground(new SaveCallback() {
                            @Override
                            public void done(AVException e) {
                                if (e == null) {
                                    currentUser.put("ImageId",file.getObjectId());
                                    currentUser.saveInBackground();
                                    Toast.makeText(PersonalActivity.this, "上传成功",Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(PersonalActivity.this,"上传失败",Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                        circleImage.setImageBitmap(bitmap);
                        personList.get(0).setImageUri(imageUri);
                        personAdapter.notifyDataSetChanged();
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
            circleImage.setImageBitmap(bitmap);
            personList.get(0).setImagePath(imagePath);
            personAdapter.notifyDataSetChanged();
            String imageName;
            imageName=getPicNameFromPath(imagePath);
            //Toast.makeText(this,"imageName is "+imageName,Toast.LENGTH_LONG).show();
            try {
                if(currentUser.getString("ImageId")==null)
                    file = AVFile.withAbsoluteLocalPath(imageName,imagePath);
                else
                {
                    AVQuery<AVObject> query = new AVQuery<>("_File");
                    query.whereEqualTo("objectId",currentUser.getString("ImageId"));
                    query.deleteAllInBackground(new DeleteCallback() {
                        @Override
                        public void done(AVException e) {

                        }
                    });
                    file = AVFile.withAbsoluteLocalPath(imageName,imagePath);
                }
            }
           catch(FileNotFoundException e){
                e.printStackTrace();

        }

        file.saveInBackground(new SaveCallback() {
            @Override
            public void done(AVException e) {
                if (e == null) {
                        currentUser.put("ImageId",file.getObjectId());
                        currentUser.saveInBackground();
                    Toast.makeText(PersonalActivity.this, "上传成功",Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(PersonalActivity.this,"上传失败",Toast.LENGTH_SHORT).show();
                }
            }
        });
        }

        else
        {
            Toast.makeText(this,"failed to get image",Toast.LENGTH_SHORT).show();
        }
    }

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

    //获取当前用户
    public static AVUser getCurrentUser() {
        AVUser currentUser = AVUser.getCurrentUser();
        return currentUser;
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
