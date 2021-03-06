package com.thestudnet.twicandroidplugin.managers;

import android.content.ContentValues;
import android.util.Log;

import com.opentok.android.Connection;
import com.thestudnet.twicandroidplugin.communication.APIClientConfigurator;
import com.thestudnet.twicandroidplugin.events.APIInteraction;
import com.thestudnet.twicandroidplugin.events.MessageInteraction;
import com.thestudnet.twicandroidplugin.models.GenericModel;
import com.thestudnet.twicandroidplugin.utils.DateUtils;
import com.thestudnet.twicandroidplugin.utils.RandomInt;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Request;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;
import com.thetransactioncompany.jsonrpc2.client.JSONRPC2Session;
import com.thetransactioncompany.jsonrpc2.client.JSONRPC2SessionException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * INTERACTIVE LAYER
 * Created by Baptiste PHILIBERT on 27/04/2017.
 */

public class APIClient {

    private static final String TAG = "com.thestudnet.twicandroidplugin.managers." + APIClient.class.getSimpleName();

    public static String TWIC_CONVERSATION_GETPATH          = "conversation.get";
    public static String TWIC_CONVERSATION_GETTOKENPATH     = "conversation.getToken";
    public static String TWIC_USER_GETPATH                  = "user.get";
    public static String TWIC_ACTIVITY_ADDPATH              = "activity.add";
    public static String TWIC_STARTARCHIVEPATH              = "videoarchive.startRecord";
    public static String TWIC_STOPARCHIVEPATH               = "videoarchive.stopRecord";
    public static String TWIC_MESSAGELISTPATH               = "message.getList";
    public static String TWIC_CONVERSATIONREADPATH          = "conversation.read";
    public static String TWIC_MESSAGESENDPATH               = "message.send";
    
    public static String HANGOUT_EVENT_JOIN                 = "hangout.join";
    public static String HANGOUT_EVENT_LEAVE                = "hangout.leave";
    public static String HANGOUT_EVENT_USERSPOKE            = "hangout.userspoke";
    public static String HANGOUT_EVENT_SHARECAMERA          = "hangout.sharecamera";
    public static String HANGOUT_EVENT_SHAREMICROPHONE      = "hangout.sharemicrophone";
    public static String HANGOUT_EVENT_MESSAGE              = "hangout.message";
    public static String HANGOUT_EVENT_STARTRECORD          = "hangout.startrecord";
    public static String HANGOUT_EVENT_STOPRECORD           = "hangout.stoprecord";
    public static String HANGOUT_EVENT_LAUNCH_USERCAMERA    = "hangout.launchusercamera";
    public static String HANGOUT_EVENT_LAUNCH_USERMICROPHONE= "hangout.launchusermicrophone";
    public static String HANGOUT_EVENT_LAUNC_HUSERSCREEN    = "hangout.launchuserscreen";
    public static String HANGOUT_EVENT_ASK_MICROPHONE_AUTH  = "hangout.ask_microphone_auth";
    public static String HANGOUT_EVENT_ASK_CAMERA_AUTH      = "hangout.ask_camera_auth";
    public static String HANGOUT_EVENT_KICK_USER            = "hangout.kickuser";

    private JSONRPC2Session client;

    private static APIClient instance;
    public APIClient() {
    }
    public static APIClient getInstance() {
        if(instance == null) {
            APIClient minstance = new APIClient();
            instance = minstance;
            // The JSON-RPC 2.0 server URL
            URL serverURL = null;
            try {

                JSONObject apiSettings = new JSONObject(SettingsManager.getInstance().getRawValueForKey(SettingsManager.SETTINGS_APIKEY));
                String protocol = apiSettings.optString(SettingsManager.SETTINGS_PROTOCOLKEY, "http");
                String hostname = apiSettings.optString(SettingsManager.SETTINGS_DOMAINKEY, "");
                JSONObject paths = new JSONObject(apiSettings.optString(SettingsManager.SETTINGS_PATHSKEY, ""));
                String path = paths.optString(SettingsManager.SETTINGS_PATHS_JSONRPCKEY);

                serverURL = new URL(protocol
                        + "://"
                        + hostname
                        + "/"
                        + path);

                instance.client = new JSONRPC2Session(serverURL);
                instance.client.setConnectionConfigurator(new APIClientConfigurator());
                instance.client.getOptions().trustAllCerts(true);
            }
            catch (JSONException e) {
                Log.e(TAG, e.getLocalizedMessage());
            }
            catch (MalformedURLException e) {
                Log.e(TAG, e.getLocalizedMessage());
            }
            return instance;
        } else {
            return instance;
        }
    }

