package com.journaldev.android;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Iterator;
import java.util.concurrent.TimeUnit;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;
import io.realm.exceptions.RealmPrimaryKeyConstraintException;
import io.realm.mongodb.App;
import io.realm.mongodb.AppConfiguration;
import io.realm.mongodb.Credentials;
import io.realm.mongodb.User;
import io.realm.mongodb.sync.Progress;
import io.realm.mongodb.sync.ProgressListener;
import io.realm.mongodb.sync.ProgressMode;
import io.realm.mongodb.sync.SyncConfiguration;
import io.realm.mongodb.sync.SyncSession;

public class MainActivity extends Activity implements View.OnClickListener {

    Button btnAdd, btnRead;
    EditText inName, inTitle;
    TextView textView;
    private Realm mRealm;
    App app;
    Employee employee;
   // private double latestState = -1.0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();

        app = new App( new AppConfiguration.Builder("myrealmsync-oggbn")
                .baseUrl("https://realm.mongodb.com").build());
        enableSync();

    }

    void enableSync()
    {


/*
        SyncSession.  addDownloadProgressListener(ProgressMode.CURRENT_CHANGE
                , new ProgressListener(){

                    @ovverride
                    public void onChange(Progress progress){


                        if (progress.isTransferComplete() {
                            sysout("send notificatiomn")
                        }

                    })

                }
*/




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
    }

    protected void initialiseData(boolean login) {
        String partitionValue = "My Project";
        SyncConfiguration config = new SyncConfiguration.Builder(app.currentUser(),partitionValue)
                .build();



        // Use this to delete local realm files
        // Realm.deleteRealm(config);

        if(mRealm == null) {
            if (login) {

              //  System.out.println("State is "+ latestState);

                Realm.getInstanceAsync(config, new Realm.Callback() {

                    @Override
                    public void onError(Throwable exception) {
                        super.onError(exception);
                    }

                    @Override
                    public void onSuccess(Realm realm) {


                      /*  SyncSession session = app.getSync().getSession(config);

                        Thread downloadThread = new Thread(new DownloadServerChanges(session));
                        downloadThread.start();

                        Thread uploadThread = new Thread(new UploadServerChanges(session));
                        uploadThread.start();


                        session.addDownloadProgressListener(ProgressMode.INDEFINITELY, new ProgressListener() {
                            @Override
                            public void onChange(Progress progress) {
                                if (progress.isTransferComplete()) {
                                    System.out.println("Download Send Notification");
                                }
                                System.out.println("Download Progress"+ progress.getFractionTransferred());
                                latestState = Double.parseDouble(String.valueOf(progress.getFractionTransferred()));

                            }

                        });



                        session.addUploadProgressListener(ProgressMode.INDEFINITELY, new ProgressListener() {
                            @Override
                            public void onChange(Progress progress) {
                                if (progress.isTransferComplete()) {
                                    System.out.println("Upload Send Notification");
                                }
                                System.out.println("Upload Progress"+ progress.getFractionTransferred());
                                latestState = Double.parseDouble(String.valueOf(progress.getFractionTransferred()));
                            }

                        });*/



                        MainActivity.this.mRealm=realm;

                        RealmResults<Employee> employee = mRealm.where(Employee.class).findAllAsync().sort("mytitle", Sort.DESCENDING);
                        mRealm.addChangeListener(obj -> {
                            // if (RealmObject.isLoaded(obj) && RealmObject.isValid(obj.first())) {
                            textView.setText("");
                            Iterator<Employee> emplIterator = employee.iterator();
                            while(emplIterator.hasNext()){
                                Employee emp = emplIterator.next();
                                textView.append("Name is: "+emp.name +" Title is: "+emp.mytitle+"\n");
                            }
                        });

                        Iterator<Employee> emplIterator = employee.iterator();
                        while(emplIterator.hasNext()){
                            Employee emp = emplIterator.next();
                            textView.append("Name is: "+emp.name +" Title is: "+emp.mytitle+"\n");
                        }

                    }

                });
                return;
            } else {

                MainActivity.this.mRealm = Realm.getInstance(config);


            }
        }
    }


    private void initViews() {
        btnAdd = findViewById(R.id.btnAdd);
        btnAdd.setOnClickListener(this);
        textView = findViewById(R.id.textViewEmployees);
        inName = findViewById(R.id.inName);
        inTitle = findViewById(R.id.inAge);
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.btnAdd:
                addEmployee();
                break;
           /* case R.id.btnRead:
                readEmployeeRecords(mRealm);
                break;*/
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
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mRealm != null) {
            mRealm.close();
        }
    }

    /*class DownloadServerChanges implements Runnable{

        private SyncSession session;
        public DownloadServerChanges(SyncSession session){
            this.session = session;
        }
        @Override
        public void run() {
            try {
                System.out.println("Download STATUS FINAL "+session.downloadAllServerChanges(100, TimeUnit.SECONDS));
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    class UploadServerChanges implements Runnable{

        private SyncSession session;
        public UploadServerChanges(SyncSession session){
            this.session = session;
        }
        @Override
        public void run() {
            try {
                System.out.println("Upload STATUS FINAL "+session.uploadAllLocalChanges(100, TimeUnit.SECONDS));
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }*/
}
