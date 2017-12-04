package com.radfordstemnav;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBQueryExpression;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.PaginatedQueryList;
import com.amazonaws.models.nosql.LocationsDO;
import com.amazonaws.models.nosql.RecentsFavoritesDO;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link DirectionsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link DirectionsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DirectionsFragment extends Fragment implements LocationListener {

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    private static final String ARG_PARAM1 = "selection";

    LocationManager locationManager;
    LatLng dir_dest;
    Context context;
    ArrayList<String> directions = new ArrayList<>();
    TextView dir_textView;
    Adapter textViewAdapter;
    LatLng myGPSPosition;
    String str;
    private String mParam1;
    private GoogleMap mMap;


    public DirectionsFragment() {
        // Required empty public constructor
    }


    public static DirectionsFragment newInstance(String param1) {
        DirectionsFragment fragment = new DirectionsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        fragment.setArguments(args);
        return fragment;
    }

    public void generateRoute(LatLng current) {

        String url = getUrl(current, dir_dest);
        FetchUrl FetchUrl = new FetchUrl();
        // Download JSON data from Google Directions API
        try {
            FetchUrl.execute(url).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

    }

    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof HomeFragment.OnFragmentInteractionListener) {
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_directions, container, false);
        dir_textView = view.findViewById(R.id.dir_textView);
        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkLocationPermission();
        }
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
        }
        context = getActivity().getApplicationContext();
        try {
            new DirectionsFragment.database().execute().get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        PendingIntent pi = PendingIntent.getBroadcast(context,
                0,
                new Intent(""),
                PendingIntent.FLAG_UPDATE_CURRENT);
        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, pi);

        try {
            @SuppressLint("MissingPermission") Location locationGPS = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

            double latitude = locationGPS.getLatitude();
            double longitude = locationGPS.getLongitude();
            myGPSPosition = new LatLng(latitude, longitude);
            generateRoute(myGPSPosition);
        } catch (NullPointerException | IllegalArgumentException e) {
            e.printStackTrace();
            Toast.makeText(getActivity(), "Unable to get GPS data", Toast.LENGTH_SHORT).show();
        }
    }


    // Checks to verify that the user has a network connecton to make the request for the route
    public boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager)
                getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }


    private String getUrl(LatLng origin, LatLng dir_dest) {

        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;

        // Destination of route
        String str_dest = "destination=" + dir_dest.latitude + "," + dir_dest.longitude;

        String str_mode = "mode=walking";

        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + "&" + str_mode;

        // Output format
        String output = "json";

        // Building the url to Google Directions web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;
        return url;
    }

    // Download JSON data from the URL
    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);

            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();

            // Connecting to url
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb = new StringBuffer();

            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            data = sb.toString();
            Log.d("downloadUrl", data.toString());
            br.close();

        } catch (Exception e) {
            Log.d("Exception", e.toString());
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }


    @Override
    public void onLocationChanged(Location location) {

    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Give a reason why location services are required. Once the user acknowledges the
                // explanation, permission prompt should show
                new AlertDialog.Builder(getActivity())
                        .setTitle("Location Permission Required")
                        .setMessage("Radford Navigator requires Location Services, please grant permission to use location functionality.")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // The following should prompt the user for permissions after the explanation above
                                ActivityCompat.requestPermissions(getActivity(),
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION);
                            }
                        })
                        .create()
                        .show();

            } else {
                // Request the permission on first-time startup and permissions have not yet been
                // allowed or denied.
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // If permission was granted, the app will then load the activity with users location
                    if (ContextCompat.checkSelfPermission(getActivity(),
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                    }
                } else {
                    // If permission is denied, this displays to the user a confirmation.
                    // The app shows the map activity currently, but does not get the user's location.
                    Toast.makeText(getActivity(), "Location permission denied", Toast.LENGTH_LONG).show();
                }
                return;
            }
            // Other permission requests can be added here.
        }
    }

    public interface OnFragmentInteractionListener {
    }

    // Fetches data from url passed
    private class FetchUrl extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... url) {

            // For storing data from web service
            String data = "";
            try {
                // Fetching the data from web service
                data = downloadUrl(url[0]);
                Log.d("Background Task data", data.toString());
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            DirectionsFragment.ParserTask parserTask = new DirectionsFragment.ParserTask();
            // Invokes the thread for parsing the JSON data
            try {
                parserTask.execute(result).get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
    }

    // Parse the Google Places in JSON format
    private class ParserTask extends AsyncTask<String, Integer, List<ArrayList<String>>> {

        // Parsing the data in non-ui thread
        @Override
        protected List<ArrayList<String>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<ArrayList<String>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                Log.d("ParserTask", jsonData[0].toString());
                DataParser parser = new DataParser();
                Log.d("ParserTask", parser.toString());

                // Starts parsing data
                routes = parser.parseDirections(jObject);
                Log.d("ParserTask", "Executing routes");
                Log.d("ParserTask", routes.toString());

            } catch (Exception e) {
                Log.d("ParserTask", e.toString());
                e.printStackTrace();
            }
            return routes;
        }

        // Executes in UI thread, after the parsing process
        //@RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        protected void onPostExecute(List<ArrayList<String>> result) {

            // Traversing through all the routes
            for (int i = 0; i < result.size(); i++) {

                // Fetching i-th route
                ArrayList<String> dir_path = result.get(i);

                for (int j = 0; j < dir_path.size(); j++) {
                    directions.add(dir_path.get(j));
                }
            }
            str = TextUtils.join("<br>", directions);
            dir_textView.setText(Html.fromHtml(str));
        }
    }

    private class database extends AsyncTask<LatLng, LatLng, LatLng> {
        @Override
        protected LatLng doInBackground(LatLng... params) {

            CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                    context,    /* get the context for the application */
                    "us-east-1:843f215d-5abf-42e6-96a0-b64dd0b333b0",    /* Identity Pool ID */
                    Regions.US_EAST_1           /* Region for your identity pool--US_EAST_1 or EU_WEST_1*/
            );
            AmazonDynamoDBClient ddbClient = new AmazonDynamoDBClient(credentialsProvider);
            DynamoDBMapper mapper = new DynamoDBMapper(ddbClient);
            LocationsDO dir_destination = new LocationsDO();
            RecentsFavoritesDO recentLocation = new RecentsFavoritesDO();
            dir_destination.setCategory("test");
            dir_destination.setName(mParam1);

            recentLocation.setCategory("recents");
            recentLocation.setUserId(credentialsProvider.getIdentityId());
            recentLocation.setName(mParam1);
            System.out.println("NAME: " + mParam1);

            DynamoDBQueryExpression<LocationsDO> queryExpr = new DynamoDBQueryExpression<LocationsDO>()
                    .withHashKeyValues(dir_destination);
            PaginatedQueryList<LocationsDO> latlng = mapper.query(LocationsDO.class, queryExpr);

            for (int i = 0; i < latlng.size(); i++) {
                if (latlng.get(i).getName().equals(mParam1)) {
                    Double lat = latlng.get(i).getLatitude();
                    Double lng = latlng.get(i).getLongitude();
                    dir_dest = new LatLng(lat, lng);

                    recentLocation.setUserId(credentialsProvider.getIdentityId());
                    recentLocation.setCategory("recents");
                    recentLocation.setLatitude(lat);
                    recentLocation.setLongitude(lng);
                    recentLocation.setName(mParam1);
                    // TTL of one week, then the item is removed from the database
                    recentLocation.setTTL((int) (System.currentTimeMillis() / 1000L) + 604800);
                    mapper.save(recentLocation);

                }
            }
            return dir_dest;
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected void onPostExecute(LatLng params) {
        }
    }


}