package com.radfordstemnav;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Context;
import android.graphics.Path;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
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
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.models.nosql.LocationsDO;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.*;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link HomeFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment implements RouteFragment.OnFragmentInteractionListener
{

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    ArrayList<String> menu_items;
    ListView listView;

    private OnFragmentInteractionListener homeListener;

    public HomeFragment() {
        // Required empty public constructor
    }
    
    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HomeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static HomeFragment newInstance(String param1, String param2) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            homeListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            String mParam1 = getArguments().getString(ARG_PARAM1);
            String mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }
    Context context;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        context = getActivity().getApplicationContext();
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.CUPCAKE) {
                new db().execute().get(1000, TimeUnit.MILLISECONDS);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }

        listView = (ListView) view.findViewById(R.id.mainMenu);

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

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.popup_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        String key = (String) listView.getItemAtPosition(info.position);
        FragmentManager fragmentManager = getFragmentManager();

        switch (item.getItemId()){
            case R.id.route:
                Bundle routeArgs = new Bundle();
                routeArgs.putString("selection", key);
                RouteFragment routeFrag = new RouteFragment();
                routeFrag.setArguments(routeArgs);
                fragmentManager.beginTransaction()
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


        // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (homeListener != null) {
            homeListener.onFragmentInteraction(uri);
        }
    }

    public void routeHepler() {

    }

    @Override
    public void onDetach() {
        super.onDetach();
        homeListener = null;
    }


    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }



    private class db extends AsyncTask<ArrayList, ArrayList, ArrayList> {
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

            for (int i = 0; i < location_list.size(); i++)
            {
                menu.add(location_list.get(i).getName());
                System.out.println("ITEM ID: " + menu);
            }
            menu_items = menu;
            return menu;
        }


        @Override
        protected void onPreExecute() {}
        @Override
        protected void onPostExecute(ArrayList params){
        }
    }
}
