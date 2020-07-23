package com.journaldev.android;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Iterator;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmObject;
import io.realm.RealmResults;
import io.realm.exceptions.RealmPrimaryKeyConstraintException;
import io.realm.mongodb.App;
import io.realm.mongodb.AppConfiguration;
import io.realm.mongodb.Credentials;
import io.realm.mongodb.User;
import io.realm.mongodb.sync.SyncConfiguration;

public class MainActivity extends Activity implements View.OnClickListener {

    Button btnAdd, btnRead;
    EditText inName, inTitle;
    TextView textView;
    private Realm mRealm;
    App app;
    Employee employee;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();

        app = new App( new AppConfiguration.Builder("myrealmsync-oggbn")
                .baseUrl("https://realm.mongodb.com").build());

        enableSync();

        //readEmployeeRecords(mRealm);
       // new Handler().postDelayed(() -> readEmployeeRecords(mRealm), 2000);



    }

    void enableSync()
    {

        //if (app.currentUser() == null) {
            Credentials credentials = Credentials.emailPassword("manish.sahni@mongodb.com", "realmsync");
            app.loginAsync(credentials, new App.Callback<User>() {
                @Override
                public void onResult(App.Result<User> result) {
                    if(result.isSuccess()){
                        initialiseData(true);
                    } else {
                        new AlertDialog.Builder(MainActivity.this)
                                .setMessage("Login Failed - cannot continue")
                                .setPositiveButton("OK", (dialog, which) -> finish())
                                .show();
                    }
                }
            });
       /* } else {
            System.out.println("app.currentUser()  "+app.currentUser());
            initialiseData(false);
        }*/
    }

    protected void initialiseData(boolean login) {
        String partitionValue = "My Project";
        SyncConfiguration config = new SyncConfiguration.Builder(app.currentUser(),partitionValue)
                .waitForInitialRemoteData().build();

        if(mRealm == null) {
            if (login) {

                Realm.getInstanceAsync(config, new Realm.Callback() {

                    @Override
                    public void onError(Throwable exception) {
                        super.onError(exception);
                    }

                    @Override
                    public void onSuccess(Realm realm) {
                        MainActivity.this.mRealm=realm;
                    }

                });
                //readEmployeeRecords(mRealm);
                return;
            } else {

                        MainActivity.this.mRealm = Realm.getInstance(config);
                        //readEmployeeRecords(mRealm);
                    }
            }
    }


    private void initViews() {
        btnAdd = findViewById(R.id.btnAdd);
        btnAdd.setOnClickListener(this);
        btnRead = findViewById(R.id.btnRead);
        btnRead.setOnClickListener(this);
        textView = findViewById(R.id.textViewEmployees);
        inName = findViewById(R.id.inName);
        inTitle = findViewById(R.id.inAge);
        //readEmployeeRecords(mRealm);
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.btnAdd:
                addEmployee();
                break;
            case R.id.btnRead:
                readEmployeeRecords(mRealm);
                break;
        }
    }

    private void addEmployee() {

        Realm realm = MainActivity.this.mRealm;
        try {
            realm.executeTransaction(mRealm -> {

        System.out.println("****** INSIDE add employee");
                try {


                    if (!inName.getText().toString().trim().isEmpty()) {
                        Employee employee = new Employee();
                        employee.name = inName.getText().toString().trim();

                        if (!inTitle.getText().toString().trim().isEmpty())
                            employee.mytitle = inTitle.getText().toString().trim();


                        mRealm.insert(employee);
                    }

                } catch (RealmPrimaryKeyConstraintException e) {
                    Toast.makeText(getApplicationContext(), "Primary Key exists, Press Update instead", Toast.LENGTH_SHORT).show();
                }
            });
            //readEmployeeRecords(MainActivity.this.mRealm);
           /* Realm.getInstanceAsync(config, new Realm.Callback() {
                @Override
                public void onSuccess(Realm realm) {
                    employee = mRealm.where(Employee.class).findFirstAsync();
                    employee.addChangeListener(obj -> {
                        //if (RealmObject.isLoaded(obj) && RealmObject.isValid(obj)) {
                        textView.setText("");
                        textView.append("Name is: "+employee.getName() + " Age is: " + employee.getAge()+"\n");
                        // }
                    });

                }
            });*/

        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    private void readEmployeeRecords(Realm mRealm) {

        System.out.println("Inside read employee " + System.currentTimeMillis());

     /*  employee = mRealm.where(Employee.class).findFirstAsync();
        employee.addChangeListener(obj -> {
            if (RealmObject.isLoaded(obj) && RealmObject.isValid(obj)) {
                textView.setText("");
                textView.append("Name is: " + employee.name + " Title is: " + employee.mytitle + "\n");
            }
        });*/

       RealmResults<Employee> employee = mRealm.where(Employee.class).findAllAsync();
       employee.addChangeListener(obj -> {
           // if (RealmObject.isLoaded(obj) && RealmObject.isValid(obj.first())) {
           textView.setText("");
            Iterator<Employee> emplIterator = employee.iterator();
            while(emplIterator.hasNext()){
                Employee emp = emplIterator.next();
                textView.append("Name is: "+emp.name +" Title is: "+emp.mytitle+"\n");
            //   String text = "Name is: \"+\"<font color=#cc0029\"+emp.name +\"</font>\"+ \" Title is: \" + \"<font color=#ffcc00>\"+emp.mytitle+\"</font>\\n";
              //    textView.setText(Html.fromHtml(text));

            }

           // }
        });

       /* employee.addChangeListener(new RealmChangeListener<Employee>() {
             @Override
               public void onChange(Employee employee) {
                 textView.setText("");
                 textView.append("Name is: "+employee.name + " Title is: " + employee.mytitle+"\n");
                }
            });*/
/*
        RealmResults<Employee> employee = mRealm.where(Employee.class).findAllAsync();
        employee.addChangeListener(obj -> {
            if (RealmObject.isLoaded(obj.first()) && RealmObject.isValid(obj.first())) {
                textView.setText("");
                Iterator<Employee> emplIterator = employee.iterator();
                while(emplIterator.hasNext()){
                    textView.append("Name is: "+emplIterator.next().name + " Age is: " + emplIterator.next().mytitle+"\n");
               }

            }
        });*/
     /*  RealmResults<Employee> results = mRealm.where(Employee.class).findAll();
        textView.setText("");
        for (Employee employee : results) {
            textView.append("Name is: "+employee.name + " Title is: " + employee.mytitle+"\n");
        }
    }*/
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mRealm != null) {
            mRealm.close();
        }
    }
}
