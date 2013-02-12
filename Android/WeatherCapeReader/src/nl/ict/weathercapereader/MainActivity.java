package nl.ict.weathercapereader;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import nl.ict.aapbridge.aap.UsbConnection;
import nl.ict.aapbridge.bridge.AccessoryBridge;
import nl.ict.aapbridge.bridge.ServiceRequestException;
import nl.ict.aapbridge.dbus.DbusHandler;
import nl.ict.aapbridge.dbus.DbusSignals;
import nl.ict.aapbridge.dbus.RemoteDbusException;
import nl.ict.aapbridge.dbus.RemotePayloadException;
import nl.ict.aapbridge.dbus.message.DbusMessage;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.model.TimeSeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import android.os.Bundle;
import android.os.Message;
import android.app.Activity;
import android.graphics.Color;
import android.util.Log;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.Toast;

public class MainActivity extends Activity {
	
	public static final String TAG = "WeatherCapeReader";
	
	public static AccessoryBridge aapbridge;
	
	DbusHandler dbushandler = new DbusHandler() {
		@Override
		public void handleMessage(Message msg) {
			DbusMessage signal = (DbusMessage) msg.obj;
			try {
				Object[] values = signal.getValues();
				String sensorName = (String) values[0];
				double sensorValue = (Double) values[1];
				datasetMap.get(sensorName).add(System.currentTimeMillis(), sensorValue);
				mChartView.repaint();
			} catch (Exception e) {
				String errormsg = "Exception when extracting values from d-bus message" + e.getLocalizedMessage();
				Log.e(TAG, errormsg, e);
				Toast.makeText(MainActivity.this, errormsg, Toast.LENGTH_LONG).show();
			}
		}
	};
	
	private DbusSignals signals;
	private GraphicalView mChartView;
	private XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
	private Map<String, TimeSeries> datasetMap = new HashMap<String, TimeSeries>();
	private XYMultipleSeriesRenderer renderers = new XYMultipleSeriesRenderer();
	private Map<String, XYSeriesRenderer> renderersMap = new HashMap<String, XYSeriesRenderer>();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
        if(aapbridge == null || !aapbridge.isOpen())
        {
			try {
				aapbridge = new AccessoryBridge(UsbConnection.easyConnect(getApplicationContext()));
			} catch (IOException e) {
				Log.e(TAG, "Could not setup aapbridge", e);
				Toast.makeText(this, "Could not setup aapbridge: "+e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
				finish();
			}
        }
		
        try {
			signals = aapbridge.createDbusSignal(dbushandler, "nl.ict.sensors", "/nl/ict/sensors", "nl.ict.sensors", "Sensor");
		} catch (Exception e) {
			Log.e(TAG, "Could not setup DbusSignal", e);
			Toast.makeText(this, "Could not setup DbusSignal: "+e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
			finish();
		}
        
        TimeSeries series;
		XYSeriesRenderer serieRenderer;
		
		series = new TimeSeries("lux1");
		dataset.addSeries(series);
		datasetMap.put("lux1", series);
		serieRenderer = new XYSeriesRenderer();
		serieRenderer.setColor(Color.MAGENTA);
		renderers.addSeriesRenderer(serieRenderer);
		renderersMap.put("lux1", serieRenderer);
		
		series = new TimeSeries("humidity1");
		dataset.addSeries(series);
		datasetMap.put("humidity1", series);
		serieRenderer = new XYSeriesRenderer();
		serieRenderer.setColor(Color.GREEN);
		renderers.addSeriesRenderer(serieRenderer);
		renderersMap.put("humidity1", serieRenderer);
		
		series = new TimeSeries("temp1");
		dataset.addSeries(series);
		datasetMap.put("temp1", series);
		serieRenderer = new XYSeriesRenderer();
		serieRenderer.setColor(Color.RED);
		renderers.addSeriesRenderer(serieRenderer);
		renderersMap.put("temp1", serieRenderer);
		
		series = new TimeSeries("pressure0");
		dataset.addSeries(series);
		datasetMap.put("pressure0", series);
		serieRenderer = new XYSeriesRenderer();
		renderers.addSeriesRenderer(serieRenderer);
		renderersMap.put("pressure0", serieRenderer);
		
		series = new TimeSeries("temp0");
		dataset.addSeries(series);
		datasetMap.put("temp0", series);
		serieRenderer = new XYSeriesRenderer();
		serieRenderer.setColor(Color.YELLOW);
		renderers.addSeriesRenderer(serieRenderer);
		renderersMap.put("temp0", serieRenderer);
		
		renderers.setZoomEnabled(true);
		renderers.setZoomButtonsVisible(true);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		if (mChartView == null) {
			mChartView = ChartFactory.getTimeChartView(this, dataset, renderers, null);
			LinearLayout layout = (LinearLayout) findViewById(R.id.chart);
			layout.addView(mChartView, new LayoutParams(
					LayoutParams.MATCH_PARENT,
			        LayoutParams.MATCH_PARENT));
		}
	}
}
