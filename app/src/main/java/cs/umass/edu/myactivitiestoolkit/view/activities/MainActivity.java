package cs.umass.edu.myactivitiestoolkit.view.activities;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import com.google.android.material.tabs.TabLayout;
import androidx.legacy.app.FragmentPagerAdapter;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.widget.TextView;

import cs.umass.edu.myactivitiestoolkit.R;
import cs.umass.edu.myactivitiestoolkit.constants.Constants;
import cs.umass.edu.myactivitiestoolkit.view.fragments.AboutFragment;
import cs.umass.edu.myactivitiestoolkit.view.fragments.AudioFragment;
import cs.umass.edu.myactivitiestoolkit.view.fragments.ExerciseFragment;
import cs.umass.edu.myactivitiestoolkit.view.fragments.HeartRateFragment;
import cs.umass.edu.myactivitiestoolkit.view.fragments.LocationsFragment;
import cs.umass.edu.myactivitiestoolkit.view.fragments.SettingsFragment;

/**
 * The main activity is the entry point for the application. It is the primary UI and allows
 * the user to interact with the system.
 *
 * To help you organize your UI, we've grouped health aspects together into tabs. Swipe from
 * tab to tab to showcase the work that you've done.
 *
 * Most of the work you do will be in the individual fragments, services and helper classes.
 * You are not required to change anything in the main activity, although displaying status
 * messages appropriately may be useful and you're more than welcome to play around with the
 * tab layout.
 *
 * @author Sean Noran
 */
public class MainActivity extends AppCompatActivity {

    @SuppressWarnings("unused")
    /** used for debugging purposes */
    private static final String TAG = MainActivity.class.getName();

    /**
     * Defines all available tabs in the main UI. For help on enums,
     * see the <a href="https://docs.oracle.com/javase/tutorial/java/javaOO/enum.html">Java documentation</a>.
     *
     * Each enum constant is parameterized with the class of the fragment associated
     * with it. The {@link #getPageNumber()} and {@link #getTitle()} methods define
     * which tab the fragment sits in and the tab text displayed to the user.
     *
     * If you wish to add another tab, e.g. for your final project, just follow the same setup.
     */
    public enum PAGES {
        MOTION_DATA(ExerciseFragment.class) {
            @Override
            public String getTitle() {
                return "My Exercise";
            }

            @Override
            public int getPageNumber() {
                return 0;
            }
        },
        AUDIO_DATA(AudioFragment.class) {
            @Override
            public String getTitle() {
                return "My Friends";
            }

            @Override
            public int getPageNumber() {
                return 1;
            }
        },
        PPG_DATA(HeartRateFragment.class) {
            @Override
            public String getTitle() {
                return "My Heart";
            }

            @Override
            public int getPageNumber() {
                return 2;
            }
        },
        LOCATION_DATA(LocationsFragment.class) {
            @Override
            public String getTitle() {
                return "My Locations";
            }

            @Override
            public int getPageNumber() {
                return 3;
            }
        },
        SETTINGS(SettingsFragment.class) {
            @Override
            public String getTitle() {
                return "Settings";
            }

            @Override
            public int getPageNumber() {
                return 4;
            }
        },
        ABOUT(AboutFragment.class) {
            @Override
            public String getTitle() {
                return "About";
            }

            @Override
            public int getPageNumber() {
                return 5;
            }
        };

        /**
         * Indicates the title of the page. This will be displayed in the tab.
         * Default is the enum name, e.g. "ABOUT". Override this to return a different title.
         * @return the page title displayed in the tab
         */
        public String getTitle(){
            return name();
        }

        /**
         * Returns the fragment associated with the page
         * @return Fragment object, null if an instantiate error occurs.
         */
        public Fragment getFragment(){
            try {
                return fragment.newInstance();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch(IllegalAccessException e) {
                Log.d(TAG, "Cannot instantiate fragment. Constructor may be private.");
                e.printStackTrace();
            }
            return null;
        }

        /**
         * Indicates the page number of the page. If omitted, it will return
         * its position in the enum. Override this to specify a different page number.
         * @return the page number
         */
        public int getPageNumber(){
            return ordinal();
        }

        /**
         * Returns the number of pages available.
         * @return the length of {@link #values()}
         */
        static int getCount(){
            return values().length;
        }

        /**
         * Constructor for a page. It requires a fragment class which defines the fragment
         * that will be displayed in the tab.
         * @param fragment class type that extends Fragment
         */
        PAGES(Class<? extends Fragment> fragment){
            this.fragment = fragment;
        }

        /**
         * The fragment class associated with the enum type.
         */
        private final Class<? extends Fragment> fragment;
    }

    /** Displays status messages, e.g. connection station. **/
    private TextView txtStatus;