    public void getHangoutData() {
        if(this.client != null) {
//            getHangoutDataTask task = new getHangoutDataTask();
//            if(Build.VERSION.SDK_INT >= 11/*HONEYCOMB*/) {
//                task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
//            }
//            else {
//                task.execute();
//            }

            new Thread() {
                public void run() {
                    // Construct request
                    int requestID = new RandomInt().nextNonNegative();
                    HashMap<String, Object> param = new HashMap<>(1);
                    //JSONObject jsonParams = new JSONObject();
                    JSONRPC2Response response = null;

                    try {
//                        jsonParams.addOrReplace("id", SettingsManager.getInstance().getRawValueForKey(SettingsManager.SETTINGS_HANGOUTIDKEY));
//                        param.addOrReplace("params", jsonParams);
                        param.put("id", SettingsManager.getInstance().getRawValueForKey(SettingsManager.SETTINGS_HANGOUTIDKEY));
                        JSONRPC2Request request = new JSONRPC2Request(TWIC_CONVERSATION_GETPATH, param, requestID);

                        // Send request
                        response = client.send(request);
                    }
//                    catch (JSONException e) {
//                        Log.e(TAG, e.getLocalizedMessage());
//                    }
                    catch (JSONRPC2SessionException e) {
                        Log.e(TAG, e.getLocalizedMessage());
                        Log.e(TAG, e.getMessage());
                    }

                    if(response != null && response.indicatesSuccess()) {
                        Log.d(TAG, response.getResult().toString());

                        HangoutManager.getInstance().configure(response.getResult().toString());

                        APIInteraction.getInstance().FireEvent(APIInteraction.Type.ON_HANGOUT_DATA_RECEIVED, null);
                    }
                }
            }.start();
        }
    }

//    private class getHangoutDataTask extends AsyncTask<String, Void, JSONRPC2Response> {
//
//        @Override
//        protected void onPreExecute() {
//            super.onPreExecute();
//            Log.e("AsyncTask", "onPreExecute");
//        }
//
//        protected JSONRPC2Response doInBackground(String... urls) {
//            // Construct request
//            int requestID = new RandomInt().nextNonNegative();
//            HashMap<String, Object> params = new HashMap<>(1);
//            params.addOrReplace("id", SettingsManager.getInstance().getRawValueForKey(SettingsManager.SETTINGS_HANGOUTIDKEY));
//            JSONRPC2Request request = new JSONRPC2Request(TWIC_CONVERSATION_GETPATH, params, requestID);
//
//            // Send request
//            JSONRPC2Response response = null;
//
//            try {
//                response = client.send(request);
//            }
//            catch (JSONRPC2SessionException e) {
//                Log.e(TAG, String.valueOf(e.getCauseType()));
//            }
//            finally {
//                return response;
//            }
//        }
//
//        protected void onPostExecute(JSONRPC2Response response) {
//            if(response != null && response.indicatesSuccess()) {
//                Log.e(TAG, response.getResult().toString());
//
//                APIInteraction.getInstance().FireEvent(APIInteraction.Type.ON_HANGOUT_DATA_RECEIVED, null);
//            }
//        }
//    }

