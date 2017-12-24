package com.example.jarvist.minilock;

import android.net.Uri;

/**
 * Created by wangweiqiang on 2017/11/11.
 */

public class Person {
    private String personalInformation;
    private int imageId;
    private String informationContent;
    private Uri imageUri;
    private String imagePath;

    public Person(String personalInformation,int imageId,String informationContent,String imagePath)
    {
        this.imageId=imageId;
        this.informationContent=informationContent;
        this.personalInformation=personalInformation;
        this.imageUri = null;
        this.imagePath = imagePath;
    }

    public Uri getImageUri()
    {
        return imageUri;
    }

    public String getImagePath()
    {
        return imagePath;
    }

    public int getImageId()
    {
        return imageId;
    }

    public void setImageUri(Uri imageUri)
    {
        this.imageUri = imageUri;
        this.imagePath = "";
    }

    public void setImagePath(String imagePath)
    {
        this.imagePath = imagePath;
        this.imageUri = null;
    }

    public String getPersonalInformation()
    {
        return personalInformation;
    }

    public String getInformationContent()
    {
        return informationContent;
    }

    public void setInformationContent(String arg)
    {
        informationContent = arg;
    }


}
