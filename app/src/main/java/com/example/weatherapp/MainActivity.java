package com.example.weatherapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Looper;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toolbar;

import com.example.weatherapp.Adapter.ViewPagerAdapter;
import com.example.weatherapp.Common.Common;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private android.support.v7.widget.Toolbar toolbar;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private CoordinatorLayout coordinatorLayout;

    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationCallback locationCallback;
    private LocationRequest locationRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.root_view);
        toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Request permission
        Dexter.withActivity(this)
                .withPermissions(Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        if (report.areAllPermissionsGranted()) {
                            buildLocationRequest();
                            buildLocationCallback();

                            if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                                 != PackageManager.PERMISSION_GRANTED
                                 && ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION)
                                 != PackageManager.PERMISSION_GRANTED) {
                                return;
                            }
                            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(MainActivity.this);
                            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        Snackbar.make(coordinatorLayout,"Permission Denied",Snackbar.LENGTH_LONG)
                                .show();
                    }
                }).check();
    }

    private void buildLocationRequest() {

        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(3000);
        locationRequest.setSmallestDisplacement(10.0f);
    }

    private void buildLocationCallback(){

        locationCallback = new LocationCallback(){

            @Override
            public void onLocationResult(LocationResult locationResult){
                super.onLocationResult(locationResult);

                Common.current_location = locationResult.getLastLocation();

                viewPager = (ViewPager)findViewById(R.id.view_pager);
                setupViewPager(viewPager);
                tabLayout = (TabLayout) findViewById(R.id.tabs);
                tabLayout.setupWithViewPager(viewPager);

                Log.d("Location", locationResult.getLastLocation().getLatitude() + "/"
                        + locationResult.getLastLocation().getLongitude());
            }
        };
    }

    private void setupViewPager(ViewPager viewPager) {

        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(TodayWeatherFragment.getInstance(), "Today");
        adapter.addFragment(ForecastFragment.getInstance(),"5 Days");
        adapter.addFragment(CityFragment.getInstance(),"Cities");
        viewPager.setAdapter(adapter);
    }
}
