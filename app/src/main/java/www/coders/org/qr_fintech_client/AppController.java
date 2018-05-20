package www.coders.org.qr_fintech_client;

import android.app.Application;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;

public class AppController extends Application {

	public static final String TAG = AppController.class.getSimpleName();

	private RequestQueue mRequestQueue;

	private static AppController mInstance;

	@Override
	public void onCreate() {
		super.onCreate();
		mInstance = this;
	}

	public static synchronized AppController getInstance() {
		return mInstance;
	}

	public RequestQueue getRequestQueue() {
		if (mRequestQueue == null) {
			mRequestQueue = Volley.newRequestQueue(getApplicationContext());
		}

		return mRequestQueue;
	}

	public <T> void addToRequestQueue(Request<T> req, String tag) {
		req.setTag(TextUtils.isEmpty(tag) ? TAG : tag);
		getRequestQueue().add(req);
	}

	public <T> void addToRequestQueue(Request<T> req) {
		req.setTag(TAG);
		getRequestQueue().add(req);
	}

	public void cancelPendingRequests(Object tag) {
		if (mRequestQueue != null) {
			mRequestQueue.cancelAll(tag);
		}
	}

	public void sendImage(Bitmap bitmap, String url) throws IOException {
		// 이미지
	// 기타 필요한 내용
		String attachmentName = "bitmap";
		String attachmentFileName = "bitmap.bmp";
		String crlf = "\r\n";
		String twoHyphens = "--";
		String boundary =  "*****";
		URL u = null;
		try {
			u = new URL(url);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}

	// request 준비
		HttpURLConnection httpUrlConnection = null;
		httpUrlConnection = (HttpURLConnection) u.openConnection();
		httpUrlConnection.setUseCaches(false);
		httpUrlConnection.setDoOutput(true);

		httpUrlConnection.setRequestMethod("POST");
		httpUrlConnection.setRequestProperty("Connection", "Keep-Alive");
		httpUrlConnection.setRequestProperty("Cache-Control", "no-cache");
		httpUrlConnection.setRequestProperty(
				"Content-Type", "multipart/form-data;boundary=" + boundary);

	// content wrapper시작
		DataOutputStream request = new DataOutputStream(
				httpUrlConnection.getOutputStream());

		request.writeBytes(twoHyphens + boundary + crlf);
		request.writeBytes("Content-Disposition: form-data; name=\"" +
				attachmentName + "\";filename=\"" +
				attachmentFileName + "\"" + crlf);
		request.writeBytes(crlf);

	// Bitmap을 ByteBuffer로 전환
		byte[] pixels = new byte[bitmap.getWidth() * bitmap.getHeight()];
		for (int i = 0; i < bitmap.getWidth(); ++i) {
			for (int j = 0; j < bitmap.getHeight(); ++j) {
				//we're interested only in the MSB of the first byte,
				//since the other 3 bytes are identical for B&W images
				pixels[i + j] = (byte) ((bitmap.getPixel(i, j) & 0x80) >> 7);
			}
		}
		request.write(pixels);

	// content wrapper종료
		request.writeBytes(crlf);
		request.writeBytes(twoHyphens + boundary + twoHyphens + crlf);

	// buffer flush
		request.flush();
		request.close();

	// Response받기
		InputStream responseStream = new
				BufferedInputStream(httpUrlConnection.getInputStream());
		BufferedReader responseStreamReader =
				new BufferedReader(new InputStreamReader(responseStream));
		String line = "";
		StringBuilder stringBuilder = new StringBuilder();
		while ((line = responseStreamReader.readLine()) != null) {
			stringBuilder.append(line).append("\n");
		}
		responseStreamReader.close();
		String response = stringBuilder.toString();


	//Response stream종료
		responseStream.close();

// connection종료
		httpUrlConnection.disconnect();
	}
}
