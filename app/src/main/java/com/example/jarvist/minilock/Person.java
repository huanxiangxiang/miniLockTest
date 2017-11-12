package com.example.jarvist.minilock;

/**
 * Created by wangweiqiang on 2017/11/11.
 */

public class Person {
    private String personalInformation;
    private int imageId;
    private String informationContent;

    public Person(String personalInformation,int imageId,String informationContent)
    {
        this.imageId=imageId;
        this.informationContent=informationContent;
        this.personalInformation=personalInformation;
    }

    public int getImageId()
    {
        return imageId;
    }

    public String getPersonalInformation()
    {
        return personalInformation;
    }

    public String getInformationContent()
    {
        return informationContent;
    }


}
