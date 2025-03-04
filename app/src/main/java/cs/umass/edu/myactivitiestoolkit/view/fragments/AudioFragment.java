package cs.umass.edu.myactivitiestoolkit.view.fragments;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Toast;

import cs.umass.edu.myactivitiestoolkit.R;
import cs.umass.edu.myactivitiestoolkit.constants.Constants;
import cs.umass.edu.myactivitiestoolkit.services.AudioService;
import cs.umass.edu.myactivitiestoolkit.services.AccelerometerService;
import cs.umass.edu.myactivitiestoolkit.services.PPGService;
import cs.umass.edu.myactivitiestoolkit.services.ServiceManager;
import cs.umass.edu.myactivitiestoolkit.util.PermissionsUtil;

/**
 * Fragment used for interacting with the {@link AudioService}. We have already set up
 * the permissions request for you. We also display the spectrogram of the incoming audio
 * buffer as a heat map on an image view. You should update the UI to display the current
 * speaker. You must set up the fragment to listen for speaker identification messages that
 * you send from the audio service. You may make any other UI changes as you please.
 *
 * @author CS390MB
 *
 * @see AudioService
 */
public class AudioFragment extends Fragment {

    @SuppressWarnings("unused")
    /** Used during debugging to identify logs by class */
    private static final String TAG = AudioFragment.class.getName();

    /** Request code required for obtaining audio recording permission. **/
    private static final int AUDIO_PERMISSION_REQUEST_CODE = 5;

    /** The image displaying the audio spectrogram. **/
    private ImageView imgSpectrogram;

    /** The switch which toggles the {@link AudioService}. **/
    private Switch switchRecord;

    /** Reference to the service manager which communicates to the {@link PPGService}. **/
    private ServiceManager serviceManager;

