package nl.vu.cs.cn;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import nl.vu.cs.cn.TCP.Socket;

public class Chat extends Activity {

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
        try {
            Socket a = new TCP(1).socket((short)50);
			Log.i("test", "tipota");
        }catch (Exception e){
			Log.i("error", e.getMessage());
			Log.i("test", "tipota");
        }
		// Connect various GUI components to the networking stack.
	}

}
