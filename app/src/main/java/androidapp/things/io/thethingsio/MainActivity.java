package androidapp.things.io.thethingsio;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;


public class MainActivity extends ActionBarActivity {

    //Global class variables
    SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //We pick up SharedPreferences of the app
        sp = PreferenceManager.getDefaultSharedPreferences(this);

        //We show the user login dialog if we do not have an account registered
        if(sp.getString("accountEmail","").equals(""))
            createLoginDialog();

        //We add click listener to the buttons to perform actions
        final Button accountLogin = (Button) findViewById(R.id.account_login);
        accountLogin.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                createLoginDialog();
            }
        });

        final Button callApi = (Button) findViewById(R.id.callApi);
        callApi.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                createApiDialog();
            }
        });


    }

    public void createLoginDialog(){

        //We create a dialog to get the username and password
        final Dialog dialog = new Dialog(this, R.style.FullHeightDialog);
        dialog.setContentView(R.layout.custom_dialog_account_login);
        dialog.setCanceledOnTouchOutside(true);

        //We instantiate the editText objects and we retrieve the inputs later when user clicks OK
        final EditText editTextEmail = (EditText) dialog.findViewById(R.id.editTextEmail);
        editTextEmail.setText(sp.getString("accountEmail",""));

        final EditText editTextPass = (EditText) dialog.findViewById(R.id.editTextPass);
        //We should not store the password in the sharedpreferences as plain text, but this is a test
        editTextPass.setText(sp.getString("accountPass",""));

        final EditText editTextAppID = (EditText) dialog.findViewById(R.id.editTextAppID);
        editTextAppID.setText(sp.getString("appID",""));

        final EditText editTextThingToken = (EditText) dialog.findViewById(R.id.editTextThingToken);
        editTextThingToken.setText(sp.getString("thingToken",""));

        Button okButton = (Button) dialog.findViewById(R.id.dialogButtonOK);
        okButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //We pick up the user inputs
                String email = editTextEmail.getText().toString();
                String pass = editTextPass.getText().toString();
                String appID = editTextAppID.getText().toString();
                String thingToken = editTextThingToken.getText().toString();

                //We update the sharedPreferences
                sp.edit().putString("accountEmail",email).commit();
                sp.edit().putString("accountPass",pass).commit();
                sp.edit().putString("appID",appID).commit();
                sp.edit().putString("thingToken",thingToken).commit();
                dialog.dismiss();

                Toast.makeText(getBaseContext(),"Preferences updated",Toast.LENGTH_LONG).show();
            }
        } );

        Button cancelButton = (Button) dialog.findViewById(R.id.dialogButtonCancel);
        cancelButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        } );

        CheckBox showPass = (CheckBox) dialog.findViewById(R.id.checkBoxShowPass);

        showPass.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener() {

            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // checkbox status is changed from uncheck to checked.
                if (!isChecked) {
                    // show passwords
                    editTextPass.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    editTextAppID.setTransformationMethod(PasswordTransformationMethod.getInstance());

                } else {
                    // hide passwords
                    editTextPass.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    editTextAppID.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                }
            }
        });

        dialog.show();
    }

    public void createApiDialog(){

        //We create a dialog to get the username and password
        final Dialog dialog = new Dialog(this, R.style.FullHeightDialog);
        dialog.setContentView(R.layout.custom_dialog_call_api);
        dialog.setCanceledOnTouchOutside(true);

        //We store this activity into a variable, to send it to asynkTasks
        final Activity activity = this;

        Button registerButton = (Button) dialog.findViewById(R.id.buttonRegister);
        registerButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                AsynkTasksCallApi asynkTask = new AsynkTasksCallApi(1,activity);
                asynkTask.execute();
            }
        } );

        Button loginButton = (Button) dialog.findViewById(R.id.buttonLogin);
        loginButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                AsynkTasksCallApi asynkTask = new AsynkTasksCallApi(2,activity);
                asynkTask.execute();
            }
        } );

        Button linkButton = (Button) dialog.findViewById(R.id.buttonLinkThing);
        linkButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if( sp.getString("thingToken","").equals("")){
                    Toast.makeText(getBaseContext(),"thingToken is empty! Please, fill this field.",Toast.LENGTH_LONG).show();
                }else {
                    AsynkTasksCallApi asynkTask = new AsynkTasksCallApi(3, activity);
                    asynkTask.execute();
                }
            }
        } );

        final Button getResources = (Button) dialog.findViewById(R.id.buttonGetResources);
        getResources.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //1XX calls are GET calls
                    AsynkTasksCallApi asynkTask = new AsynkTasksCallApi(101, activity);
                    asynkTask.execute();
                }

        } );

        Button activateCode = (Button) dialog.findViewById(R.id.buttonActivateCode);
        activateCode.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //We create a dialog to get the username and password
                final Dialog dialog2 = new Dialog(activity, R.style.FullHeightDialog);
                dialog2.setContentView(R.layout.custom_dialog_receive_input);
                dialog2.setCanceledOnTouchOutside(true);

                final EditText editTextInputData = (EditText) dialog2.findViewById(R.id.editTextInputData);
                editTextInputData.setText(sp.getString("lastActivationCode",""));
                editTextInputData.setHint(getResources().getString(R.string.activation_code));

                Button okButton = (Button) dialog2.findViewById(R.id.dialogButtonOK);
                okButton.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        //We pick up the user inputs
                        String activationCode = editTextInputData.getText().toString();

                        //We update the sharedPreferences
                        sp.edit().putString("lastActivationCode",activationCode).commit();

                        AsynkTasksCallApi asynkTask = new AsynkTasksCallApi(4, activity);
                        asynkTask.setActivationCode(activationCode);
                        asynkTask.execute();
                        dialog2.dismiss();

                    }
                } );

                Button cancelButton = (Button) dialog2.findViewById(R.id.dialogButtonCancel);
                cancelButton.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        dialog2.dismiss();
                    }
                } );

                dialog2.show();

            }

        } );

        Button postValue = (Button) dialog.findViewById(R.id.buttonPostValue);
        postValue.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //We create a dialog to get the username and password
                final Dialog dialog2 = new Dialog(activity, R.style.FullHeightDialog);
                dialog2.setContentView(R.layout.custom_dialog_post_value);
                dialog2.setCanceledOnTouchOutside(true);

                final EditText editTextKey = (EditText) dialog2.findViewById(R.id.editTextKey);
                editTextKey.setText(sp.getString("lastKey",""));

                final EditText editTextValue = (EditText) dialog2.findViewById(R.id.editTextValue);
                editTextValue.setText("");

                Button okButton = (Button) dialog2.findViewById(R.id.dialogButtonOK);
                okButton.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        //We pick up the user inputs
                        String key = editTextKey.getText().toString();
                        String value = editTextValue.getText().toString();

                        sp.edit().putString("lastKey",key).commit();

                        if(key.equals("") ||value.equals("")) {
                            Toast.makeText(activity,"Please fill the fields",Toast.LENGTH_LONG).show();
                        }
                       else{
                            AsynkTasksCallApi asynkTask = new AsynkTasksCallApi(5, activity);
                            asynkTask.setKeyValue(key, value);
                            asynkTask.execute();
                            dialog2.dismiss();
                        }



                    }
                } );

                Button cancelButton = (Button) dialog2.findViewById(R.id.dialogButtonCancel);
                cancelButton.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        dialog2.dismiss();
                    }
                } );

                dialog2.show();

            }

        } );

        Button getResource = (Button) dialog.findViewById(R.id.buttonGetResource);
        getResource.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //We create a dialog to get the username and password
                final Dialog dialog2 = new Dialog(activity, R.style.FullHeightDialog);
                dialog2.setContentView(R.layout.custom_dialog_receive_input);
                dialog2.setCanceledOnTouchOutside(true);

                final EditText editTextInputData = (EditText) dialog2.findViewById(R.id.editTextInputData);
                editTextInputData.setText(sp.getString("lastResource",""));
                editTextInputData.setHint(getResources().getString(R.string.resource));

                Button okButton = (Button) dialog2.findViewById(R.id.dialogButtonOK);
                okButton.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        //We pick up the user inputs
                        String key = editTextInputData.getText().toString();

                        //We update the sharedPreferences
                        sp.edit().putString("lastResource",key).commit();

                        AsynkTasksCallApi asynkTask = new AsynkTasksCallApi(102, activity);
                        asynkTask.setKey(key);
                        asynkTask.execute();
                        dialog2.dismiss();

                    }
                } );

                Button cancelButton = (Button) dialog2.findViewById(R.id.dialogButtonCancel);
                cancelButton.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        dialog2.dismiss();
                    }
                } );

                dialog2.show();

            }

        } );



        dialog.show();
    }



}
