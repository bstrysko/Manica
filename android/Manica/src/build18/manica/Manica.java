package build18.manica;

import java.net.URI;
import java.util.Arrays;
import java.util.List;

import com.codebutler.android_websockets.WebSocketClient;
import com.codebutler.android_websockets.WebSocketClient.Listener;

import build18.manica.R;
import ioio.lib.api.AnalogInput;
import ioio.lib.api.DigitalOutput;
import ioio.lib.api.IOIO;
import ioio.lib.api.PwmOutput;
import ioio.lib.api.TwiMaster;
import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.util.BaseIOIOLooper;
import ioio.lib.util.IOIOLooper;
import ioio.lib.util.android.IOIOActivity;
import android.content.Context;
import android.os.Bundle;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Manica extends IOIOActivity {
	private TextView textView_;
	private SeekBar seekBar_;
	private ToggleButton toggleButton_;
	private WebSocketClient server;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        textView_ = (TextView)findViewById(R.id.TextView);
        seekBar_ = (SeekBar)findViewById(R.id.SeekBar);
        toggleButton_ = (ToggleButton)findViewById(R.id.ToggleButton);
        
        enableUi(false);
        List<BasicNameValuePair> extraHeaders = Arrays.asList();

        server = new WebSocketClient(URI.create("ws://128.2.97.197:8080"), new Listener() 
        {
        	    @Override
        	    public void onConnect() {
        	    }

        	    @Override
        	    public void onMessage(String message) {
        	    }

        	    @Override
        	    public void onMessage(byte[] data) {
        	    }

        	    @Override
        	    public void onDisconnect(int code, String reason) {
        	    }

        	    @Override
        	    public void onError(Exception error) {
        	    }
        }, extraHeaders);

        server.connect();

        	// Later… 
        	/*client.send("hello!");
        	client.send(new byte[] { 0xDE, 0xAD, 0xBE, 0xEF });
        	client.disconnect();*/
    }
	
	class Looper extends BaseIOIOLooper {
		private DigitalOutput led_;
		private TwiMaster twi0;
		
		@Override
		public void setup() throws ConnectionLostException {
			try {
				led_ = ioio_.openDigitalOutput(IOIO.LED_PIN, true);	
		        twi0 = ioio_.openTwiMaster(0,TwiMaster.Rate.RATE_100KHz,false);

		        byte send_buffer[] = new byte[2]; 
				send_buffer[0] = 0x11;
				send_buffer[1] = (byte)0x80;
				
				byte read_buffer[] = new byte[1];
				
				twi0.writeRead(0x0E,false,send_buffer,send_buffer.length, read_buffer, 0);
				
				Thread.sleep(15);
				
				send_buffer[0] = 0x10;
				send_buffer[1] = 0x1;
				
				twi0.writeRead(0x0E,false,send_buffer,send_buffer.length,read_buffer,0);
		        
				enableUi(true);
				
				setText("Completed Setup");
			}
			catch (InterruptedException e) 
			{
				setText("Setup InterruptedException");
				ioio_.disconnect();
			} 
			catch (ConnectionLostException e) 
			{
				setText("Setup ConnectionLostException");
				enableUi(false);
				throw e;
			}
		}
		
		@Override
		public void loop() throws ConnectionLostException {
			try 
			{
				JSONObject message = new JSONObject();

				setText("Begin Loop");
				
				byte send_buffer[] = new byte[1]; 				
				byte read_buffer[] = new byte[1];
				
				send_buffer[0] = 0x00;
				twi0.writeRead(0x0E,false,send_buffer,send_buffer.length, read_buffer, read_buffer.length);
				message.put("DR_STATUS",Byte.valueOf(read_buffer[0]));
				Thread.sleep(4);
				
				short x_data = 0;
				short y_data = 0;
				short z_data = 0;
				
				send_buffer[0] = 0x01;
				//twi0.writeRead(0x0E,false,send_buffer,send_buffer.length, read_buffer, read_buffer.length);
				read_buffer[0] = 0x1;
				x_data = (short)((read_buffer[0]) << 8);
				Thread.sleep(4);
				
				send_buffer[0] = 0x02;
				read_buffer[0] = 0x2;
				//twi0.writeRead(0x0E,false,send_buffer,send_buffer.length, read_buffer, read_buffer.length);
				x_data |= ((short)read_buffer[0] & 0x00FF);
				message.put("OUT_X",Short.valueOf(x_data));
				Thread.sleep(4);

				send_buffer[0] = 0x03;
				twi0.writeRead(0x0E,false,send_buffer,send_buffer.length, read_buffer, read_buffer.length);
				y_data = (short)((read_buffer[0]) << 8);
				Thread.sleep(4);

				send_buffer[0] = 0x04;
				twi0.writeRead(0x0E,false,send_buffer,send_buffer.length, read_buffer, read_buffer.length);
				y_data |= ((short)read_buffer[0] & 0x00FF);
				message.put("OUT_Y",Short.valueOf(y_data));
				Thread.sleep(4);
				
				send_buffer[0] = 0x05;
				twi0.writeRead(0x0E,false,send_buffer,send_buffer.length, read_buffer, read_buffer.length);
				z_data =  (short)((read_buffer[0]) << 8);
				Thread.sleep(4);
				
				send_buffer[0] = 0x6;
				twi0.writeRead(0x0E,false,send_buffer,send_buffer.length, read_buffer, read_buffer.length);
				z_data |= ((short)read_buffer[0] & 0x00FF);
				message.put("OUT_Z",Short.valueOf(z_data));
				Thread.sleep(4);
				
				send_buffer[0] = 0x07;
				twi0.writeRead(0x0E,false,send_buffer,send_buffer.length, read_buffer, read_buffer.length);
				message.put("WHO_AM_I",Byte.valueOf(read_buffer[0]));
				Thread.sleep(4);
				
				send_buffer[0] = 0x08;
				twi0.writeRead(0x0E,false,send_buffer,send_buffer.length, read_buffer, read_buffer.length);
				message.put("SYSMOD",Byte.valueOf(read_buffer[0]));
				Thread.sleep(4);
				
				server.send(message.toString());
				
				setText("End Loop");
				
				Thread.sleep(1000);
				
			} catch (InterruptedException e) {
				setText("Loop  InterruptedException");
				ioio_.disconnect();
			} catch (ConnectionLostException e) {
				setText("Loop  ConnectionLostException");
				enableUi(false);
				throw e;
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Override
	protected IOIOLooper createIOIOLooper() {
		return new Looper();
	}

	private void enableUi(final boolean enable) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				seekBar_.setEnabled(enable);
				toggleButton_.setEnabled(enable);
			}
		});
	}
	
	private void setText(final String str) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				textView_.setText(str);
			}
		});
	}
}