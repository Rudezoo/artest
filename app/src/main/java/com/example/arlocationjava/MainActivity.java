package com.example.arlocationjava;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import android.location.Location;
import android.location.LocationManager;

import android.os.Bundle;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;

import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;


import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.ar.core.Anchor;
import com.google.ar.core.ArCoreApk;
import com.google.ar.core.Frame;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.core.Pose;
import com.google.ar.core.Session;
import com.google.ar.core.TrackingState;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.Camera;
import com.google.ar.sceneform.FrameTime;
import com.google.ar.sceneform.HitTestResult;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.Scene;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static java.lang.Math.PI;


public class MainActivity extends AppCompatActivity implements  SensorEventListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener{

    private static final String TAG = "MainActivity";


    private FusedLocationProviderClient providerClient;
    private  GoogleApiClient googleApiClient;

    private ModelRenderable modelRenderable=null;
    private ArFragment arFragment;

    private TextView txtResult;
    private TextView degResult;
    private TextView headResult;

    double longitude;
    double latitude;


    double degrees=0;
    double lngDestination=126.910366;
    double latDestination=37.513901;

    private SensorManager mSensorManager;
    private  Sensor mAccelerometer;
    private Sensor mMagnetometer;
    private  float[] mLastAccelorometer = new float[3];
    private  float[] mLastMagnetometer = new float[3];
    private boolean mLastAccelorometerSet=false;
    private boolean mLastMagnetometerSet=false;
    private float[] mR=new float[9];
    private  float[] mOrientation = new float[3];
    private  float mCurrentDegree=0f;

    float azimuthinDegress= 0;

    boolean requestingLocationUpdates=false;

    LocationRequest locationRequest=new LocationRequest();

    private ArrayList<Pininfo> pininfos=new ArrayList<Pininfo>();
    private ArrayList<AnchorNode> anchorNodes=new ArrayList<AnchorNode>();
    private ArrayList<Node> nodes=new ArrayList<Node>();

    private Button button;
   /* private Location getMyLocation() {
        Location currentLocation=null;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
           // getMyLocation();
        }else{
            String locationProvider = LocationManager.GPS_PROVIDER;
            currentLocation = locationManager.getLastKnownLocation(locationProvider);
            if (currentLocation != null) {
                longitude = currentLocation.getLongitude();
                latitude = currentLocation.getLatitude();
                altitude=currentLocation.getAltitude();

                String txt;
                txt="latitude : "+latitude+"longtitude : "+longitude+"deslatitude : "+latDestination+"deslongtitude : "+lngDestination;
                txtResult.setText(txt);
                Log.d(TAG,txt);
            }
        }
        return currentLocation;
    };*/
    private final PermissionListener permissionListener= new PermissionListener() {
       @Override
       public void onPermissionGranted() {
           Toast.makeText(MainActivity.this,"권한 허가", Toast.LENGTH_SHORT).show();
       }

       @Override
       public void onPermissionDenied(List<String> deniedPermissions) {
           Toast.makeText(MainActivity.this,"권한 거부", Toast.LENGTH_SHORT).show();
       }
   };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtResult = (TextView)findViewById(R.id.txtResult);
        degResult = (TextView)findViewById(R.id.degreetxt);
        headResult = (TextView)findViewById(R.id.headingtxt);
        button=(Button) findViewById(R.id.button);
        button.setOnClickListener(new Button.OnClickListener(){

            @Override
            public void onClick(View v) {
                Pininfo mypininfo=new Pininfo(latitude,longitude);
                pininfos.add(mypininfo);
            }
        });

        Pininfo pininfo=new Pininfo(latDestination,lngDestination);
        Pininfo pininfo2=new Pininfo(37.513916,126.910383);
        pininfos.add(pininfo);
        pininfos.add(pininfo2);
      /*  locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Location location=getMyLocation();*/

        TedPermission.with(this)
                .setPermissionListener(permissionListener)
                .setRationaleConfirmText("권한 필요")
                .setDeniedMessage("권한 거절")
                .setPermissions(Manifest.permission.CAMERA)
                .check();
        TedPermission.with(this)
                .setPermissionListener(permissionListener)
                .setRationaleConfirmText("권한 필요")
                .setDeniedMessage("권한 거절")
                .setPermissions(Manifest.permission.ACCESS_FINE_LOCATION)
                .check();

        TedPermission.with(this)
                .setPermissionListener(permissionListener)
                .setRationaleConfirmText("권한 필요")
                .setDeniedMessage("권한 거절")
                .setPermissions(Manifest.permission.ACCESS_COARSE_LOCATION)
                .check();


        googleApiClient=new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        providerClient=LocationServices.getFusedLocationProviderClient(this);

        providerClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if(location!=null){
                            for (int i = 0; i < pininfos.size(); i++) {
                                latitude = location.getLatitude();
                                longitude = location.getLongitude();
                                degree(i);
                                distance2(i);
                                String txt;
                                txt = "좌표 :" + "latitude :" + latitude + "longtitude : " + longitude;
                                txtResult.setText(txt);
                                Log.d(TAG, txt);
                            }
                        }
                    }
                });
        LocationCallback listener=new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                if(locationResult!=null){
                    for(Location location : locationResult.getLocations()){
                        for (int i = 0; i < pininfos.size(); i++) {
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                            degree(i);
                            distance2(i);
                            String txt;
                            txt = "좌표 :" + "latitude :" + latitude + "longtitude : " + longitude;
                            txtResult.setText(txt);
                            Log.d(TAG, txt);
                        }
                    }
                }
            }
        };
        googleApiClient.connect();

        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mMagnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);


      /*  if (
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 0);
           // getMyLocation();
        }else{

            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    1,
                    0,
                    gpsLocationListener);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                    1,
                    0,
                    gpsLocationListener);
            locationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER,
                    1,
                    0,
                    gpsLocationListener);
        }
*/
        ModelRenderable.builder()
                .setSource(this,R.raw.thispin)
                .build()

                .thenAccept(
                        modelRenderable ->
                                MainActivity.this.modelRenderable=modelRenderable)
                .exceptionally(throwable ->
                        {
                            Toast.makeText(this, "unable to load", Toast.LENGTH_SHORT).show();
                            return null;
                        });

        arFragment=(ArFragment)getSupportFragmentManager().findFragmentById(R.id.ux_fragment);

        arFragment.getArSceneView().getScene().addOnUpdateListener(this::onUpdate);
       // camera=scene.getCamera();


    }




   /* final LocationListener gpsLocationListener = new LocationListener() {
        public void onLocationChanged(Location location) {

            Log.d(TAG,"hereing");

            String provider = location.getProvider();
            longitude = location.getLongitude();
            latitude = location.getLatitude();
            altitude = location.getAltitude();

            String txt;
            txt="latitude : "+latitude+"longtitude : "+longitude+"deslatitude : "+latDestination+"deslongtitude : "+lngDestination;
            txtResult.setText(txt);

        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        public void onProviderEnabled(String provider) {
        }

        public void onProviderDisabled(String provider) {
        }
    };*/

    public void onUpdate(FrameTime frameTime) {


        Frame frame = arFragment.getArSceneView().getArFrame();

        if(frame !=null) {
            //Iterator var3=frame.getUpdatedTrackables(Plane.class).iterator();

                for (Object o : frame.getUpdatedTrackables(Plane.class)) {
                    //Object o = frame.getUpdatedTrackables(Plane.class);
                    Plane plane = (Plane) o;

                    if (plane.getTrackingState() == TrackingState.TRACKING) {
                        arFragment.getPlaneDiscoveryController().hide();
                        //=degree();
                        Iterator iterableAnchor = frame.getUpdatedAnchors().iterator();
                        if (!iterableAnchor.hasNext()) {
                            getpins(plane, frame);
                        }
                    }
                }
            }

        }

    private static double deg2rad(double deg) {
        return (deg * PI / 180.0);
    }
    private static double rad2deg(double rad) {
        return (rad * 180 / PI);
    }
    private void distance2(int i){
        Location A=new Location("point A");
        A.setLatitude(latitude);
        A.setLongitude(longitude);
        Location B=new Location("point B");
        B.setLatitude(pininfos.get(i).pinlatitude);
        B.setLongitude(pininfos.get(i).pinlontitude);

        double distanced=A.distanceTo(B);
        String str="distanced : " + distanced;
        Log.d(TAG, str);
        pininfos.get(i).distance=distanced;
    }
    private  void distance(int i){
        double _latitude1=latitude;
        double _longitude1=longitude;
        double _latitude2=pininfos.get(i).pinlatitude;
        double _longitude2=pininfos.get(i).pinlontitude;
        double theta, dist;
        theta = _longitude1 - _longitude2;
        dist = Math.sin(DegreeToRadian(_latitude1)) * Math.sin(DegreeToRadian(_latitude2)) + Math.cos(DegreeToRadian(_latitude1))
                * Math.cos(DegreeToRadian(_latitude2)) * Math.cos(DegreeToRadian(theta));
        dist = Math.acos(dist);
        dist = RadianToDegree(dist);

        dist = dist * 60 * 1.1515;
        dist = dist * 1.609344;    // 단위 mile 에서 km 변환.
        dist = dist * 1000.0;      // 단위  km 에서 m 로 변환

        pininfos.get(i).distance=dist;
    }
    public double DegreeToRadian(double degree){
        return degree * Math.PI / 180.0;
    }

    //randian -> degree 변환
    public double RadianToDegree(double radian){
        return radian * 180d / Math.PI;
    }


    private void degree(int i){


            double lat1 = latitude / 180 * PI;
            double lng1 = longitude / 180 * PI;
            double lat2 = (pininfos.get(i).pinlatitude) / 180 * PI;
            double lng2 = (pininfos.get(i).pinlontitude) / 180 * PI;


            double x = Math.sin(lng2 - lng1) * Math.cos(lat2);
            double y = Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1) * Math.cos(lat2) * Math.cos(lng2 - lng1);

            double tan2 = Math.atan2(x, y);
            double degre = tan2 * 180 / PI;

            if (degre < 0) {
                degre=degre + 360;
            } else {
                degre= degre;
            }

            pininfos.get(i).degree=degre;
        }

    //아래는 범위를 만들기 위한 계산 함수 자신의 latitude-LatitudeInDifference ~ latitude+LatitudeInDifference 가 범위가 된다
    //diff는 거리 여기서는 30m 가 들어갈 예정
    //반경 m이내의 위도차(degree)
    public double LatitudeInDifference(int diff){
        //지구반지름
        final int earth = 6371000;    //단위m

        return (diff*360.0) / (2*Math.PI*earth);
    }

    //반경 m이내의 경도차(degree)
    public double LongitudeInDifference(double _latitude, int diff){
        //지구반지름
        final int earth = 6371000;    //단위m

        double ddd = Math.cos(0);
        double ddf = Math.cos(Math.toRadians(_latitude));

        return (diff*360.0) / (2*Math.PI*earth*Math.cos(Math.toRadians(_latitude)));
    }



    private Vector3 getScreenCenter(){
        View vw=findViewById(android.R.id.content);
        return new Vector3(vw.getWidth()/2f,vw.getHeight()/2f,0f);
    }


    private void getpins(Plane plane, Frame frame) {

        Log.d(TAG,"here1");
        for(int i=0;i<pininfos.size();i++) {
            if(pininfos.get(i).distance>30){ //특정거리 이상시 해당 핀 제거
                anchorNodes.get(i).removeChild(nodes.get(i));
                anchorNodes.remove(i);
                nodes.remove(i);
                pininfos.remove(i);
            }
            if (pininfos.get(i).pinplaced != true) {
                if ((pininfos.get(i).degree >= azimuthinDegress - 10) && (pininfos.get(i).degree<= azimuthinDegress + 10)) {  //pin이 주변에 있을때
                    Toast.makeText(this, "walk", Toast.LENGTH_SHORT).show();
                    // List<HitResult> hitTest = frame.hitTest(getScreenCenter().x, getScreenCenter().y);
                    Log.d(TAG, "here2");
                    // Iterator hitTestIterator = hitTest.iterator();
                    //  if (hitTestIterator.hasNext()) {
                    //HitResult hitResult = (HitResult) hitTestIterator.next();

               /* Anchor modelAnchor = plane.createAnchor(hitResult.getHitPose());
                AnchorNode anchorNode = new AnchorNode(modelAnchor);
                anchorNode.setRenderable(MainActivity.this.modelRenderable);
                anchorNode.setParent(arFragment.getArSceneView().getScene());*/


                    // Node node = new Node();
                    // node.setParent(anchorNode);
                    // node.setRenderable(MainActivity.this.modelRenderable); //add pin to scene

                   /* node.setWorldRotation(Quaternion.eulerAngles(new Vector3(modelAnchor.getPose().tx(),
                            modelAnchor.getPose().compose(Pose.makeTranslation(0f, 0.05f, 0f)).ty(),
                            modelAnchor.getPose().tz())));*/
                    // dist=distance(latitude,longitude,latDestination,lngDestination);


                    float x = (float) (pininfos.get(i).distance * Math.cos(degrees));
                    float y = 0;
                    float z = (float) (pininfos.get(i).distance * Math.sin(degrees));

                    //float y=modelAnchor.getPose().compose(Pose.makeTranslation(0f, 0.05f, 0)).ty();

                    //float z=modelAnchor.getPose().tz();
                    // float y=modelAnchor.getPose().ty();
                    //거리 맞춰서 표시해야함
                    String ck = "xis : " + pininfos.get(i).distance;
                    String ck2 = "xis2 : " + y;
                    String ck3 = "xis3 : " + z;
                    Log.d(TAG, ck);
                    Log.d(TAG, ck2);
                    Log.d(TAG, ck3);

                    Session session = arFragment.getArSceneView().getSession();

               /* float[] pos = { x,y,-1*z };
                float[] rotation = {0,0,0,1};
                Anchor anchor =  session.createAnchor(new Pose(pos, rotation));*/


                    Vector3 cameraPos = arFragment.getArSceneView().getScene().getCamera().getWorldPosition();
                    Vector3 cameraForward = arFragment.getArSceneView().getScene().getCamera().getForward();
                    Vector3 position = Vector3.add(cameraPos, cameraForward.scaled((float) pininfos.get(i).distance));
;

                    Pose pose = Pose.makeTranslation(position.x, 0, position.z);
                    Anchor anchor = arFragment.getArSceneView().getSession().createAnchor(pose);

                    AnchorNode anchorNode = new AnchorNode(anchor);
                    anchorNode.setParent(arFragment.getArSceneView().getScene());

                    Vector3 direction = Vector3.subtract(cameraPos,anchorNode.getWorldPosition());
                    Quaternion lookRotation=Quaternion.lookRotation(direction,Vector3.up());


                    Node node = new Node();
                    node.setLocalRotation(Quaternion.axisAngle(new Vector3(0, 1f, 0), 90));
                    node.setRenderable(MainActivity.this.modelRenderable);

                    node.setParent(anchorNode);
                   // node.setWorldRotation(lookRotation);


                    //Vector3 distance = Vector3.subtract(node.getWorldPosition(), camera.getWorldPosition());
                    //anchorNode.setWorldPosition(new Vector3(x, y,z));

                /*Vector3 cameraPosition=scene.getCamera().getWorldPosition();
                Vector3 direction = Vector3.subtract(cameraPosition,anchorNode.getWorldPosition());
                Quaternion lookRotation=Quaternion.lookRotation(direction,Vector3.up());

                anchorNode.setWorldRotation(lookRotation);*/

                    Context c = this;

                    String str = "dist : " +  pininfos.get(i).distance;

                    node.setOnTouchListener((v, event) -> {
                        Toast.makeText(
                                c, str, Toast.LENGTH_LONG).show();

                        //anchorNode.removeChild(node);
                        return false;


                    });
                    anchorNodes.add(anchorNode);
                    nodes.add(node);
                    pininfos.get(i).pinplaced=true;

                    //   }

                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(requestingLocationUpdates){
            providerClient.requestLocationUpdates(locationRequest,listener,null);
        }
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED){
            return;
        }
        mSensorManager.registerListener((SensorEventListener) this,mAccelerometer,SensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener((SensorEventListener) this,mMagnetometer,SensorManager.SENSOR_DELAY_GAME);

      /*  locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,1,0,gpsLocationListener);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,1,0,gpsLocationListener);
        locationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER,1,0,gpsLocationListener);*/
    }
    //location 변화시 불려오는 listener
    private  LocationCallback listener=new LocationCallback(){
        @Override
        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);
            if(locationResult!=null){
                for(Location location : locationResult.getLocations()) {
                    for (int i = 0; i < pininfos.size(); i++) {
                        latitude = location.getLatitude();
                        longitude = location.getLongitude();
                        String strr="bearing : "+pininfos.get(i).degree;
                        Log.d(TAG, strr);

                        degree(i); //degree 변경
                        distance2(i); //distance 변경

                        String txt;
                        txt = "좌표 :" + "latitude :" + latitude + "longtitude : " + longitude;
                        txtResult.setText(txt);
                        Log.d(TAG, txt);
                    }
                }
            }
        }
    };

    @Override
    protected void onPause() {
        super.onPause();
      //  locationManager.removeUpdates(gpsLocationListener);
        mSensorManager.unregisterListener((SensorEventListener) this, mAccelerometer);
        mSensorManager.unregisterListener((SensorEventListener) this,mMagnetometer);
        providerClient.removeLocationUpdates(listener);
    }

    //sensor 함수들
    @Override
    public void onSensorChanged(SensorEvent event) {
        if(event.sensor==mAccelerometer){
            System.arraycopy(event.values,0,mLastAccelorometer,0,event.values.length);
            mLastAccelorometerSet=true;
        }else if(event.sensor==mMagnetometer){
            System.arraycopy(event.values,0,mLastMagnetometer,0,event.values.length);
            mLastMagnetometerSet=true;
        }

        if(mLastAccelorometerSet && mLastMagnetometerSet){
            SensorManager.getRotationMatrix(mR,null,mLastAccelorometer,mLastMagnetometer);
            azimuthinDegress=(int)(Math.toDegrees(SensorManager.getOrientation(mR,mOrientation)[0])+360)%360;

            mCurrentDegree=-azimuthinDegress;

            String check;
            check="heading : "+azimuthinDegress;
            degResult.setText(check);


            //degrees=degree();
           // String check2;
           // check2="bearing : "+degree();
           // headResult.setText(check2);

            Log.d(TAG,check);
            //Log.d(TAG,check2);
        }
    }

    //아래는 fuse location provider

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) { //연결되었을때
       locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
       locationRequest.setInterval(1000);
       //locationRequest.setFastestInterval(500);
       providerClient.requestLocationUpdates(locationRequest,listener,null);
       requestingLocationUpdates=true;

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
