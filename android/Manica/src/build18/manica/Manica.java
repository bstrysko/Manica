package build18.manica;

import java.net.URI;
import java.nio.ByteBuffer;
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

public class Manica extends IOIOActivity 
{
	private static final int NUMBER_OF_MAGNOMETERS = 3;
	private static final int NUMBER_OF_MAGNOMETER_AXIS = 3;
	
	private WebSocketClient server;
	private TextView magnometers_textviews[][];
	
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        magnometers_textviews = new TextView[NUMBER_OF_MAGNOMETERS][NUMBER_OF_MAGNOMETER_AXIS];
        
        magnometers_textviews[0][0] = (TextView)findViewById(R.id.magnometer_0_x);
        magnometers_textviews[0][1] = (TextView)findViewById(R.id.magnometer_0_y);
        magnometers_textviews[0][2] = (TextView)findViewById(R.id.magnometer_0_z);
        
        magnometers_textviews[1][0] = (TextView)findViewById(R.id.magnometer_1_x);
        magnometers_textviews[1][1] = (TextView)findViewById(R.id.magnometer_1_y);
        magnometers_textviews[1][2] = (TextView)findViewById(R.id.magnometer_1_z);
        
        magnometers_textviews[2][0] = (TextView)findViewById(R.id.magnometer_2_x);
        magnometers_textviews[2][1] = (TextView)findViewById(R.id.magnometer_2_y);
        magnometers_textviews[2][2] = (TextView)findViewById(R.id.magnometer_2_z);
        
        List<BasicNameValuePair> extraHeaders = Arrays.asList();

        server = new WebSocketClient(URI.create("ws://128.2.97.197:8080"), new Listener() 
        {
        	    @Override
        	    public void onConnect() 
        	    {
        	    }

        	    @Override
        	    public void onMessage(String message) 
        	    {
        	    }

        	    @Override
        	    public void onMessage(byte[] data) 
        	    {
        	    }

        	    @Override
        	    public void onDisconnect(int code, String reason) 
        	    {
        	    }

        	    @Override
        	    public void onError(Exception error) 
        	    {
        	    }
        }, extraHeaders);

