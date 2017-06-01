package com.example.cristiano.myteam.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.example.cristiano.myteam.R;

import com.example.cristiano.myteam.fragment.ClubProfileFragment;
import com.example.cristiano.myteam.util.Constant;

/**
 *  this activity holds a frame that contains certain fragments,
 *  which will present the club profile, members or tournaments
 */
public class ClubActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private int clubID, playerID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_club);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_club);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view_club);
        navigationView.setNavigationItemSelectedListener(this);

        Bundle bundle = getIntent().getExtras();
        if ( !bundle.containsKey(Constant.KEY_CLUB_ID) ||
                !bundle.containsKey(Constant.KEY_PLAYER_ID) ) {
            Log.e("ClubActivity","clubID/playerID not specified!");
            return;
        }
        this.clubID = bundle.getInt(Constant.KEY_CLUB_ID);
        this.playerID = bundle.getInt(Constant.KEY_PLAYER_ID);
        Log.d("CLUB_ID","="+this.clubID);
        showProfilePage();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            FragmentManager fragmentManager = getSupportFragmentManager();
            Fragment fragment = fragmentManager.findFragmentByTag(Constant.FRAGMENT_CLUB_PROFILE);
            if ( fragment != null && fragment.isVisible() ) {
                super.onBackPressed();
            } else {
                showProfilePage();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.club, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_result) {

        } else if (id == R.id.nav_stats) {

        } else if (id == R.id.nav_recordMatch) {

        } else if (id == R.id.nav_logOut) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * use a fragment to display the club's profile page
     */
    private void showProfilePage(){
        ClubProfileFragment clubProfileFragment = ClubProfileFragment.newInstance(clubID,playerID);
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_content,clubProfileFragment,Constant.FRAGMENT_CLUB_PROFILE);
        fragmentTransaction.commit();

    }
}
