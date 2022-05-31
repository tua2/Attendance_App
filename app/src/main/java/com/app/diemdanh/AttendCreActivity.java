package com.app.diemdanh;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AttendCreActivity extends AppCompatActivity
        implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {


    FirebaseFirestore db;

    String classCode;
    String gps;
    boolean showCurGps = true;

    String strCode = null;
    String strLocation = null;

    Long curSession;

    int status = 0;
    int START = 0,
            OPENING = 1,
            END = 2;

    String strTchCode, strTchLocation;

    private Location location;
    private TextView txtLocation, txtCode, txtSession, txtNotify;
    private Button btnBack, btnAction;
    private GoogleApiClient googleApiClient;
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private LocationRequest locationRequest;
    private static final long UPDATE_INTERVAL = 5000, FASTEST_INTERVAL = 5000; // = 5 seconds
    // lists for permissions
    private ArrayList<String> permissionsToRequest;
    private ArrayList<String> permissionsRejected = new ArrayList<>();
    private ArrayList<String> permissions = new ArrayList<>();
    // integer for permissions results request
    private static final int ALL_PERMISSIONS_RESULT = 1011;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.attend_cre);

        txtLocation = findViewById(R.id.txtlocal);
        txtCode = findViewById(R.id.txtCode);
        txtSession = findViewById(R.id.txtStatus);
        txtNotify = findViewById(R.id.txtNotify);
        btnBack = findViewById(R.id.buttonTop);
        btnAction = findViewById(R.id.btnAction);

        //add permissions , request location of the users
        permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);

        permissionsToRequest = permissionsToRequest(permissions);

        classCode = getIntent().getStringExtra("CLASS_CODE");

        getLocation();
        checkAttend(classCode);


        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (status == START) {
                    if (strCode != null && !strCode.equals("") && strLocation != null && !strLocation.equals("")) {
//                    Toast.makeText(AttendCreActivity.this, "Create attend", Toast.LENGTH_SHORT).show();
                        createEnrollSession(curSession, strCode, strLocation);
                    } else if (strCode == null || strCode.equals("")) {
                        getInputCode();
                    }else if(strLocation == null || strLocation.equals("")){
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(intent);
                    }
                } else if (status == OPENING) {
                    endSession(curSession);
                }
            }
        });

        txtCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("CRE","Ma QR :"+ strCode);
                if (!strCode.trim().equals("null") && !strCode.equals("")) {

                    Intent intentQr = new Intent(AttendCreActivity.this, QRCodeActivity.class);
                    intentQr.putExtra("QR_CODE", strCode);
                    startActivity(intentQr);

                } else {
                    getInputCode();
                }
            }
        });

    }

    public void getInputCode() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Nhập code điểm danh ");

        final EditText input = new EditText(this);
        input.setPadding(20, 10, 10, 20);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                txtCode.setText("Code: " + input.getText());
                strCode = input.getText().toString();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                strCode = input.getText().toString();
                dialog.cancel();
            }
        });

        builder.show();

    }

    public void checkAttend(final String classCode) {
        db = FirebaseFirestore.getInstance();
        db.collection("enroll").document(classCode)
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                        // Kiem tra lop diem danh da duoc tao hay chua
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            curSession = (Long) document.getData().get("session");
                            Long sessionCount = (Long) document.getData().get("sessionCount");
                            Log.d("FIRE", "Current data: " + "session " + curSession);
                            // Tao lop diem danh moi
                            if (curSession == 0) {
                                curSession = sessionCount + 1;
                                status = START;
                                txtSession.setText("Buổi " + curSession);
                                btnAction.setText("Start ! ");
                                getInputCode();
                                // Lop diem danh hien tai dang mo
                            } else if (curSession != 0) {
                                status = OPENING;
                                btnAction.setText("\nIN PROCESS \n... ");
                                txtNotify.setText("Đang mở điểm danh !!!");
                                txtSession.setText(" Buổi " + curSession);
                                showCurGps = false;
                                getAttendResult(curSession);
                            }
                        } else {
                            Log.d("FIRE", "Current data: null");
                            btnAction.setText("Not Open");
                        }
                    }
                });
    }

    public void getAttendResult(Long session) {
        db.collection("enroll").document(classCode).collection("tch").document(session + "")
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {


                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            strTchCode = (String) document.getData().get("code");
                            strTchLocation = (String) document.getData().get("location");
                            strCode = strTchCode;
                            Log.d("FIRE", "get teacher location and code " + strTchCode + "-" + strTchLocation);
                            txtLocation.setText("gps : " + strTchLocation);
                            txtCode.setText("code : " + strTchCode);
                        } else {

                            Log.d("FIRE", "Current data: null");
                            btnAction.setText("Not Open");
                        }
                    }
                });
    }

    public void endSession(final Long session) {
        db.collection("enroll").document(classCode)
                .update("session", 0).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("FIRE", "successfully updated! end Session " + session);
                        btnAction.setText("CLOSED !!!");
                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                finish();
                            }
                        }, 2000);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("FIRE", "Error updating document", e);
                    }
                });
        db.collection("enroll").document(classCode)
                .update("sessionCount", session).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("FIRE", "successfully updated! end Session " + session);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("FIRE", "Error updating document", e);
                    }
                });
    }

    public void createEnrollSession(final Long session, String code, String location) {
        db = FirebaseFirestore.getInstance();
        Map<String, Object> std_attend = new HashMap<>();

        final Map<String, Object> sessionDataStd = new HashMap<>();
        sessionDataStd.put("std_attend", std_attend);
        sessionDataStd.put("student", new ArrayList<>());

        db.collection("enroll").document(classCode).collection("std").document(session + "")
                .set(sessionDataStd)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("CRE", classCode + " Enroll Class successfully written! Session : " + session);

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("CRE", "Khong the tao diem danh Session : " + session, e);
                    }
                });

        final Map<String, Object> sessionDataTch = new HashMap<>();
        sessionDataTch.put("code", code);
        sessionDataTch.put("day", "11/06/2021");
        sessionDataTch.put("location", location);

        db.collection("enroll").document(classCode).collection("tch").document(session + "")
                .set(sessionDataTch)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("CRE", classCode + " Enroll Class successfully written!");

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("CRE", "Error writing document", e);
                    }
                });

        db.collection("enroll").document(classCode)
                .update("session", session).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("FIRE", "successfully updated! end Session " + session);
                        btnAction.setText("\nIN PROCESS \n...");
                        status = OPENING;
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("FIRE", "Error updating document", e);
                    }
                });
    }

    public void getLocation() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (permissionsToRequest.size() > 0) {
                requestPermissions(permissionsToRequest.toArray(
                        new String[permissionsToRequest.size()]), ALL_PERMISSIONS_RESULT);
            }
        }

        // google api client
        googleApiClient = new GoogleApiClient.Builder(this).
                addApi(LocationServices.API).
                addConnectionCallbacks(this).
                addOnConnectionFailedListener(this).build();
    }

    private ArrayList<String> permissionsToRequest(ArrayList<String> wantedPermissions) {
        ArrayList<String> result = new ArrayList<>();

        for (String perm : wantedPermissions) {
            if (!hasPermission(perm)) {
                result.add(perm);
            }
        }

        return result;
    }

    private boolean hasPermission(String permission) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
        }

        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (googleApiClient != null) {
            googleApiClient.connect();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!checkPlayServices()) {
            txtLocation.setText("You need to install Google Play Services to use the App properly");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        // stop location updates
        if (googleApiClient != null && googleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
            googleApiClient.disconnect();
        }
    }

    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);

        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST);
            } else {
                finish();
            }

            return false;
        }

        return true;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        // Permissions ok, we get last location
        location = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);

        if (location != null && showCurGps) {
            txtLocation.setText("gps : " + location.getLatitude() + "," + location.getLongitude());
//            Log.d("CRE","Latitude : " + location.getLatitude() + "Longitude : " + location.getLongitude());
            strLocation = location.getLatitude() + "," + location.getLongitude();
        }

        startLocationUpdates();
    }

    private void startLocationUpdates() {
        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(UPDATE_INTERVAL);
        locationRequest.setFastestInterval(FASTEST_INTERVAL);

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "You need to enable permissions to display location !", Toast.LENGTH_SHORT).show();
        }

        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location != null && showCurGps) {
            strLocation = location.getLatitude() + "," + location.getLongitude();
            txtLocation.setText("gps : " + location.getLatitude() + "," + location.getLongitude());
//            Log.d("CRE","Latitude : " + location.getLatitude() + "Longitude : " + location.getLongitude());
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case ALL_PERMISSIONS_RESULT:
                for (String perm : permissionsToRequest) {
                    if (!hasPermission(perm)) {
                        permissionsRejected.add(perm);
                    }
                }

                if (permissionsRejected.size() > 0) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (shouldShowRequestPermissionRationale(permissionsRejected.get(0))) {
                            new AlertDialog.Builder(AttendCreActivity.this).
                                    setMessage("These permissions are mandatory to get your location. You need to allow them.").
                                    setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                requestPermissions(permissionsRejected.
                                                        toArray(new String[permissionsRejected.size()]), ALL_PERMISSIONS_RESULT);
                                            }
                                        }
                                    }).setNegativeButton("Cancel", null).create().show();

                            return;
                        }
                    }
                } else {
                    if (googleApiClient != null) {
                        googleApiClient.connect();
                    }
                }

                break;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}