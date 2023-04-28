package com.example.ai;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Date;
import java.util.Map;

public class Dashboard extends AppCompatActivity {

    private SharedPreferences.Editor editor;
    private SharedPreferences sharedPreferences;
    FirebaseFirestore db1 = FirebaseFirestore.getInstance();

    TextView usertv;
    TextView uidtv;

    TextView headuser;
    TextView headphone;
    public DrawerLayout drawerLayout;
    SpeedometerView Soil;
    SpeedometerView Temp;
    SpeedometerView Humi;

    static String uid;
    static String username;
     int sm,hum,temp;
    Map<String, Object> doc;
    NavigationView navigationView ;
    View headerView;

    SwitchCompat motor;
    SwitchCompat mode;



    ColorDrawable colorDrawable = new ColorDrawable(Color.parseColor("#004d00"));


    public ActionBarDrawerToggle actionBarDrawerToggle;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        headerView = navigationView.getHeaderView(0);
        navigationView.getMenu().findItem(R.id.nav_logout).setOnMenuItemClickListener(menuItem -> {
            logout();
            return true;
        });
        sharedPreferences = this.getSharedPreferences("login",MODE_PRIVATE);
        editor = sharedPreferences.edit();
        SwipeRefreshLayout swipeRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.refreshLayout);

        motor = (SwitchCompat) findViewById(R.id.motor);
        mode = (SwitchCompat) findViewById(R.id.mode);


        String phone = sharedPreferences.getString("phone","");
        usertv = (TextView) findViewById(R.id.usernametv);
        uidtv = (TextView) findViewById(R.id.uidtv);

        headuser = (TextView) headerView.findViewById(R.id.headuser);
        headphone = (TextView) headerView.findViewById(R.id.headphone);

        SimpleDateFormat sdf = new SimpleDateFormat("HH");
        String Time = sdf.format(new Date());
        String greet = getGreet(Time);



        mode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {

                if (isChecked) {
                    Toast.makeText(Dashboard.this,"Auto Mode turned On", Toast.LENGTH_SHORT).show();
                    motor.setClickable(false);
                }
                else {
                    motor.setClickable(true);

                    Toast.makeText(Dashboard.this,"Auto Mode turned OFF", Toast.LENGTH_SHORT).show();

                }
            }
        });





        motor.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {

                if (isChecked) {
                    Toast.makeText(Dashboard.this,"Motor turned On", Toast.LENGTH_SHORT).show();
                }
                else {

                    Toast.makeText(Dashboard.this,"Motor turned OFF", Toast.LENGTH_SHORT).show();

                }
            }
        });



        swipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {


                        setUser(phone,greet);
                        System.out.println("refreshed");


                        swipeRefreshLayout.setRefreshing(false);


                    }});


        setUser(phone,greet);

        // drawer layout instance to toggle the menu icon to open
        // drawer and back button to close drawer
        drawerLayout = findViewById(R.id.my_drawer_layout);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.nav_open, R.string.nav_close);

        // pass the Open and Close toggle for the drawer layout listener
        // to toggle the button
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

        // to make the Navigation drawer icon always appear on the action bar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setBackgroundDrawable(colorDrawable);


        headphone.setText("+91 " + phone);

        Soil = (SpeedometerView)findViewById(R.id.speedometer);
        Temp = (SpeedometerView)findViewById(R.id.speedometer2);
        Humi = (SpeedometerView)findViewById(R.id.speedometer3);



        Soil.setLabelConverter(new SpeedometerView.LabelConverter() {
            @Override
            public String getLabelFor(double progress, double maxProgress) {
                return String.valueOf((int) Math.round(progress));
            }

        });
        Temp.setLabelConverter(new SpeedometerView.LabelConverter() {
            @Override
            public String getLabelFor(double progress, double maxProgress) {
                return String.valueOf((int) Math.round(progress));
            }

        });

        Humi.setLabelConverter(new SpeedometerView.LabelConverter() {
            @Override
            public String getLabelFor(double progress, double maxProgress) {
                return String.valueOf((int) Math.round(progress));
            }

        });



// configure value range and ticks
        Soil.setMaxSpeed(100);
        Soil.setMajorTickStep(10);
        Soil.setMinorTicks(0);

        Temp.setMaxSpeed(50);
        Temp.setMajorTickStep(5);
        Temp.setMinorTicks(0);

        Humi.setMaxSpeed(100);
        Humi.setMajorTickStep(10);
        Humi.setMinorTicks(0);

    }





    private void setUser(String phone,String greet) {

        DocumentReference docRef = db1.collection("Users").document(phone);

        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d(TAG,"loggg"+document.getData().toString());
                        username= document.get("username").toString();
                        uid = document.get("UID").toString();
                        usertv.setText(greet+" \n"+document.get("username").toString());
                        headuser.setText(document.get("username").toString());

                        setSoilData(uid);
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });

    }

    private void logout() {

        editor.clear();
        editor.commit();
        Toast.makeText(Dashboard.this,"You have been logged out", Toast.LENGTH_LONG).show();
        Intent iout = new Intent(Dashboard.this, LoginActivity.class);
        startActivity(iout);



    }

    private String getGreet(String Time) {

        int tim = Integer.parseInt(Time);
        if(tim<12){
            return "Good Morning";
        }
        else if (tim<16){
            return "Good Afternoon";
        }
        else return "Good Evening";

    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void setSoilData(String uid){

        db1.collection("DS")
                .whereEqualTo("UID", uid).orderBy("RN", Query.Direction.DESCENDING).limit(1)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot dct : task.getResult()) {
                                doc = dct.getData();
                                //     ob = new Dashboard();

                            boolean motorStatus = (boolean) doc.get("Motor");
                                sm = Integer.parseInt(doc.get("SM").toString());
                                temp =Integer.parseInt(doc.get("Temp").toString());
                                hum = Integer.parseInt(doc.get("Hum").toString());
                                // test.setText(sm+"op");
                                //  test.setText(sm+" "+hum+" "+temp);
                                motor.setChecked(motorStatus);
                                Soil.addColoredRange(0, 25, Color.RED);
                                Soil.addColoredRange(25, 75, Color.YELLOW);
                                Soil.addColoredRange(75, 100, Color.GREEN);
                                Soil.setSpeed(sm, 2000, 500);

                                Temp.addColoredRange(0, 20, Color.BLUE);
                                Temp.addColoredRange(20, 35, Color.YELLOW);
                                Temp.addColoredRange(35, 60, Color.RED);
                                Temp.setSpeed(temp, 2000, 500);

                                Humi.addColoredRange(0, 25, Color.RED);
                                Humi.addColoredRange(25, 75, Color.YELLOW);
                                Humi.addColoredRange(75, 100, Color.GREEN);
                                Humi.setSpeed(hum, 2000, 500);
                            }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }

                });

        return ;


    }




}