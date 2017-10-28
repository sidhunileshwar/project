package com.example.root.locationfinder;

import android.Manifest;
import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;


public class MainActivity extends Activity implements View.OnClickListener
{

    private static final int REQUEST_LOCATION = 1;
    Button btnShow;
    TextView textViewLoc,datetime,address;
    LocationManager locationManager;
    public String lattitude,longitude,dt,tme,adrs;
    DateFormat dfDate,dfTime;
    JsonObjectRequest request;
    private RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);

        textViewLoc= (TextView)findViewById(R.id.text_location);
        datetime= (TextView)findViewById(R.id.text_dateTime);
        address=(TextView)findViewById(R.id.text_address);
        btnShow = (Button)findViewById(R.id.button_location);

        btnShow.setOnClickListener(this);

        dfDate = new SimpleDateFormat("yyyy-MM-dd");
        dt = dfDate.format(Calendar.getInstance().getTime());
        dfTime = new SimpleDateFormat("HH:mm");
        tme = dfTime.format(Calendar.getInstance().getTime());
        requestQueue = Volley.newRequestQueue(getApplicationContext());

    }
    @Override
    public void onClick(View view)
    {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
        {
            buildAlertMessageNoGps();

        }
        else if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
        {
            getLocation();
        }
    }
    private void getLocation()
    {
        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission
                (MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {

            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);

        }
        else
        {
            Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

            if (location != null)
            {
                double latti = location.getLatitude();
                double longi = location.getLongitude();
                lattitude = String.valueOf(latti);
                longitude = String.valueOf(longi);

                textViewLoc.setText("Your current location is"+ "\n" + "Lattitude = " + lattitude
                        + "\n" + "Longitude = " + longitude);

                datetime.setText("Current date is: " + dt+"\n"+"Time is: " +tme);


                request = new JsonObjectRequest("https://maps.googleapis.com/maps/api/geocode/json?latlng=" + lattitude + "," + longitude + "&key=AIzaSyCyDo5nBY3RIRsvCgDtp-5xqg-a48Kz_aI", new Response.Listener<JSONObject>()
                {
                    @Override
                    public void onResponse(JSONObject response)
                    {
                        try
                        {
                            //convert lat long value to address
                            adrs = response.getJSONArray("results").getJSONObject(0).getString("formatted_address");



                            address.setText("Current address is : \n" + adrs);
                        }
                        catch (JSONException e)
                        {
                            e.printStackTrace();
                        }


                    }
                }, new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                });

                requestQueue.add(request);
            }
            else
            {
                Toast.makeText(this,"Unble to Trace your location",Toast.LENGTH_SHORT).show();
            }
        }
    }
    protected void buildAlertMessageNoGps()
    {

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Please Turn ON your GPS Connection")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                {
                    public void onClick(final DialogInterface dialog, final int id)
                    {
                        startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener()
                {
                    public void onClick(final DialogInterface dialog, final int id)
                    {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }
}