    public void getTokBoxData() {
        if(this.client != null) {
//            getTokBoxDataTask task = new getTokBoxDataTask();
//            if(Build.VERSION.SDK_INT >= 11/*HONEYCOMB*/) {
//                task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
//            }
//            else {
//                task.execute();
//            }

            new Thread() {
                public void run() {
                    // Construct request
                    int requestID = new RandomInt().nextNonNegative();
                    HashMap<String, Object> param = new HashMap<>(1);
                    //JSONObject jsonParams = new JSONObject();
                    JSONRPC2Response response = null;

                    try {
//                        jsonParams.addOrReplace("id", SettingsManager.getInstance().getRawValueForKey(SettingsManager.SETTINGS_HANGOUTIDKEY));
//                        param.addOrReplace("params", jsonParams);
                        param.put("id", SettingsManager.getInstance().getRawValueForKey(SettingsManager.SETTINGS_HANGOUTIDKEY));
                        JSONRPC2Request request = new JSONRPC2Request(TWIC_CONVERSATION_GETTOKENPATH, param, requestID);

                        // Send request
                        response = client.send(request);
                    }
//                    catch (JSONException e) {
//                        Log.e(TAG, e.getLocalizedMessage());
//                    }
                    catch (JSONRPC2SessionException e) {
                        Log.e(TAG, e.getLocalizedMessage());
                        Log.e(TAG, e.getMessage());
                    }

                    if(response != null && response.indicatesSuccess()) {
                        Log.d(TAG, response.getResult().toString());

                        ArrayList<GenericModel> list = new ArrayList<>(1);
                        ContentValues contentValues = new ContentValues();
                        net.minidev.json.JSONObject jsonResponse = (net.minidev.json.JSONObject) response.getResult();
                        contentValues.put("token", (String) jsonResponse.get("token"));
                        contentValues.put("session", (String) jsonResponse.get("session"));
                        list.add(new GenericModel(contentValues));

                        // Inject current user role
                        HangoutManager.getInstance().addOrReplace(HangoutManager.HANGOUT_CURRENT_USER_ROLE, (String) jsonResponse.get("role"));

                        APIInteraction.getInstance().FireEvent(APIInteraction.Type.ON_TOKBOX_DATA_RECEIVED, list);
                    }
                }
            }.start();
        }
    }

//    private class getTokBoxDataTask extends AsyncTask<String, Void, JSONRPC2Response> {
//
//        protected JSONRPC2Response doInBackground(String... urls) {
//            // Construct request
//            int requestID = new RandomInt().nextNonNegative();
//            HashMap<String, Object> params = new HashMap<>(1);
//            params.addOrReplace("id", SettingsManager.getInstance().getRawValueForKey(SettingsManager.SETTINGS_HANGOUTIDKEY));
//            JSONRPC2Request request = new JSONRPC2Request(TWIC_CONVERSATION_GETPATH, params, requestID);
//
//            // Send request
//            JSONRPC2Response response = null;
//
//            try {
//                response = client.send(request);
//            }
//            catch (JSONRPC2SessionException e) {
//                Log.e(TAG, e.getLocalizedMessage());
//            }
//            finally {
//                return response;
//            }
//        }
//
//        protected void onPostExecute(JSONRPC2Response response) {
//            if(response != null && response.indicatesSuccess()) {
//                Log.e(TAG, response.getResult().toString());
//
//                APIInteraction.getInstance().FireEvent(APIInteraction.Type.ON_TOKBOX_DATA_RECEIVED, null);
//            }
//        }
//    }

