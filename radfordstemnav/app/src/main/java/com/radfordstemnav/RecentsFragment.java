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
 * {@link RecentsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class RecentsFragment extends Fragment implements RouteFragment.OnFragmentInteractionListener {

    ArrayList<String> menu_items;
    ListView listView;
    Context context;

    public RecentsFragment() {
        // Required empty public constructor
    }

    /**
     * @param context
     */
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        View view = inflater.inflate(R.layout.fragment_recents, container, false);
        context = getActivity().getApplicationContext();
        try {
            new db().execute().get(1000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }

        listView = view.findViewById(R.id.recentMenu);

        System.out.println("MENU ITEM SIZE: " + menu_items.size());
        ArrayAdapter<String> listViewAdapter = new ArrayAdapter<>(
                getActivity(),
                android.R.layout.simple_list_item_1,
                menu_items);
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
     * @param item
     * @return the item that is selected. Either route, direction, favorite
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


    private class db extends AsyncTask<ArrayList, ArrayList, ArrayList> {
        /**
         * @param params
         * @return all items with type "recents"
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
            RecentsFavoritesDO location = new RecentsFavoritesDO();
            location.setCategory("recents");
            location.setUserId(credentialsProvider.getIdentityId());

            DynamoDBQueryExpression<RecentsFavoritesDO> queryExpression = new DynamoDBQueryExpression<RecentsFavoritesDO>()
                    .withHashKeyValues(location);
            ArrayList<String> menu = new ArrayList<>();

            List<RecentsFavoritesDO> location_list = mapper.query(RecentsFavoritesDO.class, queryExpression);

            for (int i = 0; i < location_list.size(); i++) {
                if (location_list.get(i).getCategory().equals("recents")) {
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
}

