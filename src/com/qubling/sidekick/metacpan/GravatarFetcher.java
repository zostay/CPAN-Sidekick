package com.qubling.sidekick.metacpan;

import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.qubling.sidekick.metacpan.collection.ModuleList;
import com.qubling.sidekick.metacpan.result.Module;

public class GravatarFetcher extends RemoteAPI<Module, Void, Void> {
	
	private static final float GRAVATAR_DP_SIZE = 35f;
	private static final Pattern RESIZE_GRAVATAR_PATTERN = Pattern.compile("([?&])s=[0-9]+\\b");
	
	private Context context;
	private ModuleList moduleList;
	
	public GravatarFetcher(Context context, HttpClientManager clientManager, ModuleList moduleList) {
		super(clientManager);
		
		this.context       = context;
		this.moduleList    = moduleList;
	}
	
	private Bitmap fetchBitmap(String gravatarURL) {
		
		// Calculate the pixel size of the Gravatar
		int gravatarPixelSize = Math.min(
				(int) (GRAVATAR_DP_SIZE * context.getResources().getDisplayMetrics().density + 0.5f),
				512);
		
		Matcher resizeGravatarMatcher = RESIZE_GRAVATAR_PATTERN.matcher(gravatarURL);
		String resizedGravatarURL = resizeGravatarMatcher.replaceFirst("$1s=" + gravatarPixelSize);
		
		try {
			
			// Do the request
			Log.d("AuthorByDistributionSearch", "Gravatar: " + resizedGravatarURL);
			HttpGet req = new HttpGet(resizedGravatarURL);
			HttpResponse res = getClient().execute(req);
			
			// Get the response content
			HttpEntity entity = res.getEntity();
			InputStream content = entity.getContent();
			Bitmap gravatarBitmap = BitmapFactory.decodeStream(content);
			return gravatarBitmap;
		}
		
		catch (IOException e) {
			// TODO Return a generic image when this happens
			Log.e("AuthorByDistributionSearch", "Error loading Gravatar: " + e);
			return null;
		}
	}

	@Override
	protected Void doInBackground(Module... modules) {
		
		for (Module module : modules) {
			String url = module.getAuthorGravatarURL();
			Bitmap gravatar = fetchBitmap(url);
			module.setAuthorGravatarBitmap(gravatar);
		}

		return null;
	}

	@Override
	protected void onPostExecute(Void result) {
		super.onPostExecute(result);
		
		moduleList.notifyModuleListUpdaters();
	}
}