    public void getHangoutUsers() {
        if(this.client != null) {
//            getHangoutDataTask task = new getHangoutDataTask();
//            if(Build.VERSION.SDK_INT >= 11/*HONEYCOMB*/) {
//                task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
//            }
//            else {
//                task.execute();
//            }

            new Thread() {
                public void run() {
                    // Construct request
                    int requestID = new RandomInt().nextNonNegative();
                    HashMap<String, Object> param = new HashMap<>(1);
                    //JSONObject jsonParams = new JSONObject();
                    JSONRPC2Response response = null;

                    try {
//                        jsonParams.addOrReplace("id", SettingsManager.getInstance().getRawValueForKey(SettingsManager.SETTINGS_HANGOUTIDKEY));
//                        param.addOrReplace("params", jsonParams);
//                        param.addOrReplace("id", HangoutManager.getInstance().getRawValueForKey(HangoutManager.HANGOUT_USERSKEY));
                        JSONArray jsonIds = new JSONArray(HangoutManager.getInstance().getRawValueForKey(HangoutManager.HANGOUT_USERSKEY));
                        ArrayList<String> ids = new ArrayList<>(jsonIds.length());
                        JSONRPC2Request request = new JSONRPC2Request(TWIC_USER_GETPATH, param, requestID);
                        for (int i=0; i<jsonIds.length(); i++){
                            ids.add(jsonIds.getString(i));
                        }
                        param.put("id", ids);
                        // Send request
                        response = client.send(request);
                    }
//                    catch (JSONException e) {
//                        Log.e(TAG, e.getLocalizedMessage());
//                    }
                    catch (JSONRPC2SessionException e) {
                        Log.e(TAG, e.getLocalizedMessage());
                        Log.e(TAG, e.getMessage());
                    } catch (JSONException e) {
                        Log.e(TAG, e.getLocalizedMessage());
                        Log.e(TAG, e.getMessage());
                    }

                    if(response != null && response.indicatesSuccess()) {
                        Log.d(TAG, response.getResult().toString());

                        UserManager.getInstance().configure(response.getResult().toString());

                        APIInteraction.getInstance().FireEvent(APIInteraction.Type.ON_HANGOUT_USERS_RECEIVED, null);
                    }
                }
            }.start();
        }
    }

    public void getNewUsers(final String userId, final Connection relatedUserConnection) {
        if(this.client != null) {
            new Thread() {
                public void run() {
                    // Construct request
                    int requestID = new RandomInt().nextNonNegative();
                    HashMap<String, Object> param = new HashMap<>(1);
                    //JSONObject jsonParams = new JSONObject();
                    JSONRPC2Response response = null;

                    try {
                        // Prepare params
                        ArrayList<String> ids = new ArrayList<>(1);
                        ids.add(0, userId);
                        param.put("id", ids);
                        // Prepare request
                        JSONRPC2Request request = new JSONRPC2Request(TWIC_USER_GETPATH, param, requestID);
                        // Send request
                        response = client.send(request);
                    }
                    catch (JSONRPC2SessionException e) {
                        Log.e(TAG, e.getLocalizedMessage());
                        Log.e(TAG, e.getMessage());
                    }

                    if(response != null && response.indicatesSuccess()) {
                        Log.d(TAG, response.getResult().toString());

                        // Add user in users list
                        UserManager.getInstance().addOrReplace(userId, response.getResult().toString());
                        // Set user connection state to "connected"
                        UserManager.getInstance().setConnectionState(true, userId);
                        // Set user connection
                        UserManager.getInstance().addOrReplaceUserConnection(userId, relatedUserConnection);

                        APIInteraction.getInstance().FireEvent(APIInteraction.Type.ON_USER_CONNECTION_STATE_CHANGED, null);
                    }
                }
            }.start();
        }
    }

