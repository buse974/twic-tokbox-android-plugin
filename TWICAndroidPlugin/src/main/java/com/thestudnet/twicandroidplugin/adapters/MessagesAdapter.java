package com.thestudnet.twicandroidplugin.adapters;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.thestudnet.twicandroidplugin.R;
import com.thestudnet.twicandroidplugin.libs.CustomAdapter;
import com.thestudnet.twicandroidplugin.managers.SettingsManager;
import com.thestudnet.twicandroidplugin.managers.UserManager;
import com.thestudnet.twicandroidplugin.models.GenericModel;

import org.json.JSONObject;

import java.util.ArrayList;

/**
 * INTERACTIVE LAYER
 * Created by Baptiste PHILIBERT on 02/04/2014.
 */
public class MessagesAdapter extends CustomAdapter {

    private ArrayList<? extends GenericModel> originalValues;

    public MessagesAdapter(Context context, ArrayList<? extends GenericModel> list) {
        super(context, list);
        this.originalValues = list;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = inflateIfRequired(convertView, position, parent);
        bind(this.values.get(position), convertView);
        return convertView;
    }

    private void bind(GenericModel message, View view) {
        String type = message.getContentValue("type");
        if(!"local".equals(type)) {
            JSONObject user = UserManager.getInstance().getSettingsForKey(message.getContentValue("user_id"));
            if(user != null) {
                ViewHolder holder = (ViewHolder) view.getTag();
                holder.message_user.setText(user.optString(UserManager.USER_FIRSTNAMEKEY) + " " + user.optString(UserManager.USER_LASTNAMEKEY));
                holder.message_content.setText(message.getContentValue("text"));
                JSONObject dsmSettings = SettingsManager.getInstance().getSettingsForKey(SettingsManager.SETTINGS_DMSKEY);
                if(dsmSettings != null) {
                    JSONObject pathKeySettings = dsmSettings.optJSONObject(SettingsManager.SETTINGS_PATHSKEY);
                    if(pathKeySettings != null) {
                        String url = dsmSettings.optString(SettingsManager.SETTINGS_PROTOCOLKEY, "")
                                + "://"
                                + dsmSettings.optString(SettingsManager.SETTINGS_DOMAINKEY, "")
                                + "/"
                                + pathKeySettings.optString("datas", "")
                                + "/"
                                + user.optString(UserManager.USER_AVATARKEY, "");
                        Glide.with(this.context).load(url).error(R.drawable.users).centerCrop().into(holder.user_avatar_image);
                    }
                }
                if(message.getContentValue("user_id").equals(UserManager.getInstance().getCurrentUserId())) {
                    holder.message_user.setTextColor(ContextCompat.getColor(super.context, R.color.message_white));
                    holder.message_content.setTextColor(ContextCompat.getColor(super.context, R.color.message_white));
                    holder.message_container.setBackgroundResource(R.drawable.bg_message_me);

                }
                else {
                    holder.message_user.setTextColor(ContextCompat.getColor(super.context, R.color.message));
                    holder.message_content.setTextColor(ContextCompat.getColor(super.context, R.color.message));
                    holder.message_container.setBackgroundResource(R.drawable.bg_message);
                }
            }
        }
        else {
            ViewHolder holder = (ViewHolder) view.getTag();
            holder.message_user.setText(this.context.getString(R.string.message_user_bot));
            holder.message_content.setText(message.getContentValue("text"));
            if(!"".equals(message.getContentValue("user_id"))) {
                JSONObject user = UserManager.getInstance().getSettingsForKey(message.getContentValue("user_id"));
                if(user != null) {
                    JSONObject dsmSettings = SettingsManager.getInstance().getSettingsForKey(SettingsManager.SETTINGS_DMSKEY);
                    if(dsmSettings != null) {
                        JSONObject pathKeySettings = dsmSettings.optJSONObject(SettingsManager.SETTINGS_PATHSKEY);
                        if(pathKeySettings != null) {
                            String url = dsmSettings.optString(SettingsManager.SETTINGS_PROTOCOLKEY, "")
                                    + "://"
                                    + dsmSettings.optString(SettingsManager.SETTINGS_DOMAINKEY, "")
                                    + "/"
                                    + pathKeySettings.optString("datas", "")
                                    + "/"
                                    + user.optString(UserManager.USER_AVATARKEY, "");
                            Glide.with(this.context).load(url).error(R.drawable.users).centerCrop().into(holder.user_avatar_image);
                        }
                    }
                }
            }
            else {
                Glide.with(this.context).load(R.drawable.users).centerCrop().into(holder.user_avatar_image);
            }
            holder.message_user.setTextColor(ContextCompat.getColor(super.context, R.color.message));
            holder.message_content.setTextColor(ContextCompat.getColor(super.context, R.color.message));
            holder.message_container.setBackgroundResource(R.drawable.bg_message);
        }
    }

    private View inflateIfRequired(View view, int position, ViewGroup parent) {
        GenericModel item = this.values.get(position);
        if(view == null) {
            view = this.inflate(view, item, parent);
            view.setTag(new ViewHolder(view));
        }
        return view;
    }

    static class ViewHolder {
//        final ImageView image;
        final RelativeLayout message_container;
        final TextView message_user;
        final TextView message_content;
        final ImageView user_avatar_image;

        ViewHolder(View view) {
//            image = (ImageView) view.findViewById(R.id.image);
            message_container = (RelativeLayout) view.findViewById(R.id.message_container);
            message_user = (TextView) view.findViewById(R.id.message_user);
            message_content = (TextView) view.findViewById(R.id.message_content);
            user_avatar_image = (ImageView) view.findViewById(R.id.user_avatar_image);
        }
    }

    private View inflate(View view, GenericModel menu, ViewGroup parent) {
        view = LayoutInflater.from(this.context).inflate(R.layout.message, parent, false);
        return view;
    }

}
