package com.radfordstemnav;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBQueryExpression;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.PaginatedQueryList;
import com.amazonaws.models.nosql.LocationsDO;
import com.amazonaws.models.nosql.RecentsFavoritesDO;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link EventsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link EventsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PopularFragment extends Fragment implements RouteFragment.OnFragmentInteractionListener {

    ArrayList<String> menu_items;
    ListView listView;
    ArrayAdapter<String> listViewAdapter;
    Context context;

    public PopularFragment() {
        // Required empty public constructor
    }


    /**
     * @param context
     */
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    /**
     * @param savedInstanceState
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getActivity().getApplicationContext();
        // makes call to database task
        try {
            new db().execute().get(5000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return the generated view
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_popular, container, false);
        listView = view.findViewById(R.id.popularMenu);

        listViewAdapter = new ArrayAdapter<>(
                getActivity(),
                android.R.layout.simple_list_item_1,
                menu_items);

        listView.setAdapter(listViewAdapter);


        registerForContextMenu(listView);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener()

        {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                view.showContextMenu();
            }
        });

        return view;
    }

    /**
     * @param menu
     * @param v
     * @param menuInfo
     */
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.popup_menu, menu);
    }

    /**
     * @param item
     * @return
     */
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        String key = (String) listView.getItemAtPosition(info.position);
        FragmentManager fragmentManager = getFragmentManager();

        switch (item.getItemId()) {
            case R.id.route:
                Bundle routeArgs = new Bundle();
                routeArgs.putString("selection", key);
                RouteFragment routeFrag = new RouteFragment();
                routeFrag.setArguments(routeArgs);
                fragmentManager.beginTransaction()
                        .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right)
                        .replace(R.id.main_fragment_container, routeFrag)
                        .addToBackStack("")
                        .commit()
                ;
                return true;

            case R.id.directions:
                Bundle dirArgs = new Bundle();
                dirArgs.putString("selection", key);
                DirectionsFragment dirFrag = new DirectionsFragment();
                dirFrag.setArguments(dirArgs);
                fragmentManager.beginTransaction()
                        .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right)
                        .replace(R.id.main_fragment_container, dirFrag)
                        .addToBackStack("")
                        .commit();
                return true;

            case R.id.save:
                new favDB().execute(key);
                Toast.makeText(getActivity(), "Location saved", Toast.LENGTH_LONG).show();
                return true;

            default:
                return super.onContextItemSelected(item);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }


    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     */
    public interface OnFragmentInteractionListener {
        // required
    }


    /**
     * Pulls items with type "popular" from the dynamoDB table
     */
    private class db extends AsyncTask<ArrayList, ArrayList, ArrayList> {
        /**
         * @param params
         * @return popular locations
         */
        @Override
        protected ArrayList doInBackground(ArrayList... params) {

            CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                    context,    /* get the context for the application */
                    "us-east-1:843f215d-5abf-42e6-96a0-b64dd0b333b0",    /* Identity Pool ID */
                    Regions.US_EAST_1           /* Region for your identity pool--US_EAST_1 or EU_WEST_1*/
            );
            AmazonDynamoDBClient ddbClient = new AmazonDynamoDBClient(credentialsProvider);
            DynamoDBMapper mapper = new DynamoDBMapper(ddbClient);
            LocationsDO location = new LocationsDO();
            location.setCategory("test");

            DynamoDBQueryExpression<LocationsDO> queryExpression = new DynamoDBQueryExpression<LocationsDO>()
                    .withHashKeyValues(location);
            ArrayList<String> menu = new ArrayList<>();

            List<LocationsDO> location_list = mapper.query(LocationsDO.class, queryExpression);

            for (int i = 0; i < location_list.size(); i++) {
                if (location_list.get(i).getType().equals("popular")) {
                    menu.add(location_list.get(i).getName());
                    System.out.println("ITEM ID: " + menu);
                }
            }
            menu_items = menu;
            return menu;
        }


        @Override
        protected void onPreExecute() {
        }

        /**
         * @param params
         */
        @Override
        protected void onPostExecute(ArrayList params) {
        }
    }


    private class favDB extends AsyncTask<String, String, String> {

        /**
         * @param strings
         * @return null
         * Simply adds to recent/favorites table
         */
        @Override
        protected String doInBackground(String... strings) {
            String location = strings[0];
            CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                    context,    /* get the context for the application */
                    "us-east-1:843f215d-5abf-42e6-96a0-b64dd0b333b0",    /* Identity Pool ID */
                    Regions.US_EAST_1
            );
            AmazonDynamoDBClient ddbClient = new AmazonDynamoDBClient(credentialsProvider);
            DynamoDBMapper mapper = new DynamoDBMapper(ddbClient);
            LocationsDO locations = new LocationsDO();
            RecentsFavoritesDO favorite = new RecentsFavoritesDO();
            locations.setCategory("test");
            locations.setName(location);

            DynamoDBQueryExpression<LocationsDO> favQueryExpr = new DynamoDBQueryExpression<LocationsDO>()
                    .withHashKeyValues(locations);
            PaginatedQueryList<LocationsDO> latlng = mapper.query(LocationsDO.class, favQueryExpr);

            for (int i = 0; i < latlng.size(); i++) {
                if (latlng.get(i).getName().equals(location)) {
                    Double lat = latlng.get(i).getLatitude();
                    Double lng = latlng.get(i).getLongitude();

                    favorite.setUserId(credentialsProvider.getIdentityId());
                    favorite.setCategory("favorites");
                    favorite.setLatitude(lat);
                    favorite.setLongitude(lng);
                    favorite.setName(location);
                    mapper.save(favorite);
                }
            }
            return null;
        }
    }
}