        server.connect();
    }
	
	class Looper extends BaseIOIOLooper 
	{
		private DigitalOutput led_;
		
		private TwiMaster twi[];
		
		@Override
		public void setup() throws ConnectionLostException 
		{
			try 
			{
				led_ = ioio_.openDigitalOutput(IOIO.LED_PIN, true);	
		        
				twi = new TwiMaster[NUMBER_OF_MAGNOMETERS];
				
				for(int i = 0; i < twi.length; i++)
				{
					twi[i] = ioio_.openTwiMaster(i, TwiMaster.Rate.RATE_100KHz, false);
					init_magnometer(i);
				}		        
			} 
			catch (ConnectionLostException e) 
			{
				throw e;
			}
		}
		
		private void init_magnometer(int magnometer_id)
		{
			byte send_buffer[] = new byte[2]; 
			send_buffer[0] = 0x11;
			send_buffer[1] = (byte)0x80;
			
			byte read_buffer[] = new byte[1];
			
			try 
			{
				twi[magnometer_id].writeRead(0x0E,false,send_buffer,send_buffer.length, read_buffer, 0);
				Thread.sleep(15);
			} 
			catch (ConnectionLostException e) 
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
			catch (InterruptedException e) 
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
						
			send_buffer[0] = 0x10;
			send_buffer[1] = 0x1;
			
			try 
			{
				twi[magnometer_id].writeRead(0x0E,false,send_buffer,send_buffer.length,read_buffer,0);
			} 
			catch (ConnectionLostException e) 
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
			catch (InterruptedException e) 
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		private JSONObject read_magnometer(int magnometer_id) throws ConnectionLostException
		{
			JSONObject message = new JSONObject();
			
			try 
			{
				message.put("magnometer",magnometer_id);
			} 
			catch (JSONException e1) 
			{
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			byte send_buffer[] = new byte[1]; 				
			byte read_buffer[] = new byte[1];

			short x_data = 0;
			short y_data = 0;
			short z_data = 0;
			
			try 
			{				
				send_buffer[0] = 0x00;
				twi[magnometer_id].writeRead(0x0E,false,send_buffer,send_buffer.length, read_buffer, read_buffer.length);
				message.put("DR_STATUS",Byte.valueOf(read_buffer[0]));
				Thread.sleep(4);
				
				send_buffer[0] = 0x01;
				twi[magnometer_id].writeRead(0x0E,false,send_buffer,send_buffer.length, read_buffer, read_buffer.length);
				x_data = (short)((read_buffer[0]) << 8);
				Thread.sleep(4);
				
				send_buffer[0] = 0x02;
				read_buffer[0] = 0x2;
				twi[magnometer_id].writeRead(0x0E,false,send_buffer,send_buffer.length, read_buffer, read_buffer.length);
				x_data |= ((short)read_buffer[0] & 0x00FF);
				message.put("OUT_X",Short.valueOf(x_data));
				Thread.sleep(4);

				send_buffer[0] = 0x03;
				twi[magnometer_id].writeRead(0x0E,false,send_buffer,send_buffer.length, read_buffer, read_buffer.length);
				y_data = (short)((read_buffer[0]) << 8);
				Thread.sleep(4);

				send_buffer[0] = 0x04;
				twi[magnometer_id].writeRead(0x0E,false,send_buffer,send_buffer.length, read_buffer, read_buffer.length);
				y_data |= ((short)read_buffer[0] & 0x00FF);
				message.put("OUT_Y",Short.valueOf(y_data));
				Thread.sleep(4);
				
				send_buffer[0] = 0x05;
				twi[magnometer_id].writeRead(0x0E,false,send_buffer,send_buffer.length, read_buffer, read_buffer.length);
				z_data =  (short)((read_buffer[0]) << 8);
				Thread.sleep(4);
				
				send_buffer[0] = 0x6;
				twi[magnometer_id].writeRead(0x0E,false,send_buffer,send_buffer.length, read_buffer, read_buffer.length);
				z_data |= ((short)read_buffer[0] & 0x00FF);
				message.put("OUT_Z",Short.valueOf(z_data));
				Thread.sleep(4);
				
				send_buffer[0] = 0x07;
				twi[magnometer_id].writeRead(0x0E,false,send_buffer,send_buffer.length, read_buffer, read_buffer.length);
				message.put("WHO_AM_I",Byte.valueOf(read_buffer[0]));
				Thread.sleep(4);
				
				send_buffer[0] = 0x08;
				twi[magnometer_id].writeRead(0x0E,false,send_buffer,send_buffer.length, read_buffer, read_buffer.length);
				message.put("SYSMOD",Byte.valueOf(read_buffer[0]));
				Thread.sleep(4);
								
				return message;
			} 
			catch (InterruptedException e) 
			{
				ioio_.disconnect();
			} 
			catch (ConnectionLostException e) 
			{
				throw e;
			} 
			catch (JSONException e) 
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			return null;
		}
		
		@Override
		public void loop() throws ConnectionLostException 
		{
			try {
				JSONArray message = new JSONArray();
				
				short values[][] = new short[twi.length][NUMBER_OF_MAGNOMETER_AXIS];
				
				for(int i = 0; i < twi.length; i++)
				{
					JSONObject magnometer_values = read_magnometer(i);
					
					try 
					{
						values[i][0] = (Short)magnometer_values.get("OUT_X");
						values[i][1] = (Short)magnometer_values.get("OUT_Y");
						values[i][2] = (Short)magnometer_values.get("OUT_Z");
					} 
					catch (JSONException e) 
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					message.put(magnometer_values);
				}
				
				server.send(message.toString());
				
				Thread.sleep(5);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Override
	protected IOIOLooper createIOIOLooper() 
	{	
		return new Looper();
	}
	
	private void set_magnometers_textviews(final short[][] values) 
	{
		runOnUiThread(new Runnable() 
		{
			@Override
			public void run() 
			{
				for(int magnometer = 0; magnometer < NUMBER_OF_MAGNOMETERS; magnometer++)
				{
					for(int axis = 0; axis < NUMBER_OF_MAGNOMETER_AXIS; axis++)
					{
						magnometers_textviews[magnometer][axis].setText(values[magnometer][axis]);
					}
				}
			}
		});
	}
}