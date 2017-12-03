package com.radfordstemnav.navigation;

import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.amazonaws.mobile.auth.core.IdentityManager;
import com.amazonaws.mobile.auth.core.IdentityProvider;
import com.radfordstemnav.R;

import static com.radfordstemnav.R.string.app_name;

public class NavigationDrawer {

    /* The navigation drawer layout view control. */
    private DrawerLayout drawerLayout;

    /**
     * Constructs the Navigation Drawer.
     *
     * @param activity             the activity that will contain this navigation drawer.
     * @param toolbar              the toolbar the activity is using.
     * @param layout               the DrawerLayout for this navigation drawer.
     * @param drawerItemsContainer the parent view group for the navigation drawer items.
     */
    public NavigationDrawer(final AppCompatActivity activity,
                            final Toolbar toolbar,
                            final DrawerLayout layout,
                            final ListView drawerItemsContainer,
                            final int fragmentContainerId) {
        // Keep a reference to the activity containing this navigation drawer.
        AppCompatActivity containingActivity = activity;
        /* The view group that will contain the navigation drawer menu items. */
        ListView drawerItems = drawerItemsContainer;
        this.drawerLayout = layout;
        /* The id of the fragment container. */
        int fragmentContainerId1 = fragmentContainerId;

        // Create the navigation drawer toggle helper.
        /* The helper class used to toggle the left navigation drawer open and closed. */
        ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle(activity, drawerLayout, toolbar,
                app_name, app_name) {

            @Override
            public void syncState() {
                super.syncState();
                updateUserName(activity);
                // TODO user photo
                //updateUserImage(activity);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                updateUserName(activity);
                // TODO user photo
               // updateUserImage(activity);
            }
        };

        // Set the listener to allow a swipe from the screen edge to bring up the navigation drawer.
        drawerLayout.addDrawerListener(drawerToggle);

        // Display the home button on the toolbar that will open the navigation drawer.
        final ActionBar supportActionBar = containingActivity.getSupportActionBar();
        supportActionBar.setDisplayHomeAsUpEnabled(true);
        supportActionBar.setHomeButtonEnabled(true);

        // Switch to display the hamburger icon for the home button.
        drawerToggle.syncState();
    }

    private void updateUserName(final AppCompatActivity activity) {
        final IdentityManager identityManager =
                IdentityManager.getDefaultIdentityManager();
        final IdentityProvider identityProvider =
                identityManager.getCurrentIdentityProvider();

        final TextView userNameView = (TextView) activity.findViewById(R.id.userName);

        if (identityProvider == null) {
            // Not signed in
            userNameView.setText("Guest");
            userNameView.setBackgroundColor(activity.getResources().getColor(R.color.nav_drawer_no_user_background));
            return;
        }

        if (identityManager.isUserSignedIn()) {
            userNameView.setText("Authenticated");
        }
    }


     // TODO for later implementation if we want to incorporate a photo (unnecessary)
//    private void updateUserImage(final AppCompatActivity activity) {
//
//        final IdentityManager identityManager =
//                IdentityManager.getDefaultIdentityManager();
//        final IdentityProvider identityProvider =
//                identityManager.getCurrentIdentityProvider();
//
//
//        /
//        if (identityProvider == null) {
//            // Not signed in
//            if (Build.VERSION.SDK_INT < 22) {
//            } else {
//            }
//
//            return;
//        }
//    }


    /**
     * Closes the navigation drawer.
     */
    public void closeDrawer() {
        drawerLayout.closeDrawers();
    }

    /**
     * @return true if the navigation drawer is open, otherwise false.
     */
    public boolean isDrawerOpen() {
        return drawerLayout.isDrawerOpen(GravityCompat.START);
    }
}
