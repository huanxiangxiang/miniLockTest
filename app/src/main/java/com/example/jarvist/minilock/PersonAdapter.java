package com.example.jarvist.minilock;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by wangweiqiang on 2017/11/11.
 */

public class PersonAdapter extends ArrayAdapter<Person> {
    private int resourceId;

    public PersonAdapter(Context context, int textViewResourceId, List<Person> objects)
    {
        super(context,textViewResourceId,objects);
        resourceId=textViewResourceId;
    }

    @Override
    public View getView(int position,  View convertView, ViewGroup parent) {
        Person person=getItem(position);
        View view= LayoutInflater.from(getContext()).inflate(resourceId,parent,false);
        CircleImageView personImage=(CircleImageView)view.findViewById(R.id.personImage);
        TextView personCenter_name=(TextView)view.findViewById(R.id.personCenter_name);
        TextView personCenter_content=(TextView)view.findViewById(R.id.personCenter_content);
        if(person.getInformationContent()=="XX")
        {
            personImage.setVisibility(View.VISIBLE);
            personImage.setImageResource(person.getImageId());
            personCenter_name.setText(person.getPersonalInformation());
        }
        else
        {
            personCenter_content.setVisibility(View.VISIBLE);
            personCenter_content.setText(person.getInformationContent());
            personCenter_name.setText(person.getPersonalInformation());
        }
        return view;

    }
}
