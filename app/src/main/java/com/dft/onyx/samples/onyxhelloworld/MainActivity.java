package com.dft.onyx.samples.onyxhelloworld;

import org.opencv.android.OpenCVLoader;

import com.dft.onyx.FingerprintTemplate;
import com.dft.onyxcamera.licensing.License;
import com.dft.onyxcamera.licensing.LicenseException;
import com.dft.onyx.core;
import com.dft.onyxcamera.ui.CaptureConfiguration;
import com.dft.onyxcamera.ui.CaptureConfigurationBuilder;
import com.dft.onyxcamera.ui.OnyxFragment;
import com.dft.onyxcamera.ui.OnyxFragment.FingerprintTemplateCallback;

import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.util.Log;
import android.view.Menu;
import android.widget.Toast;

import com.dft.onyx.samples.onyxhelloworld.R;

public class MainActivity extends Activity {

	private static final String TAG = MainActivity.class.getSimpleName();
	private FingerprintTemplate mCurrentTemplate = null;
	private FingerprintTemplate mPreviousTemplate = null;
	private OnyxFragment mFragment = null;

	private OnyxFragment.ErrorCallback mErrorCallback = new OnyxFragment.ErrorCallback() {

		@Override
		public void onError(Error arg0, String arg1, Exception arg2) {
			switch(arg0) {
			case AUTOFOCUS_FAILURE:
				mFragment.startOneShotAutoCapture();
				break;
			default:
				Log.d(TAG, "Error occurred: " + arg1);
				break;
			}
		}
    	
    };

	private FingerprintTemplateCallback mTemplateCallback = new FingerprintTemplateCallback() {
	
		@Override
		public void onFingerprintTemplateReady(FingerprintTemplate fingerprintTemplate) {
			if(mCurrentTemplate == null) {
				mCurrentTemplate = fingerprintTemplate;
			} else {
				mPreviousTemplate = mCurrentTemplate;
				mCurrentTemplate = fingerprintTemplate;
				float matchScore;
				try {
					matchScore = core.verify(mPreviousTemplate, mCurrentTemplate);
					if(matchScore >= 0.1) {
						Toast.makeText(
								getApplicationContext(),
								"Successful match: " + matchScore,
								Toast.LENGTH_SHORT).show();
					} else {
						Toast.makeText(
								getApplicationContext(),
								"Failed to match: " + matchScore,
								Toast.LENGTH_SHORT).show();
					}
				} catch (Exception e) {
					Log.e(TAG, "Verify failed: " + e.getMessage());
				}
				
			}
			mFragment.startOneShotAutoCapture();
		}
		
	};

	static {
		if(!OpenCVLoader.initDebug()) {
			Log.d(TAG, "Unable to load OpenCV!");
		} else {
			Log.i(TAG, "OpenCV loaded successfully");
			core.initOnyx();
		}
	}
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mFragment = (OnyxFragment)getFragmentManager()
        		.findFragmentById(R.id.onyx_frag);
        CaptureConfiguration captureConfig = new CaptureConfigurationBuilder()
        	.setFingerprintTemplateCallback(mTemplateCallback)
        	.buildCaptureConfiguration();
        mFragment.setCaptureConfiguration(captureConfig);
		mFragment.setErrorCallback(mErrorCallback);
		mFragment.startOneShotAutoCapture();
    }
    
    @Override
    protected void onResume() {
    	super.onResume();

		License lic = License.getInstance(this);
		try {
			lic.validate(getString(R.string.onyx_license));
		} catch (LicenseException e) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("License error")
				.setMessage(e.getMessage())
				.setNegativeButton("OK", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
					}
    		});
			builder.create().show();
		}
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
}
