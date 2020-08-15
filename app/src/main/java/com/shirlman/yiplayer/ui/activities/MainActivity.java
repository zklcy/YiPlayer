package com.shirlman.yiplayer.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.coder.Control.VideoCheck;
import com.shirlman.yiplayer.R;
import com.shirlman.yiplayer.events.VideoEvents;
import com.shirlman.yiplayer.models.VideoGroup;
import com.shirlman.yiplayer.models.VideoInfo;
import com.shirlman.yiplayer.ui.fragments.LocalVideoFragment;
import com.shirlman.yiplayer.ui.fragments.LocalVideoGroupFragment;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import static com.coder.Control.FFmpegCmd.CMD_AddBGTrack;
import static com.coder.Control.FFmpegCmd.test;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EventBus.getDefault().register(this);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, new LocalVideoGroupFragment()).commit();

        Log.d("ZK", "Test JNI " + test());
//        ffmpegTest();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
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

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        EventBus.getDefault().unregister(this);
    }

    private Fragment getCurrentFragment() {
        Fragment currentFragment = null;

        List<Fragment> fragmentList = getSupportFragmentManager().getFragments();

        if(fragmentList != null) {
            for (Fragment fragment : fragmentList) {
                if (fragment != null && fragment.isVisible()) {
                    currentFragment = fragment;

                    break;
                }
            }
        }

        return currentFragment;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(VideoEvents.OpenVideoGroup event) {
        LocalVideoFragment localVideoFragment =  new LocalVideoFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(VideoGroup.class.getSimpleName(), event.getVideoGroup());
        localVideoFragment.setArguments(bundle);
        getSupportFragmentManager()
                .beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .hide(getCurrentFragment())
                .add(R.id.fragment_container, localVideoFragment)
                .addToBackStack(null)
                .commit();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(VideoEvents.PlayLocalVideo event) {
        VideoCheck videoCheck = new VideoCheck();
        String filename = event.getVideoInfo().getPath();
        boolean ret = videoCheck.isHaveBGTrack(filename);
        videoCheck.release();
        if(!ret){
            CMD_AddBGTrack(filename);
            Log.d(TAG, "onEventMainThread: Add background audio track "+filename);
        }else {
            Log.d(TAG, filename+" already have backgroud audio track");
        }

        Intent intent = new Intent(MainActivity.this, VideoActivity.class);
        intent.putExtra(VideoInfo.class.getSimpleName(), event.getVideoInfo());
        startActivity(intent);
    }



//    private void ffmpegTest() {
//        new Thread() {
//            @Override
//            public void run() {
//                long startTime = System.currentTimeMillis();
//                String input =
//                        Environment.getExternalStorageDirectory().getPath() + File.separator +
//                                "DCIM" + File.separator + "test.mp3";
//                String output =
//                        Environment.getExternalStorageDirectory().getPath() + File.separator +
//                                "DCIM" + File.separator + "output.mp3";
//
//                String cmd = "ffmpeg -y -i %s -vn -acodec copy -ss %s -t %s %s";
//                String result = String.format(cmd, input, "00:00:01", "00:00:10", output);
//                FFmpegCmd.runCmd(result.split(" "));
//                Log.d("FFmpegTest", "run: 耗时：" + (System.currentTimeMillis() - startTime));
//            }
//        }.start();
//    }

}