    public void getMessages() {
        if(this.client != null) {
            new Thread() {
                public void run() {
                    // Construct request
                    int requestID = new RandomInt().nextNonNegative();
                    HashMap<String, Object> param = new HashMap<>(2);
                    //JSONObject jsonParams = new JSONObject();
                    JSONRPC2Response response = null;

                    try {
                        param.put("conversation_id", SettingsManager.getInstance().getRawValueForKey(SettingsManager.SETTINGS_HANGOUTIDKEY));
                        // “filter”:{“o”:{“message_id”:“ASC”}}
//                        String filter = "{\"o\":{\"message.id\":\"DESC\"}}";
//                        String filter = "o:{\"message.id\":\"DESC\"}";
                        HashMap<String, Object> filterParams = new HashMap<String, Object>(1);
                        HashMap<String, Object> filterParamsValue = new HashMap<String, Object>(1);
                        filterParamsValue.put("message.id", "DESC");
                        filterParams.put("o", filterParamsValue);
                        param.put("filter", filterParams);
                        JSONRPC2Request request = new JSONRPC2Request(TWIC_MESSAGELISTPATH, param, requestID);

//                        String jsonString = "{\"method\":\""+TWIC_MESSAGELISTPATH+"\"," +
//                                "\"params\":{\"id\":\""+ SettingsManager.getInstance().getRawValueForKey(SettingsManager.SETTINGS_HANGOUTIDKEY) +"\",\"filter\":{\"o\":{\"message.id\":\"DESC\"}}}," +
//                                "\"id\":\""+ String.valueOf(requestID) +"\","+
//                                "\"jsonrpc\":\"2.0\"}";
//
//                        request = JSONRPC2Request.parse(jsonString);

                        // Send request
                        response = client.send(request);
                    }
//                    catch (JSONRPC2ParseException e) {
//                        Log.e(TAG, e.getLocalizedMessage());
//                        Log.e(TAG, e.getMessage());
//                    }
                    catch (JSONRPC2SessionException e) {
                        Log.e(TAG, e.getLocalizedMessage());
                        Log.e(TAG, e.getMessage());
                    }

                    if(response != null && response.indicatesSuccess()) {
                        Log.d(TAG, response.getResult().toString());

                        try {
                            JSONObject jsonResponse = new JSONObject(response.getResult().toString());
                            ArrayList<Integer> list = new ArrayList<Integer>(1);
                            list.add(MessagesManager.getInstance().insertMessages(jsonResponse.optString("list", ""), true));
                            MessageInteraction.getInstance().FireEvent(MessageInteraction.Type.ON_MESSAGES_LOADED, list);
                        } catch (JSONException e) {
                            Log.e(TAG, e.getLocalizedMessage());
                        }
                    }
                }
            }.start();
        }
    }

    public void getMessagesFromMessageId(final String messageId) {
        if(this.client != null) {
            new Thread() {
                public void run() {
                    // Construct request
                    int requestID = new RandomInt().nextNonNegative();
                    HashMap<String, Object> param = new HashMap<>(2);
                    //JSONObject jsonParams = new JSONObject();
                    JSONRPC2Response response = null;

                    try {
                        param.put("conversation_id", SettingsManager.getInstance().getRawValueForKey(SettingsManager.SETTINGS_HANGOUTIDKEY));

                        HashMap<String, Object> filterParams = new HashMap<String, Object>(1);

                        HashMap<String, Object> filterParams0Value = new HashMap<String, Object>(1);
                        filterParams0Value.put("message.id", "DESC");

                        HashMap<String, Object> filterParamsCValue = new HashMap<String, Object>(1);
                        filterParamsCValue.put("message.id", ">");

                        filterParams.put("o", filterParams0Value);
                        filterParams.put("s", messageId);
                        filterParams.put("c", filterParamsCValue);

                        param.put("filter", filterParams);

                        JSONRPC2Request request = new JSONRPC2Request(TWIC_MESSAGELISTPATH, param, requestID);

                        // Send request
                        response = client.send(request);
                    }
//                    catch (JSONRPC2ParseException e) {
//                        Log.e(TAG, e.getLocalizedMessage());
//                        Log.e(TAG, e.getMessage());
//                    }
                    catch (JSONRPC2SessionException e) {
                        Log.e(TAG, e.getLocalizedMessage());
                        Log.e(TAG, e.getMessage());
                    }

                    if(response != null && response.indicatesSuccess()) {
                        Log.d(TAG, response.getResult().toString());

                        try {
                            JSONObject jsonResponse = new JSONObject(response.getResult().toString());
                            ArrayList<Integer> list = new ArrayList<Integer>(1);
                            list.add(MessagesManager.getInstance().insertMessages(jsonResponse.optString("list", ""), true));
                            MessageInteraction.getInstance().FireEvent(MessageInteraction.Type.ON_LATEST_MESSAGES_LOADED, list);
                        } catch (JSONException e) {
                            Log.e(TAG, e.getLocalizedMessage());
                        }
                    }
                }
            }.start();
        }
    }

