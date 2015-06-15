package androidapp.things.io.thethingsio;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * When we do a server connection, we need to do it in a different thread from the main activity
 * Created by Duhnnae on 05/06/2015.
 */
public class AsynkTasksCallApi extends AsyncTask<Integer,Integer,Integer>{

    //Tag for logs
    public static final String TAG = "io.things.androidApp";
    int taskNumber = 0;
    SharedPreferences sp;
    ProgressDialog pd;
    Activity activity;

    String serverBaseUrl = "https://api.thethings.io/v2/";
    String serverCall = ""; //We specify the call depend on the taskNumber

    //We use this var to show info on the screen
    String dataToShow = "";

    //Extra data
    String activationCode = "";
    String key = "";
    String value = "";

    public AsynkTasksCallApi(int taskNumber, Activity activity){
        //We create the class and we assign the task number. We also set get the activity to show dialog and response
        this.taskNumber = taskNumber;
        this.activity = activity;
        this.sp = PreferenceManager.getDefaultSharedPreferences(activity);
    }

    @Override
    protected void onPreExecute() {
        //We create a progressDialog
       pd = new ProgressDialog(activity);
        pd.setTitle("Talking with server");
        pd.setMessage(activity.getResources().getString(R.string.connecting));
        pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        pd.setCancelable(true);
        pd.show();
        super.onPreExecute();
    }

