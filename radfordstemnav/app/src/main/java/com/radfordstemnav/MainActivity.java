//
// Copyright 2017 Amazon.com, Inc. or its affiliates (Amazon). All Rights Reserved.
//
// Code generated by AWS Mobile Hub. Amazon gives unlimited permission to 
// copy, distribute and modify it.
//
// Source code generated from template: aws-my-sample-app-android v0.19
//
package com.radfordstemnav;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobile.auth.core.DefaultSignInResultHandler;
import com.amazonaws.mobile.auth.core.IdentityManager;
import com.amazonaws.mobile.auth.core.IdentityProvider;
import com.amazonaws.mobile.auth.ui.SignInActivity;
import com.amazonaws.regions.Regions;
import com.facebook.CallbackManager;
import com.facebook.FacebookSdk;
import com.radfordstemnav.dummy.DummyContent;
import com.radfordstemnav.navigation.NavigationDrawer;

import java.lang.ref.WeakReference;

public class MainActivity extends AppCompatActivity implements View.OnClickListener,
        HomeFragment.OnFragmentInteractionListener,
        MapFragment.OnFragmentInteractionListener,
        RecentsFragment.OnFragmentInteractionListener,
        PopularFragment.OnFragmentInteractionListener,
        EventFragment.OnFragmentInteractionListener,
        MediaFragment.OnFragmentInteractionListener,
        RouteFragment.OnFragmentInteractionListener,
        FavoritesFragment.OnFragmentInteractionListener {



    /** Class name for log messages. */
    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    /** Bundle key for saving/restoring the toolbar title. */
    private static final String BUNDLE_KEY_TOOLBAR_TITLE = "title";

    /** The identity manager used to keep track of the current user account. */
    private IdentityManager identityManager;

    /** The toolbar view control. */
    private Toolbar toolbar;

    /** Our navigation drawer class for handling navigation drawer logic. */
    private NavigationDrawer navigationDrawer;

    /** The helper class used to toggle the left navigation drawer open and closed. */
    private ActionBarDrawerToggle drawerToggle;

    /** Data to be passed between fragments. */
    private Bundle fragmentBundle;

    private Button   signOutButton;
    private Button   signInButton;

    /**
     * Initializes the Toolbar for use with the activity.
     */
    private void setupToolbar(final Bundle savedInstanceState) {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        // Set up the activity to use this toolbar. As a side effect this sets the Toolbar's title
        // to the activity's title.
        setSupportActionBar(toolbar);

        if (savedInstanceState != null) {
            // Some IDEs such as Android Studio complain about possible NPE without this check.
            assert getSupportActionBar() != null;

            // Restore the Toolbar's title.
            getSupportActionBar().setTitle(
                savedInstanceState.getCharSequence(BUNDLE_KEY_TOOLBAR_TITLE));
        }
    }

    /**
     * Initializes the sign-in and sign-out buttons.
     */
    private void setupSignInButtons() {

        signOutButton = (Button) findViewById(R.id.button_signout);
        signOutButton.setOnClickListener(this);

        signInButton = (Button) findViewById(R.id.button_signin);
        signInButton.setOnClickListener(this);

        final boolean isUserSignedIn = identityManager.isUserSignedIn();
        signOutButton.setVisibility(isUserSignedIn ? View.VISIBLE : View.INVISIBLE);
        signInButton.setVisibility(!isUserSignedIn ? View.VISIBLE : View.INVISIBLE);

    }

    /**
     * Initializes the navigation drawer menu to allow toggling via the toolbar or swipe from the
     * side of the screen.
     */
    private void setupNavigationMenu(final Bundle savedInstanceState) {
        final DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        final ListView drawerItems = (ListView) findViewById(R.id.nav_drawer_items);

        // Create the navigation drawer.
        navigationDrawer = new NavigationDrawer(this, toolbar, drawerLayout, drawerItems,
            R.id.main_fragment_container);

        // Add navigation drawer menu items.
        // Home isn't a demo, but is fake as a demo.
        //mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        //mDrawerList = (ListView) findViewById(R.id.nav_drawer_items);
        String[] items = getResources().getStringArray(R.array.list_array);

        // Set the adapter for the list view
        drawerItems.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_selectable_list_item, items));

        drawerItems.setOnItemClickListener(new DrawerItemClickListener());

        if (savedInstanceState == null) {
            // Add the home fragment to be displayed initially.
        }
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectItem(position);
        }
    }

    /** Swaps fragments in the main content view */
    private void selectItem(int position) {
        // Create a new fragment and specify the planet to show based on position
        if (position == 0) {
            navigationDrawer.closeDrawer();
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.main_fragment_container, new HomeFragment())
                    .addToBackStack("")
                    .commit();
        }
        if (position == 1) {
            navigationDrawer.closeDrawer();
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.main_fragment_container, new MapFragment())
                    .addToBackStack("")
                    .commit();
        }
        if (position == 2) {
            navigationDrawer.closeDrawer();
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.main_fragment_container, new RecentsFragment())
                    .addToBackStack("")
                    .commit();
        }
        if (position == 3) {
            navigationDrawer.closeDrawer();
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.main_fragment_container, new PopularFragment())
                    .addToBackStack("")
                    .commit();
        }
        if (position == 4) {
            navigationDrawer.closeDrawer();
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.main_fragment_container, new FavoritesFragment())
                    .addToBackStack("")
                    .commit();
        }
        if (position == 5) {
            navigationDrawer.closeDrawer();
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.main_fragment_container, new MediaFragment())
                    .addToBackStack("")
                    .commit();
        }
    }

    //@Override
