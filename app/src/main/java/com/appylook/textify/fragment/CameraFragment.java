package com.appylook.textify.fragment;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;

import org.apache.http.entity.StringEntity;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.appylook.textify.R;
import com.appylook.textify.constant.TextifyServer;
import com.appylook.textify.ui.widget.CameraPreview;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

public class CameraFragment extends Fragment {
	/**
	 * The fragment argument representing the section number for this fragment.
	 */
	public static final String ARG_SECTION_NUMBER = "section_number";
	public static final String TAG = PickImageFragment.class.getSimpleName();
	private static final int PICK_FROM_GALLERY = 1;

	Button extractButton;
	ImageButton editImageButton;
	ImageButton sendImageButton;
	EditText resultTextView;
	EditText resultEditText;

	private Camera mCamera;
    private CameraPreview mPreview;

	public CameraFragment() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Create a new TextView and set its text to the fragment's section
		// number argument value.
		// Inflate the layout for this fragment
		return inflater.inflate(R.layout.fragment_camera, container, false);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		// Create an instance of Camera
        mCamera = getCameraInstance();

        // Create our Preview view and set it as the content of our activity.
        mPreview = new CameraPreview(getActivity(), mCamera);
        FrameLayout preview = (FrameLayout) getActivity().findViewById(R.id.cameraPreview);
        preview.addView(mPreview);
        
		extractButton = (Button) getActivity().findViewById(R.id.extractButton);
		extractButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				
			}
		});
		resultTextView = (EditText) getActivity().findViewById(
				R.id.resultTextView);
		resultTextView.setOnLongClickListener(new OnLongClickListener() {

			@Override
			public boolean onLongClick(View v) {
				toggleEditMode(resultTextView);
				return false;
			}
		});

		editImageButton = (ImageButton) getActivity().findViewById(
				R.id.editImageButton);
		editImageButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				toggleEditMode(resultTextView);
			}
		});

		resultEditText = (EditText) getActivity().findViewById(
				R.id.resultEditText);
		sendImageButton = (ImageButton) getActivity().findViewById(
				R.id.sendImageButton);
		sendImageButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				resultTextView.append(resultEditText.getText().toString()
						.trim());
				resultEditText.setText("");
			}
		});
	}

	public void extractText(Bitmap bitmap) throws JSONException,
			UnsupportedEncodingException {

		AsyncHttpClient client = new AsyncHttpClient();
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
		byte[] byteArray = stream.toByteArray();
		Base64.encode(byteArray, Base64.DEFAULT);

		JSONObject jsonParams = new JSONObject();
		jsonParams.put("extention", "jpeg");
		jsonParams.put("image",
				Base64.encodeToString(byteArray, Base64.DEFAULT));
		StringEntity entity = new StringEntity(jsonParams.toString());
		final ProgressBar ocrProgressBar = (ProgressBar) getActivity()
				.findViewById(R.id.ocrProgressBar);
		ocrProgressBar.setVisibility(View.VISIBLE);
		client.post(getActivity(), TextifyServer.OCR_END_POINT, entity,
				"application/json", new JsonHttpResponseHandler() {

					@Override
					public void onSuccess(JSONObject response) {
						Toast.makeText(getActivity(), response.toString(),
								Toast.LENGTH_SHORT).show();
						try {
							resultEditText.setText(response.getString("text"));
						} catch (JSONException e) {
							Log.d(TAG, "Failed " + e.getLocalizedMessage());
							e.printStackTrace();
						}
						ocrProgressBar.setVisibility(View.INVISIBLE);
					}

					@Override
					public void onFailure(Throwable arg0, JSONObject arg1) {
						super.onFailure(arg0, arg1);
						Log.d(TAG, "Failed " + arg1.toString());
						ocrProgressBar.setVisibility(View.INVISIBLE);
					}
				});
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {

		case PICK_FROM_GALLERY:

			if (resultCode == Activity.RESULT_OK) {
				if (getBitmap(data) != null) {
					Bitmap bitmap = getBitmap(data);
				} else {
					Log.d(TAG, "getBitmap retuned null");
				}
			} else {
				Log.d(TAG, "resultCode = " + resultCode);
			}

			break;

		default:
			break;
		}
	}

	private Bitmap getBitmap(Intent data) {
		Bundle extras = data.getExtras();
		if (extras != null) {
			return extras.getParcelable("data");
		} else {
			return null;
		}
	}

	private boolean toggleEditMode(EditText editText) {
		boolean state = editText.isEnabled();
		state = !state;
		editText.setEnabled(state);
		editText.setFocusableInTouchMode(state);
		editText.setFocusable(state);
		return state;
	}
	
	/** Check if this device has a camera */
	private boolean checkCameraHardware(Context context) {
	    if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
	        // this device has a camera
	        return true;
	    } else {
	        // no camera on this device
	        return false;
	    }
	}
	/** A safe way to get an instance of the Camera object. */
	public static Camera getCameraInstance(){
	    Camera c = null;
	    try {
	        c = Camera.open(); // attempt to get a Camera instance
	    }
	    catch (Exception e){
	        // Camera is not available (in use or does not exist)
	    }
	    return c; // returns null if camera is unavailable
	}
}