    @Override
    protected Integer doInBackground(Integer... params) {

        try {

            HttpPost post = null;
            HttpGet get = null;
            HttpResponse response = null;
            List<NameValuePair> pairs = new ArrayList<NameValuePair>();

            //We set the timeout for the request
            HttpParams httpParameters = new BasicHttpParams();
            // Timeout for stablish connection
            int timeoutConnection = 2000;
            HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
            // Timeout for receive data
            int timeoutSocket = 3000;
            HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);

            //Client to perform connections
            DefaultHttpClient client = new DefaultHttpClient(httpParameters);

            JSONObject jsonObject = null;

            //We get email, password and appId from preferences
            String email = sp.getString("accountEmail", "");
            String pass = sp.getString("accountPass", "");
            String appID = sp.getString("appID", "");
            String thingToken = sp.getString("thingToken", "");

            switch (taskNumber) {

                case 1:
                    //We indicate the server specific call
                    serverCall = "register";

                    //Json object to send
                    jsonObject = new JSONObject();
                    jsonObject.accumulate("email", email);
                    jsonObject.accumulate("password", pass);
                    jsonObject.accumulate("app", appID);

                    break;
                case 2:
                    //We indicate the server specific call
                    serverCall = "login";

                    //Json object to send
                    jsonObject = new JSONObject();
                    jsonObject.accumulate("email", email);
                    jsonObject.accumulate("password", pass);
                    jsonObject.accumulate("app", appID);
                    break;

                case 3:
                    //We indicate the server specific call
                    serverCall = "me/things";

                    //Json object to send
                    jsonObject = new JSONObject();
                    jsonObject.accumulate("thingToken", sp.getString("thingToken", ""));
                    break;

                case 4:
                    //We indicate the server specific call ACTIVATE
                    serverCall = "things/";

                    //Json object to send
                    jsonObject = new JSONObject();
                    jsonObject.accumulate("activationCode", this.activationCode);

                    break;
                case 5:
                    //We indicate the server specific call POST RESOURCE
                    serverCall = "me/resources/"+key+"/"+thingToken;

                    //Json object to send

                    JSONObject jsonPairs = new JSONObject();
                    //jsonPairs.accumulate("key", this.key);
                    jsonPairs.accumulate("value", this.value);

                    JSONArray jsonArray = new JSONArray();
                    jsonArray.put(jsonPairs);

                    jsonObject = new JSONObject();
                    jsonObject.accumulate("values", jsonArray);

                    //pairs.add(new BasicNameValuePair("thingToken", sp.getString("thingToken","")));
                    break;

                //Get resources
                case 101:
                    //We indicate the server specific call
                    serverCall = "me/resources";

                    break;
                case 102:
                    //We indicate the server specific call GET resource
                    serverCall = "me/resources/"+key;

                    break;
            }

            //We add headers
            if(taskNumber < 100){
                String url = serverBaseUrl+serverCall;
                post = new HttpPost(url);
                //We convert the json object into string
                if(jsonObject != null) {
                    Log.d(TAG,"Json body is: "+jsonObject.toString());
                    StringEntity se = new StringEntity(jsonObject.toString(), "UTF-8");

                    //We add it to the post request
                    post.setEntity(se);
                }

                //We set headers
                post.addHeader("Accept", "application/json");
                post.addHeader("Content-Type", "application/json");

                if(taskNumber >= 3) {
                    Log.d(TAG, "Token is: " + sp.getString("token", ""));
                    // post.setHeader(new BasicHeader("Authorization:",sp.getString("token", "")));
                    post.addHeader("Authorization",sp.getString("token", ""));
                }

                /*HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
                connection.setDoOutput(true);
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Authorization", sp.getString("token", ""));
                connection.connect();
                */

                response = client.execute(post);
            }else{

                        //These are get calls
                get =  new HttpGet(serverBaseUrl+serverCall);
                get.setHeader("Accept", "application/json");
                get.setHeader("Content-Type", "application/json");
                get.setHeader("Authorization", sp.getString("token", ""));

                response = client.execute(get);
            }

            HttpEntity httpEntity = response.getEntity();
            String serverResponse = EntityUtils.toString(httpEntity);

            //We show the response in the log
            Log.d(TAG,"Raw server response: "+serverResponse);

            //We get the status and the message field
            JSONObject jsonResponse= new JSONObject(serverResponse);
            String status = "";
            String message = "";

            //We check if we have these fields in the json response
            if(jsonResponse.has("status"))
                status = jsonResponse.getString("status");

            if(jsonResponse.has("message"))
                message = jsonResponse.getString("message");

            //In case of error, we show it on screen
            if(serverResponse.contains("error")){
                dataToShow = "Status: " + status + " || Message: " + message;
            }else {

                String token = "";
                //We switch the response. Each task has a specific response action
                switch (taskNumber) {

                    case 1:
                        //We get the token value and update the preferences with the token
                        //token = jsonResponse.getString("token");
                        //sp.edit().putString("token", token).commit();
                        //dataToShow = "Succedd! Token is: " + token;
                        //break;

                    case 2:
                        //We check for token
                        token = jsonResponse.getString("token");

                        sp.edit().putString("token", token).commit();
                        dataToShow = "Succeed! Token is: " + token;

                        break;
                    case 3:
                        dataToShow = "Status: " + status + " || Message: " + message;
                        break;

                    case 4:
                        if(jsonResponse.has("thingToken")) {
                            //We update the thingToken
                            thingToken = jsonResponse.getString("thingToken");
                            sp.edit().putString("thingToken",thingToken).commit();
                            dataToShow = "Code activated! ThingToken is: "+thingToken;
                        }
                        break;

                    default:
                        //Default action
                        dataToShow = serverResponse;
                        break;
                }
            }


        }catch(Exception e){
            Log.d(TAG, "Exception in asynkTask. "+e.getMessage().toString());
        }
        return null;
    }

    @Override
    protected void onPostExecute(Integer integer) {

        //We show the response on the screen if it is not null or empty
        if(dataToShow != null && !dataToShow.equals("")){
            Toast.makeText(activity.getBaseContext(),dataToShow,Toast.LENGTH_LONG).show();
        }

        //We dismiss the progress dialog
        if(pd != null && pd.isShowing()){
            pd.dismiss();
        }

        super.onPostExecute(integer);
    }

    public void setActivationCode(String activationCode){
        this.activationCode = activationCode;
    }

    public void setKeyValue(String key, String value){
        this.key = key;
        this.value = value;
    }

    public void setKey(String key){
        this.key = key;
    }
}