    public void getMessagesBeforeMessageId(final String messageId) {
        if(this.client != null) {
            new Thread() {
                public void run() {
                    // Construct request
                    int requestID = new RandomInt().nextNonNegative();
                    HashMap<String, Object> param = new HashMap<>(2);
                    //JSONObject jsonParams = new JSONObject();
                    JSONRPC2Response response = null;

                    try {
                        param.put("conversation_id", SettingsManager.getInstance().getRawValueForKey(SettingsManager.SETTINGS_HANGOUTIDKEY));

                        HashMap<String, Object> filterParams = new HashMap<String, Object>(1);

                        HashMap<String, Object> filterParams0Value = new HashMap<String, Object>(1);
                        filterParams0Value.put("message.id", "DESC");

                        HashMap<String, Object> filterParamsCValue = new HashMap<String, Object>(1);
                        filterParamsCValue.put("message.id", "<");

                        filterParams.put("o", filterParams0Value);
                        filterParams.put("s", messageId);
                        filterParams.put("c", filterParamsCValue);

                        param.put("filter", filterParams);

                        JSONRPC2Request request = new JSONRPC2Request(TWIC_MESSAGELISTPATH, param, requestID);

                        // Send request
                        response = client.send(request);
                    }
//                    catch (JSONRPC2ParseException e) {
//                        Log.e(TAG, e.getLocalizedMessage());
//                        Log.e(TAG, e.getMessage());
//                    }
                    catch (JSONRPC2SessionException e) {
                        Log.e(TAG, e.getLocalizedMessage());
                        Log.e(TAG, e.getMessage());
                    }

                    if(response != null && response.indicatesSuccess()) {
                        Log.d(TAG, response.getResult().toString());

                        try {
                            JSONObject jsonResponse = new JSONObject(response.getResult().toString());
                            ArrayList<Integer> list = new ArrayList<Integer>(1);
                            list.add(MessagesManager.getInstance().insertMessages(jsonResponse.optString("list", ""), false));
                            MessageInteraction.getInstance().FireEvent(MessageInteraction.Type.ON_HISTORICAL_MESSAGES_LOADED, list);
                        } catch (JSONException e) {
                            Log.e(TAG, e.getLocalizedMessage());
                        }
                    }
                }
            }.start();
        }
    }

    public void sendMessage(final String message) {
        if(this.client != null) {
            new Thread() {
                public void run() {
                    // Construct request
                    int requestID = new RandomInt().nextNonNegative();
                    HashMap<String, Object> param = new HashMap<>(2);
                    //JSONObject jsonParams = new JSONObject();
                    JSONRPC2Response response = null;

                    try {
                        param.put("conversation_id", SettingsManager.getInstance().getRawValueForKey(SettingsManager.SETTINGS_HANGOUTIDKEY));
                        param.put("text", message);

                        JSONRPC2Request request = new JSONRPC2Request(TWIC_MESSAGESENDPATH, param, requestID);

                        // Send request
                        response = client.send(request);
                    }
//                    catch (JSONRPC2ParseException e) {
//                        Log.e(TAG, e.getLocalizedMessage());
//                        Log.e(TAG, e.getMessage());
//                    }
                    catch (JSONRPC2SessionException e) {
                        Log.e(TAG, e.getLocalizedMessage());
                        Log.e(TAG, e.getMessage());
                    }

                    if(response != null && response.indicatesSuccess()) {
                        Log.d(TAG, response.getResult().toString());
                    }
                }
            }.start();
        }
    }

    public void sendConversationRead() {
        if(this.client != null) {
            new Thread() {
                public void run() {
                    // Construct request
                    int requestID = new RandomInt().nextNonNegative();
                    HashMap<String, Object> param = new HashMap<>(1);
                    //JSONObject jsonParams = new JSONObject();
                    JSONRPC2Response response = null;

                    try {
                        param.put("conversation_id", SettingsManager.getInstance().getRawValueForKey(SettingsManager.SETTINGS_HANGOUTIDKEY));
                        JSONRPC2Request request = new JSONRPC2Request(TWIC_CONVERSATIONREADPATH, param, requestID);
                        // Send request
                        response = client.send(request);
                    }
                    catch (JSONRPC2SessionException e) {
                        Log.e(TAG, e.getLocalizedMessage());
                        Log.e(TAG, e.getMessage());
                    }

                    if(response != null && response.indicatesSuccess()) {
                        Log.d(TAG, response.getResult().toString());
                    }
                    else if(response != null && response.getError() != null) {
                        Log.e(TAG, response.getError().toString());
                    }
                    else {
                        Log.e(TAG, "unknown error in sendConversationRead");
                    }
                }
            }.start();
        }
    }

