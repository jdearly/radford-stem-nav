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
 * {@link FavoritesFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class FavoritesFragment extends Fragment implements RouteFragment.OnFragmentInteractionListener {

    ArrayList<String> fav_menu_items;
    ListView listView;
    Context context;

    public FavoritesFragment() {
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
        if (getArguments() != null) {
        }
    }

    /**
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_favorite, container, false);
        context = getActivity().getApplicationContext();
        try {
            new fav_list().execute().get(1000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }

        listView = view.findViewById(R.id.favoriteMenu);

        System.out.println("MENU ITEM SIZE: " + fav_menu_items.size());
        ArrayAdapter<String> listViewAdapter = new ArrayAdapter<>(
                getActivity(),
                android.R.layout.simple_list_item_1,
                fav_menu_items);
        listView.setAdapter(listViewAdapter);
        registerForContextMenu(listView);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
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
     * Generates the context menu for user selection.
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
                Toast.makeText(getActivity(), "Location already saved", Toast.LENGTH_LONG).show();
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
     * Connects to DynamoDB table and adds the selection to the favorites category.
     */
    private class fav_list extends AsyncTask<ArrayList, ArrayList, ArrayList> {
        @Override
        protected ArrayList doInBackground(ArrayList... params) {

            CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                    context,    /* get the context for the application */
                    "us-east-1:843f215d-5abf-42e6-96a0-b64dd0b333b0",    /* Identity Pool ID */
                    Regions.US_EAST_1           /* Region for your identity pool--US_EAST_1 or EU_WEST_1*/
            );
            AmazonDynamoDBClient client = new AmazonDynamoDBClient(credentialsProvider);
            DynamoDBMapper mapper = new DynamoDBMapper(client);
            RecentsFavoritesDO fav_location = new RecentsFavoritesDO();
            fav_location.setCategory("favorites");
            fav_location.setUserId(credentialsProvider.getIdentityId());

            DynamoDBQueryExpression<RecentsFavoritesDO> favQueryExpression = new DynamoDBQueryExpression<RecentsFavoritesDO>()
                    .withHashKeyValues(fav_location);
            ArrayList<String> fav_menu = new ArrayList<>();

            List<RecentsFavoritesDO> fav_location_list = mapper.query(RecentsFavoritesDO.class, favQueryExpression);

            for (int i = 0; i < fav_location_list.size(); i++) {
                if (fav_location_list.get(i).getCategory().equals("favorites")) {
                    fav_menu.add(fav_location_list.get(i).getName());
                }
            }
            fav_menu_items = fav_menu;
            return fav_menu;
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
}

