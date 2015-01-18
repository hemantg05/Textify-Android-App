package com.appylook.textify.ui;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.Fragment;

public class ImageUtils {

	public static final String TAG = ImageUtils.class.getSimpleName();
	
	public static void pickImage(Fragment fragment, Uri uri, final int requestCode) {
		Intent intent = new Intent();
		// call android default gallery
		intent.setType("image/*");
		intent.setAction(Intent.ACTION_GET_CONTENT);
		// ******** code for crop image
		intent.putExtra("crop", "true");
		intent.putExtra("aspectX", 0);
		intent.putExtra("aspectY", 0);
		intent.putExtra("outputX", 200);
		intent.putExtra("outputY", 150);
		try {
			intent.putExtra("return-data", true);
			fragment.startActivityForResult(Intent.createChooser(intent, "Complete action using"), requestCode);
		} catch (ActivityNotFoundException e) {
			// Do nothing for now
		}
	}

	
}
