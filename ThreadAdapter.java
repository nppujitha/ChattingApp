package com.example.messaging.chattingapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

 class ThreadAdapter extends ArrayAdapter<ThreadResponse.MessageThread> {
     private Context ctx;
    private ThreadResponse.MessageThread messageThread;
    private DataUpdateAfterDelete dataUpdateAfterDelete;
    ArrayList<ThreadResponse.MessageThread> messageThreadObjects;
     private  final  String TAG="demothreadadapter";

    ThreadAdapter(@NonNull Context context, int resource, @NonNull List<ThreadResponse.MessageThread> objects, ThreadActivity threadsActivity) {
        super(context, resource,objects);
        this.ctx=context;
        this.dataUpdateAfterDelete=threadsActivity;
        this.messageThreadObjects= (ArrayList<ThreadResponse.MessageThread>) objects;
    }

     @NonNull
     @Override
     public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
         messageThread = getItem(position);
         ViewHolder viewHolder;
         if(convertView==null) {
             convertView = LayoutInflater.from(getContext()).inflate(com.example.messaging.chattingapp.R.layout.thread_listview, parent, false);
             viewHolder = new ViewHolder();
             viewHolder.threadTitleTV = convertView.findViewById(com.example.messaging.chattingapp.R.id.threadTitleTV);
             viewHolder.deleteThreadButton=convertView.findViewById(com.example.messaging.chattingapp.R.id.deleteThreadButton);
             convertView.setTag(viewHolder);
         }else{
             viewHolder = (ViewHolder) convertView.getTag();
         }
         viewHolder.threadTitleTV.setText(messageThread.title);
         if(!messageThread.user_id.equals(getUserId())){
             viewHolder.deleteThreadButton.setVisibility(View.INVISIBLE);
         }else{
             viewHolder.deleteThreadButton.setVisibility(View.VISIBLE);
         }
         viewHolder.deleteThreadButton.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 Log.d(TAG, "deleteThreadButtonOnClick: "+messageThreadObjects.get(position).id);
                 dataUpdateAfterDelete.deleteThread(getToken(),messageThreadObjects.get(position).id);
             }
         });
        return convertView;
     }

     private static class ViewHolder{
         TextView threadTitleTV;
         ImageButton deleteThreadButton;
     }

     private String getUserId(){
         SharedPreferences sharedPreferences = ctx.getSharedPreferences(ctx.getString(com.example.messaging.chattingapp.R.string.LOGIN_PREF_FILE), Context.MODE_PRIVATE);
         return sharedPreferences.getString("user_id", "");
     }

     private String getToken() {
         SharedPreferences sharedPreferences = ctx.getSharedPreferences(ctx.getString(com.example.messaging.chattingapp.R.string.LOGIN_PREF_FILE), Context.MODE_PRIVATE);
         return sharedPreferences.getString("token", "");
     }

     public interface DataUpdateAfterDelete{
         void deleteThread(String token,String thread_id);
     }

 }