    @Override
    protected void onStart() {
        super.onStart();

        LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(this);
        //the intent filter specifies the messages we are interested in receiving
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.ACTION.BROADCAST_MESSAGE);
        filter.addAction(Constants.ACTION.BROADCAST_STATUS);
        broadcastManager.registerReceiver(receiver, filter);
    }

    @Override
    protected void onStop() {
        LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(this);
        try {
            broadcastManager.unregisterReceiver(receiver);
        }catch (IllegalArgumentException e){
            e.printStackTrace();
        }
        super.onStop();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        /* Maintains the tabs and the tab layout interactions. */
        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        viewPager.setAdapter(new FragmentPagerAdapter(getFragmentManager()) {
            private final String[] tabTitles = new String[PAGES.getCount()];
            private final Fragment[] fragments = new Fragment[PAGES.getCount()];
            //instance initializer:
            {
                for (PAGES page : PAGES.values()) {
                    tabTitles[page.getPageNumber()] = page.getTitle();
                    fragments[page.getPageNumber()] = page.getFragment();
                }
            }

            @Override
            public android.app.Fragment getItem(int position) {
                return fragments[position];
            }

            @Override
            public CharSequence getPageTitle(int position) {
                return tabTitles[position];
            }

            @Override
            public int getCount() {
                return PAGES.getCount();
            }
        });
        TabLayout tabLayout = (TabLayout) findViewById(R.id.sliding_tabs);
        assert tabLayout != null;
        tabLayout.setupWithViewPager(viewPager);

        txtStatus = (TextView) findViewById(R.id.status);

        // if the activity was started by clicking a notification, then the intent contains the
        // notification ID and can be used to set the proper tab.
        if (getIntent() != null) {
            int notificationID = getIntent().getIntExtra(Constants.KEY.NOTIFICATION_ID, Constants.NOTIFICATION_ID.ACCELEROMETER_SERVICE);
            switch (notificationID){
                case Constants.NOTIFICATION_ID.ACCELEROMETER_SERVICE:
                    viewPager.setCurrentItem(PAGES.MOTION_DATA.getPageNumber());
                    break;
                case Constants.NOTIFICATION_ID.AUDIO_SERVICE:
                    viewPager.setCurrentItem(PAGES.AUDIO_DATA.getPageNumber());
                    break;
                case Constants.NOTIFICATION_ID.LOCATION_SERVICE:
                    viewPager.setCurrentItem(PAGES.LOCATION_DATA.getPageNumber());
                    break;
                case Constants.NOTIFICATION_ID.PPG_SERVICE:
                    viewPager.setCurrentItem(PAGES.PPG_DATA.getPageNumber());
                    break;
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    /**
     * Shows a removable status message at the bottom of the application.
     * @param message the status message shown
     */
    public void showStatus(String message){
        txtStatus.setText(message);
    }

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() != null) {
                if (intent.getAction().equals(Constants.ACTION.BROADCAST_MESSAGE)){
                    int message = intent.getIntExtra(Constants.KEY.MESSAGE, -1);
                    switch (message){
                        case Constants.MESSAGE.ACCELEROMETER_SERVICE_STARTED:
                            showStatus(getString(R.string.accelerometer_started));
                            break;
                        case Constants.MESSAGE.ACCELEROMETER_SERVICE_STOPPED:
                            showStatus(getString(R.string.accelerometer_stopped));
                            break;
                        case Constants.MESSAGE.AUDIO_SERVICE_STARTED:
                            showStatus(getString(R.string.audio_started));
                            break;
                        case Constants.MESSAGE.AUDIO_SERVICE_STOPPED:
                            showStatus(getString(R.string.audio_stopped));
                            break;
                        case Constants.MESSAGE.LOCATION_SERVICE_STARTED:
                            showStatus(getString(R.string.location_started));
                            break;
                        case Constants.MESSAGE.LOCATION_SERVICE_STOPPED:
                            showStatus(getString(R.string.location_stopped));
                            break;
                        case Constants.MESSAGE.PPG_SERVICE_STARTED:
                            showStatus(getString(R.string.ppg_started));
                            break;
                        case Constants.MESSAGE.PPG_SERVICE_STOPPED:
                            showStatus(getString(R.string.ppg_stopped));
                            break;
                        case Constants.MESSAGE.BAND_SERVICE_STARTED:
                            showStatus(getString(R.string.band_started));
                            break;
                        case Constants.MESSAGE.BAND_SERVICE_STOPPED:
                            showStatus(getString(R.string.band_stopped));
                            break;
                    }
                } else if (intent.getAction().equals(Constants.ACTION.BROADCAST_STATUS)){
                    String message = intent.getStringExtra(Constants.KEY.STATUS);
                    if (message != null) {
                        showStatus(message);
                    }
                }
            }
        }
    };
}