    public void startArchiving() {
        if(this.client != null) {
            new Thread() {
                public void run() {
                    // Construct request
                    int requestID = new RandomInt().nextNonNegative();
                    HashMap<String, Object> param = new HashMap<>(1);
                    //JSONObject jsonParams = new JSONObject();
                    JSONRPC2Response response = null;

                    try {
                        param.put("conversation_id", SettingsManager.getInstance().getRawValueForKey(SettingsManager.SETTINGS_HANGOUTIDKEY));
                        JSONRPC2Request request = new JSONRPC2Request(TWIC_STARTARCHIVEPATH, param, requestID);
                        // Send request
                        response = client.send(request);
                    }
                    catch (JSONRPC2SessionException e) {
                        Log.e(TAG, e.getLocalizedMessage());
                        Log.e(TAG, e.getMessage());
                    }

                    if(response != null && response.indicatesSuccess()) {
                        Log.d(TAG, response.getResult().toString());
                    }
                    else if(response != null && response.getError() != null) {
                        Log.e(TAG, response.getError().toString());
                    }
                    else {
                        Log.e(TAG, "unknown error in startArchiving");
                    }
                }
            }.start();
        }
    }

    public void stopArchiving() {
        if(this.client != null) {
            new Thread() {
                public void run() {
                    // Construct request
                    int requestID = new RandomInt().nextNonNegative();
                    HashMap<String, Object> param = new HashMap<>(1);
                    //JSONObject jsonParams = new JSONObject();
                    JSONRPC2Response response = null;

                    try {
                        param.put("conversation_id", SettingsManager.getInstance().getRawValueForKey(SettingsManager.SETTINGS_HANGOUTIDKEY));
                        JSONRPC2Request request = new JSONRPC2Request(TWIC_STOPARCHIVEPATH, param, requestID);
                        // Send request
                        response = client.send(request);
                    }
                    catch (JSONRPC2SessionException e) {
                        Log.e(TAG, e.getLocalizedMessage());
                        Log.e(TAG, e.getMessage());
                    }

                    if(response != null && response.indicatesSuccess()) {
                        Log.d(TAG, response.getResult().toString());
                    }
                    else if(response != null && response.getError() != null) {
                        Log.e(TAG, response.getError().toString());
                    }
                    else {
                        Log.e(TAG, "unknown error in stopArchiving");
                    }
                }
            }.start();
        }
    }