//    public void setTitle(CharSequence title) {
//        mTitle = title;
//        getActionBar().setTitle(mTitle);
//    }



    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Obtain a reference to the identity manager.
        identityManager = IdentityManager.getDefaultIdentityManager();

        setContentView(R.layout.activity_main);

        setupToolbar(savedInstanceState);
        Application.setContext(this);
        setupNavigationMenu(savedInstanceState);
        setFragment(new HomeFragment(), this);
        FragmentManager fm = getSupportFragmentManager();
        for(int i = 0; i < fm.getBackStackEntryCount(); ++i) {
            fm.popBackStack();
        }
        checkLocationPermission();
    }
    public void setFragment(Fragment fragment, Context context) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.main_fragment_container, fragment)
                .commit();
    }

    @Override
    protected void onResume() {
        super.onResume();


        setupSignInButtons();
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        // Handle action bar item clicks here excluding the home button.

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(final Bundle bundle) {
        super.onSaveInstanceState(bundle);
        // Save the title so it will be restored properly to match the view loaded when rotation
        // was changed or in case the activity was destroyed.
        if (toolbar != null) {
            bundle.putCharSequence(BUNDLE_KEY_TOOLBAR_TITLE, toolbar.getTitle());
        }
    }

    @Override
    public void onClick(final View view) {
        if (view == signOutButton) {
            // The user is currently signed in with a provider. Sign out of that provider.
            identityManager.signOut();
            // Initiate obtaining an anonymous (guest) identity.
            identityManager.getUserID(null);
            // Show the sign-in button and hide the sign-out button.
            signOutButton.setVisibility(View.INVISIBLE);
            signInButton.setVisibility(View.VISIBLE);

            // Close the navigation drawer.
            navigationDrawer.closeDrawer();
            return;
        }
        if (view == signInButton) {
            final WeakReference<MainActivity> self = new WeakReference<MainActivity>(this);

            identityManager.setUpToAuthenticate(this, new DefaultSignInResultHandler() {

                @Override
                public void onSuccess(Activity activity, IdentityProvider identityProvider) {
                    // User has signed in
                    Log.e("Success", "User signed in");
                    activity.finish();
                }

                @Override
                public boolean onCancel(Activity activity) {
                    return true;
                }
            });

            SignInActivity.startSignInActivity(this, com.radfordstemnav.Application.sAuthUIConfiguration);

            // Close the navigation drawer.
            navigationDrawer.closeDrawer();
            return;
        }

        // ... add any other button handling code here ...

    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        final FragmentManager fragmentManager = this.getSupportFragmentManager();
        
        if (navigationDrawer.isDrawerOpen()) {
            navigationDrawer.closeDrawer();
            return;
        }
        super.onBackPressed();
        fragmentManager.popBackStack();
    }


    /**
     * Stores data to be passed between fragments.
     * @param fragmentBundle fragment data
     */
    public void setFragmentBundle(final Bundle fragmentBundle) {
        this.fragmentBundle = fragmentBundle;
    }

    /**
     * Gets data to be passed between fragments.
     * @return fragmentBundle fragment data
     */
    public Bundle getFragmentBundle() {
        return this.fragmentBundle;
    }

    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Give a reason why location services are required. Once the user acknowledges the
                // explanation, permission prompt should show
                new AlertDialog.Builder(this)
                        .setTitle("Location Permission Required")
                        .setMessage("Radford Navigator requires Location Services, please grant permission to use location functionality.")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // The following should prompt the user for permissions after the explanation above
                                ActivityCompat.requestPermissions(MainActivity.this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION);
                            }
                        })
                        .create()
                        .show();

            } else {
                // Request the permission on first-time startup and permissions have not yet been
                // allowed or denied.
                ActivityCompat.requestPermissions(this,
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

                } else {

                    // If permission is denied, this displays to the user a confirmation.
                    // The app shows the map activity currently, but does not get the user's location.
                    Toast.makeText(this, "Location permission denied", Toast.LENGTH_LONG).show();
                }
                return;
            }

            // Other permission requests can be added here. Such as permission to store locally
            // if we want to store saved locations.

        }
    }

}
