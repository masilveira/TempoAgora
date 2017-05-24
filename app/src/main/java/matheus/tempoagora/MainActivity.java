package matheus.tempoagora;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        loadLocation();
    }

    private void loadLocation(){
        Location currentLocation = getLocation();
        if(currentLocation!=null){
        ((TextView)findViewById(R.id.city_name)).setText(getLocalityFromLocation(currentLocation));
        ((TextView)findViewById(R.id.latitude)).setText(String.format("%s", currentLocation.getLatitude()));
        ((TextView)findViewById(R.id.longitude)).setText(String.format("%s", currentLocation.getLongitude()));
        }

    }

    private Location getLocation() {
        Location location = null;
        try {
            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            boolean isGPSEnabled = locationManager
                    .isProviderEnabled(LocationManager.GPS_PROVIDER);
            if (isGPSEnabled) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                }
            }
            else{
                Toast.makeText(this, getString(R.string.no_gps),
                        Toast.LENGTH_LONG).show();
                location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return location;
    }

    private String getLocalityFromLocation(Location location) {
        Geocoder gcd = new Geocoder(this, Locale.getDefault());
        List<Address> addresses = null;
        try {
            addresses = gcd.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        if (addresses.size() > 0) {
           return addresses.get(0).getLocality();
        } else {
            return null;
        }
    }
}