    public void registerEventName(final String eventName) {
        if(this.client != null) {
            new Thread() {
                public void run() {
                    // Construct request
                    int requestID = new RandomInt().nextNonNegative();
                    HashMap<String, Object> param = new HashMap<>(1);
                    JSONRPC2Response response = null;

                    try {
                        HashMap<String, Object> root = new HashMap<String, Object>(1);
                        ArrayList rootParams = new ArrayList(1);

                        param.put("event", eventName);
                        param.put("date", DateUtils.getCurrentDateIso());

                        HashMap<String, Object> objectParams = new HashMap<String, Object>(1);

                        objectParams.put("id", SettingsManager.getInstance().getRawValueForKey(SettingsManager.SETTINGS_HANGOUTIDKEY));
                        objectParams.put("name", "hangout");

                        param.put("object", objectParams);

                        root.put("activities", rootParams);

                        rootParams.add(param);

                        JSONRPC2Request request = new JSONRPC2Request(TWIC_ACTIVITY_ADDPATH, root, requestID);


                        // Send request
                        response = client.send(request);
                    }
                    catch (JSONRPC2SessionException e) {
                        Log.e(TAG, e.getLocalizedMessage());
                        Log.e(TAG, e.getMessage());
                    }

                    if(response != null && response.indicatesSuccess()) {
                        Log.d(TAG, response.getResult().toString());
                    }
                    else if(response != null && response.getError() != null) {
                        Log.e(TAG, response.getError().toString());
                    }
                    else {
                        Log.e(TAG, "unknown error in " + eventName);
                    }
                }
            }.start();
        }
    }

//    public void sendUserJoin() {
//        if(this.client != null) {
//            new Thread() {
//                public void run() {
//                    // Construct request
//                    int requestID = new RandomInt().nextNonNegative();
//                    HashMap<String, Object> param = new HashMap<>(1);
//                    //JSONObject jsonParams = new JSONObject();
//                    JSONRPC2Response response = null;
//
//                    try {
//                        HashMap<String, Object> root = new HashMap<String, Object>(1);
//                        ArrayList rootParams = new ArrayList(1);
//
//                        param.put("event", HANGOUT_EVENT_JOIN);
//                        param.put("date", DateUtils.getCurrentDateIso());
//
//                        HashMap<String, Object> objectParams = new HashMap<String, Object>(1);
//
//                        objectParams.put("id", SettingsManager.getInstance().getRawValueForKey(SettingsManager.SETTINGS_HANGOUTIDKEY));
//                        objectParams.put("name", "hangout");
//
//                        param.put("object", objectParams);
//
//                        root.put("activities", rootParams);
//
//                        rootParams.add(param);
//
//                        JSONRPC2Request request = new JSONRPC2Request(TWIC_ACTIVITY_ADDPATH, root, requestID);
//                        // Send request
//                        response = client.send(request);
//                    }
////                    catch (JSONException e) {
////                        Log.e(TAG, e.getLocalizedMessage());
////                    }
//                    catch (JSONRPC2SessionException e) {
//                        Log.e(TAG, e.getLocalizedMessage());
//                        Log.e(TAG, e.getMessage());
//                    }
//
//                    if(response != null && response.indicatesSuccess()) {
//                        Log.d(TAG, response.getResult().toString());
//                    }
//                    else if(response != null && response.getError() != null) {
//                        Log.e(TAG, response.getError().toString());
//                    }
//                    else {
//                        Log.e(TAG, "unknown error in sendUserJoin");
//                    }
//                }
//            }.start();
//        }
//    }
//
//    public void sendUserLeave() {
//        if(this.client != null) {
//            new Thread() {
//                public void run() {
//                    // Construct request
//                    int requestID = new RandomInt().nextNonNegative();
//                    HashMap<String, Object> param = new HashMap<>(1);
//                    //JSONObject jsonParams = new JSONObject();
//                    JSONRPC2Response response = null;
//
//                    try {
//                        param.put("event", HANGOUT_EVENT_LEAVE);
//                        param.put("date", DateUtils.getCurrentDateIso());
//
//                        HashMap<String, Object> objectParams = new HashMap<String, Object>(1);
//
//                        objectParams.put("id", SettingsManager.getInstance().getRawValueForKey(SettingsManager.SETTINGS_HANGOUTIDKEY));
//                        objectParams.put("name", "hangout");
//
//                        param.put("object", objectParams);
//
//                        JSONRPC2Request request = new JSONRPC2Request(TWIC_ACTIVITY_ADDPATH, param, requestID);
//                        // Send request
//                        response = client.send(request);
//                    }
////                    catch (JSONException e) {
////                        Log.e(TAG, e.getLocalizedMessage());
////                    }
//                    catch (JSONRPC2SessionException e) {
//                        Log.e(TAG, e.getLocalizedMessage());
//                        Log.e(TAG, e.getMessage());
//                    }
//
//                    if(response != null && response.indicatesSuccess()) {
//                        Log.d(TAG, response.getResult().toString());
//                    }
//                    else if(response != null && response.getError() != null) {
//                        Log.e(TAG, response.getError().toString());
//                    }
//                    else {
//                        Log.e(TAG, "unknown error in sendUserLeave");
//                    }
//                }
//            }.start();
//        }
//    }

}