    /**
     * The receiver listens for messages from the {@link AccelerometerService}, e.g. was the
     * service started/stopped, and updates the status views accordingly. It also
     * listens for sensor data and displays the sensor readings to the user.
     */
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() != null) {
                if (intent.getAction().equals(Constants.ACTION.BROADCAST_MESSAGE)) {
                    int message = intent.getIntExtra(Constants.KEY.MESSAGE, -1);
                    if (message == Constants.MESSAGE.AUDIO_SERVICE_STOPPED){
                        switchRecord.setChecked(false);
                    }
                } else if (intent.getAction().equals(Constants.ACTION.BROADCAST_SPECTROGRAM)){
                    double[][] spectrogram = (double[][]) intent.getSerializableExtra(Constants.KEY.SPECTROGRAM);
                    updateSpectrogram(spectrogram);
                }
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        serviceManager = ServiceManager.getInstance(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_audio, container, false);
        switchRecord = (Switch) rootView.findViewById(R.id.switchMicrophone);
        switchRecord.setChecked(serviceManager.isServiceRunning(AudioService.class));
        switchRecord.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean enable) {
                if (enable){
                    requestPermissions();
                } else {
                    serviceManager.stopSensorService(AudioService.class);
                }
            }
        });
        imgSpectrogram = (ImageView) rootView.findViewById(R.id.imgSpectrogram);
        return rootView;
    }

    /**
     * When the fragment starts, register a {@link #receiver} to receive messages from the
     * {@link AudioService}. The intent filter defines messages we are interested in receiving.
     * <br><br>
     *
     * Unlike the {@link ExerciseFragment} and {@link HeartRateFragment}, we do not visualize
     * the raw data. For this reason, there is no need to listen for it from the main UI. We
     * would, however, like to display a spectrogram of the audio data. To do this, we listen for
     * {@link Constants.ACTION#BROADCAST_SPECTROGRAM}.
     * <br><br>
     *
     * Lastly to update the state of the audio switch properly, we listen for additional
     * messages, using {@link Constants.ACTION#BROADCAST_MESSAGE}.
     *
     * @see Constants.ACTION
     * @see IntentFilter
     * @see LocalBroadcastManager
     * @see #receiver
     */
    @Override
    public void onStart() {
        super.onStart();

        LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(getActivity());
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.ACTION.BROADCAST_MESSAGE);
        filter.addAction(Constants.ACTION.BROADCAST_SPECTROGRAM);
        broadcastManager.registerReceiver(receiver, filter);
    }

    /**
     * When the fragment stops, e.g. the user closes the application or opens a new activity,
     * then we should unregister the {@link #receiver}.
     */
    @Override
    public void onStop() {
        LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(getActivity());
        try {
            broadcastManager.unregisterReceiver(receiver);
        }catch (IllegalArgumentException e){
            e.printStackTrace();
        }
        super.onStop();
    }

    /**
     * Called when the user has granted audio recording permission.
     */
    public void onPermissionGranted(){
        serviceManager.startSensorService(AudioService.class);
    }

    /**
     * Request permissions required for video recording. These include
     * {@link android.Manifest.permission#WRITE_EXTERNAL_STORAGE WRITE_EXTERNAL_STORAGE},
     * and {@link android.Manifest.permission#CAMERA CAMERA}. If audio is enabled, then
     * the {@link android.Manifest.permission#RECORD_AUDIO RECORD_AUDIO} permission is
     * additionally required.
     */
    @TargetApi(Build.VERSION_CODES.M)
    public void requestPermissions(){
        String[] permissions = new String[]{Manifest.permission.RECORD_AUDIO};

        if (!PermissionsUtil.hasPermissionsGranted(getActivity(), permissions)) {
            requestPermissions(permissions, AUDIO_PERMISSION_REQUEST_CODE);
            return;
        }
        onPermissionGranted();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case AUDIO_PERMISSION_REQUEST_CODE: {
                //If the request is cancelled, the result array is empty.
                if (grantResults.length == 0) return;

                for (int i = 0; i < permissions.length; i++){
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED){
                        switch (permissions[i]) {
                            case Manifest.permission.RECORD_AUDIO:
                                Toast.makeText(getActivity(), "Audio permission required!", Toast.LENGTH_LONG).show();
                                return;
                            default:
                                return;
                        }
                    }
                }
                onPermissionGranted();
            }
        }
    }

    /**
     * Converts the spectrogram values into a heat map and projects the pixels onto a bitmap.
     * @param spectrogram the spectrogram values as a 2D array.
     */
    private void updateSpectrogram(double[][] spectrogram){
        int width = spectrogram.length;
        int height = spectrogram[0].length;

        int[] rgbValues = new int[width * height];

        double max = 0, min = 0;
        for (int j = 0; j < height; j++) {
            for (double[] row : spectrogram) {
                if (row[j] > max) {
                    max = row[j];
                }
                if (row[j] < min) {
                    min = row[j];
                }
            }
        }
        int counter = 0;
        for (int j = 0; j < height/2; j++) {
            for (double[] row : spectrogram) {
                rgbValues[counter++] = heatMap(min, max, row[j]);
            }
        }

        Bitmap bitmap = Bitmap.createBitmap(rgbValues, width, height, Bitmap.Config.ARGB_8888);
        imgSpectrogram.setImageBitmap(bitmap);
    }

    /**
     * Converts the value to a corresponding heat map color
     * @param minimum the minimum bound on the value
     * @param maximum the maximum bound on the value
     * @param value the value, within the range [minimum, maximum]
     * @return an RGB color identifier
     *
     * @see <a href="http://stackoverflow.com/questions/20792445/calculate-rgb-value-for-a-range-of-values-to-create-heat-map">Aldorado's answer.</a>
     */
    private int heatMap(double minimum, double maximum, double value) {
        double ratio = 2 * (value - minimum) / (maximum - minimum);
        int b = (int) Math.max(0, 255 * (1 - ratio));
        int r = (int) Math.max(0, 255 * (ratio - 1));
        int g = 255 - b - r;
        return (r<<16|g<<8|b|255<<24);
    }
}