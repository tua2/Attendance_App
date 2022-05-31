package com.app.diemdanh;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.app.model.userInfo;
import com.app.service.checkAttendData;
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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.SetOptions;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class AttendActivity extends AppCompatActivity
        implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    FirebaseFirestore db;

    boolean showCurGps = true;

    int status = 0;
    int NOT_OPEN = 0,
            OPENING = 1,
            SUMMIT = 2,
            CONFIRMED = 3;
    int statusCode = 0;
    int IS_GET = 1,
            NOT_GET = 0;

    boolean recheck = true;

    String classCode;
    String[] arrStd;
    Map<String, String> mapStdAttendInfo;

    Long session = null;

    String strCode = null, strLocation = null;
    String strTchCode = null, strTchLocation = null;

    private Location location;
    private TextView txtLocation, txtCode, txtSession;
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
        setContentView(R.layout.attend);

        txtLocation = findViewById(R.id.txtlocal);
        txtCode = findViewById(R.id.txtCode);
        txtSession = findViewById(R.id.txtStatus);
        btnBack = findViewById(R.id.buttonTop);
        btnAction = findViewById(R.id.btnAction);

        classCode = getIntent().getStringExtra("CLASS_CODE");

        //add permissions , request location of the users
        permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);

        permissionsToRequest = permissionsToRequest(permissions);

        getLocation();
        if (recheck) {
            checkAttend(classCode);
        }


        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recheck = false;
                finish();
            }
        });
        btnAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Toast.makeText(AttendActivity.this, "Send data" + status + "-" + statusCode +"-" + strLocation + "-"+ strCode, Toast.LENGTH_SHORT).show();
                if (status == SUMMIT) {
                    if (statusCode == IS_GET) {
                        if (strLocation != null && !strLocation.equals("") && strCode != null && !strCode.equals("")) {
                            if (compareAttend()) {
                                btnAction.setText("\nSUBMIT \n...");
                                summitAttend(strLocation, strCode, session);
                            } else {
                                btnAction.setText("FAILED");
                                Toast.makeText(AttendActivity.this, "Code hoặc Location không trùng khớp", Toast.LENGTH_LONG).show();
                                Handler handler = new Handler();
                                handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        finish();
                                    }
                                }, 2000);
                            }
                        } else if (strLocation == null) {
                            Toast.makeText(AttendActivity.this, "Bạn cần kiểm tra lại GPS để tiếp tục", Toast.LENGTH_LONG).show();
                            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivity(intent);
                        }
                    } else if (statusCode == NOT_GET && userInfo.getType().equals("std")) {
                        getScanQR();
                        Log.d("DEBUG", "At Chua get code line 152 ");

//                        Toast.makeText(AttendActivity.this, "Chưa lấy mã QR", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

    }

    public void checkAttend(final String classCode) {
        Log.d("DEBUG", "At check diem danh line 168 ");

        btnAction.setText("CHECKING");
        if(recheck){
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("enroll").document(classCode)
                    .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                        @Override
                        public void onEvent(@Nullable DocumentSnapshot snapshot,
                                            @Nullable FirebaseFirestoreException e) {
                            if (e != null) {
                                Log.w("FIRE", "Listen failed.", e);
                                return;
                            }
                            // Kiem tra lop diem danh da duoc tao hay chua
                            if (snapshot != null && snapshot.exists() && recheck) {
//                    int session = Integer.parseInt((String)snapshot.getData().get("session"));
                                session = (Long) snapshot.getData().get("session");
                                Log.d("FIRE", "Current data: " + "session " + session);
                                if (session == 0) {
                                    txtSession.setText("");
                                    btnAction.setText("Not Open");
                                    status = NOT_OPEN;
                                } else if (session != 0) {
                                    txtSession.setText("Buổi " + session);
                                    getTeacherCodeAndLocation(session);
                                    getAttendResult(session);
                                    status = OPENING;
                                }
                            } else {
                                Log.d("FIRE", "Current data: null");
                                btnAction.setText("Not Open");
                            }
                        }
                    });

        }
    }

    public void getAttendResult(Long session) {
        Log.d("DEBUG", "At check diem danh line 200 ");

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("enroll").document(classCode).collection("std").document(session + "")
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {

                                // get student list in confirmed
                                arrStd = getStudentList(document.getData().get("student").toString());
                                Log.d("FIRE", "check class attend: " + userInfo.getCode() + " - " + Arrays.asList(arrStd).contains(userInfo.getCode()));

                                // Neu da diem danh thanh cong
                                if (Arrays.asList(arrStd).contains(userInfo.getCode())) {
                                    btnAction.setText("Confirmed !");
                                    status = CONFIRMED;
                                    showCurGps = false;
                                    recheck = false;
                                    // lay chi chi tiet diem danh
                                    try {
                                        mapStdAttendInfo = ((HashMap<String, Map>) document.getData().get("std_attend")).get(userInfo.getCode());
                                        Log.d("FIRE", "get attend info " + mapStdAttendInfo.get("code"));
                                        txtCode.setText(mapStdAttendInfo.get("code"));
                                        txtLocation.setText("gps : " + mapStdAttendInfo.get("location"));
                                    } catch (Exception e) {
                                    }
                                } else if (userInfo.getType().equals("std") && recheck) {
                                    // Chua diem danh
                                    getScanQR();
                                    Log.d("DEBUG", "At Chua diem danh line 226 ");
                                }

                            } else {
                                btnAction.setText("Not Open");
                                Log.d("FIRE", "Khong tim thay data " + classCode);
                            }
                        } else {
                            Log.d("FIRE", "get failed with ", task.getException());
                        }
                    }
                });
    }

    public void summitAttend(final String location, final String code, Long session) {
        Log.d("CRE", "gui du lieu diem danh");
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String, Object> student_info = new HashMap<>();
        student_info.put("code", code);
        student_info.put("location", location);

        Map<String, Object> std_attend = new HashMap<>();
        std_attend.put(userInfo.getCode(), student_info);

        final Map<String, Object> sessionDataStd = new HashMap<>();
        sessionDataStd.put("std_attend", std_attend);

        db.collection("enroll").document(classCode).collection("std").document(session + "")
                .set(sessionDataStd, SetOptions.merge())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("CRE", classCode + " Ghi thanh cong : " + location + "-" + code);

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("CRE", "Ghi khong thanh cong ", e);
                    }
                });
        db.collection("enroll").document(classCode).collection("std").document(session + "")
                .update("student", FieldValue.arrayUnion(userInfo.getCode()))
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("CRE", classCode + " Ghi thanh cong diem danh : " + userInfo.getCode());
                        status = CONFIRMED;
                        btnAction.setText("CONFIRMED");
                        showCurGps = false;
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("CRE", "Ghi khong thanh cong ", e);
                    }
                });
    }

    public void getTeacherCodeAndLocation(Long session) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("enroll").document(classCode).collection("tch").document(session + "")
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {


                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            strTchCode = (String) document.getData().get("code");
                            strTchLocation = (String) document.getData().get("location"); // need to refactor
                            Log.d("FIRE", "get teacher location and code " + strTchCode + "-" + strTchLocation);
                        } else {
                            Log.d("FIRE", "Current data: null");
                            btnAction.setText("Not Open");
                        }
                    }
                });
    }

    public boolean compareAttend() {
        checkAttendData check = new checkAttendData(strCode, strLocation, strTchCode, strTchLocation);
//        check.checkLocation2();
        if (check.check()) {
            return true;
        }
        return false;
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

    public void getScanQR() {
        try {
            Intent intent = new Intent("com.google.zxing.client.android.SCAN");
            intent.putExtra("SCAN_MODE", "QR_CODE_MODE"); // "PRODUCT_MODE : bar codes

            startActivityForResult(intent, 0);
        } catch (Exception e) {
            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(AttendActivity.this);
            builder.setMessage("Bạn cần cài đặt QR SCANER để quét mã điểm danh");
            builder.setTitle("Thông báo !");
            builder.setCancelable(false);
            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Uri marketUri = Uri.parse("market://details?id=com.google.zxing.client.android");
                    Intent marketIntent = new Intent(Intent.ACTION_VIEW, marketUri);
                    startActivity(marketIntent);
                    finish();
                }
            });
            builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                    finish();
                }
            });
            android.app.AlertDialog alertDialog = builder.create();
            alertDialog.show();


        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {
                String contents = data.getStringExtra("SCAN_RESULT");
                strCode = contents;
                txtCode.setText("Code : " + contents);
                btnAction.setText("Submit");
                status = SUMMIT;
                statusCode = IS_GET;
            }
            if (resultCode == RESULT_CANCELED) {
//                Toast.makeText(AttendActivity.this, "Quét mã QR không thành công", Toast.LENGTH_LONG).show();
                txtCode.setText("Code : " + "Quét mã QR không thành công");
                status = SUMMIT;
                statusCode = NOT_GET;
                btnAction.setText("GET CODE");
                recheck = false;
            }
        }
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
            strLocation = location.getLatitude() + "," + location.getLongitude();
            txtLocation.setText("Gps : " + location.getLatitude() + "," + location.getLongitude());
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
            txtLocation.setText("Gps : " + location.getLatitude() + "," + location.getLongitude());
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
                            new AlertDialog.Builder(AttendActivity.this).
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

    public String[] getStudentList(String str) {
        JSONArray jsonArray = null;
        try {
            jsonArray = new JSONArray(str);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String[] strArr = new String[jsonArray.length()];
        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                strArr[i] = jsonArray.getString(i);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return strArr;
    }
    @Override
    public void onBackPressed() {
        recheck = false;
        finish();
    }
}
