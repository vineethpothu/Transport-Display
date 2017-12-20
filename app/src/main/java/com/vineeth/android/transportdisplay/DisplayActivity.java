package com.vineeth.android.transportdisplay;

import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class DisplayActivity extends FragmentActivity implements OnMapReadyCallback {

  private static final String TAG = DisplayActivity.class.getSimpleName();
  private HashMap<String, Marker> mMarkers = new HashMap<>();
  private GoogleMap mMap;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_display);
    // Obtain the SupportMapFragment and get notified when the map is ready to be used.
    SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
        .findFragmentById(R.id.map);
    mapFragment.getMapAsync(this);
  }


  /**
   * Manipulates the map once available.
   * This callback is triggered when the map is ready to be used.
   * This is where we can add markers or lines, add listeners or move the camera. In this case,
   * we just add a marker near Sydney, Australia.
   * If Google Play services is not installed on the device, the user will be prompted to install
   * it inside the SupportMapFragment. This method will only be triggered once the user has
   * installed Google Play services and returned to the app.
   */
  @Override
  public void onMapReady(GoogleMap googleMap) {
    mMap = googleMap;

    mMap.setMaxZoomPreference(16);
    loginToFirebase();
  }

  private void loginToFirebase(){
    String email = getString(R.string.firebase_email);
    String password = getString(R.string.firebase_password);

    FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
          @Override
          public void onComplete(@NonNull Task<AuthResult> task) {
            if(task.isSuccessful()){
              subscribeToUpdates();
              Log.d(TAG, "firebase auth success");
            }
            else{
              Log.d(TAG, "firebase auth failed");
            }
          }
        });
  }

  private void subscribeToUpdates(){
    DatabaseReference ref = FirebaseDatabase.getInstance().getReference(getString(R.string.firebase_path));
    ref.addChildEventListener(new ChildEventListener() {
      @Override
      public void onChildAdded(DataSnapshot dataSnapshot, String s) {
        setMarker(dataSnapshot);
        Log.d(TAG, "Child added");
      }

      @Override
      public void onChildChanged(DataSnapshot dataSnapshot, String s) {
        setMarker(dataSnapshot);
        Log.d(TAG, "Child changed");
      }

      @Override
      public void onChildRemoved(DataSnapshot dataSnapshot) {

      }

      @Override
      public void onChildMoved(DataSnapshot dataSnapshot, String s) {

      }

      @Override
      public void onCancelled(DatabaseError databaseError) {
        Log.d(TAG, "Failed to read value");
      }
    });
  }

  private void setMarker(DataSnapshot dataSnapshot) {
    // When a location update is received, put or update
    // its value in mMarkers, which contains all the markers
    // for locations received, so that we can build the
    // boundaries required to show them all on the map at once
    String key = dataSnapshot.getKey();
    HashMap<String, Object> value = (HashMap<String, Object>) dataSnapshot.getValue();
    Log.d(TAG, value.toString());
    double lat = Double.parseDouble(value.get("latitude").toString());
    double lng = Double.parseDouble(value.get("longitude").toString());
    LatLng location = new LatLng(lat, lng);
    if (!mMarkers.containsKey(key)) {
      mMarkers.put(key, mMap.addMarker(new MarkerOptions().title(key).position(location)));
    } else {
      mMarkers.get(key).setPosition(location);
    }
    LatLngBounds.Builder builder = new LatLngBounds.Builder();
    for (Marker marker : mMarkers.values()) {
      builder.include(marker.getPosition());
    }
    mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 300));
  }

  /*private void setMarker(DataSnapshot dataSnapshot) {
    String key = dataSnapshot.getKey();
    HashMap<String, Object> value = (HashMap<String, Object>) dataSnapshot.getValue();
    Log.d(TAG, value.toString());
    double lat = Double.parseDouble(value.get("latitude").toString());
    double lng = Double.parseDouble(value.get("longitude").toString());
    LatLng location = new LatLng(lat, lng);
    if (!mMarkers.containsKey(key)) {
      mMarkers.put(key, mMap.addMarker(new MarkerOptions().title(key).position(location)));
    } else {
      mMarkers.get(key).setPosition(location);
    }
    LatLngBounds.Builder builder = new LatLngBounds.Builder();
    for (Marker marker : mMarkers.values()) {
      builder.include(marker.getPosition());
    }
    mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 300));
  }*/
}
