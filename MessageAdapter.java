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

import org.ocpsoft.prettytime.PrettyTime;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

class MessageAdapter extends ArrayAdapter<MessagesListResponse.Message> {

    private Context ctx;
    private final String TAG = "demoMessageAdapter";
    private MessageAdapter.DataUpdateAfterMessageDelete dataUpdateAfterMessageDelete;
    private MessagesListResponse.Message message;
    private ArrayList<MessagesListResponse.Message> messageObjects;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.US);
    PrettyTime p = new PrettyTime();
    private static final long HOUR = 3600*1000;
    private Date convertedDate, newConvertedDate;

    MessageAdapter(@NonNull Context context, int resource, @NonNull List<MessagesListResponse.Message> objects, ChattingActivity chattingActivity) {
        super(context, resource, objects);
        this.ctx = context;
        this.messageObjects = (ArrayList<MessagesListResponse.Message>) objects;
        this.dataUpdateAfterMessageDelete = chattingActivity;
        dateFormat.setTimeZone(TimeZone.getDefault());
    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        message = getItem(position);
        MessageAdapter.ViewHolder viewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(com.example.messaging.chattingapp.R.layout.msgs_listview, parent, false);
            viewHolder = new MessageAdapter.ViewHolder();
            viewHolder.messageTV = convertView.findViewById(com.example.messaging.chattingapp.R.id.messageTV);
            viewHolder.creatorNameTV = convertView.findViewById(com.example.messaging.chattingapp.R.id.creatorNameTV);
            viewHolder.createdTimeTV = convertView.findViewById(com.example.messaging.chattingapp.R.id.createdTimeTV);
            viewHolder.deleteMessageButton = convertView.findViewById(com.example.messaging.chattingapp.R.id.deleteMessageButton);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (MessageAdapter.ViewHolder) convertView.getTag();
        }

        if (!message.user_id.equals(getUserId())) {
            viewHolder.deleteMessageButton.setVisibility(View.INVISIBLE);
        } else {
            viewHolder.deleteMessageButton.setVisibility(View.VISIBLE);
        }
        viewHolder.creatorNameTV.setText(String.format("%s %s", message.user_fname, message.user_lname));
        viewHolder.messageTV.setText(message.message);
        try {
            convertedDate = dateFormat.parse(message.created_at);
            newConvertedDate=new Date(convertedDate.getTime()-4*HOUR);
            p.setReference(new Date());
            //changeTimeZone();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        viewHolder.createdTimeTV.setText(p.format(newConvertedDate));
        viewHolder.deleteMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "deleteThreadButtonOnClick: " + messageObjects.get(position).id);
                dataUpdateAfterMessageDelete.deleteMessage(getToken(), messageObjects.get(position).id);
            }
        });

        return convertView;
    }

    private String getUserId() {
        SharedPreferences sharedPreferences = ctx.getSharedPreferences(ctx.getString(com.example.messaging.chattingapp.R.string.LOGIN_PREF_FILE), Context.MODE_PRIVATE);
        return sharedPreferences.getString("user_id", "");
    }

    private String getToken() {
        SharedPreferences sharedPreferences = ctx.getSharedPreferences(ctx.getString(com.example.messaging.chattingapp.R.string.LOGIN_PREF_FILE), Context.MODE_PRIVATE);
        return sharedPreferences.getString("token", "");
    }

    private static class ViewHolder {
        TextView messageTV, creatorNameTV, createdTimeTV;
        ImageButton deleteMessageButton;
    }

    public interface DataUpdateAfterMessageDelete {
        void deleteMessage(String token, String message_id);
    }

    private void changeTimeZone(){
        Calendar cal = new GregorianCalendar();
        cal.setTimeZone(TimeZone.getTimeZone("America/New_York"));
        cal.setTime(convertedDate);
        Log.d(TAG, "getView: " + message.created_at + "|" + convertedDate+"|"+cal.getTime()+" "+cal.getTimeZone());
    }
}
