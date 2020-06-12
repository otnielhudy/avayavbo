package com.avaya.mobilevideo.panicar;

import android.app.Activity;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Toast;

import com.avaya.mobilevideo.R;
import com.dopanic.panicarkit.lib.PARController;
//import com.dopanic.panicarkit.lib.PARFragment;
import com.dopanic.panicarkit.lib.PARFragment;
import com.dopanic.panicarkit.lib.PARPoiLabel;
import com.dopanic.panicarkit.lib.PARRadarView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class PanicARFragment extends PARFragment
{
    public static String arLocations = "";

    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        Log.d("AR:", "inside onCreate");

        PARPoiLabel label = null;
        JSONArray jsonarray = null;
        try
        {
            jsonarray = new JSONArray(arLocations);
            Log.d("AR:", "jsonarray: " + jsonarray);
            int locLen = jsonarray.length();
            for (int i = 0; i < jsonarray.length(); i++)
            {
                JSONObject obj = jsonarray.getJSONObject(i);
                String title = obj.getString("title");
                String desc = obj.getString("desc");
                double lat = Double.parseDouble(obj.getString("lat"));
                double lon = Double.parseDouble(obj.getString("lon"));
                label = createPoi(title, desc, lat, lon);
                Log.d("AR:", "POI-" + i + ": " + title);
                Log.d("AR:", "lat-" + i + ": " + lat);
                Log.d("AR:", "lon-" + i + ": " + lon);
                label.setBackgroundImageResource(R.drawable.default_poi_label);
                label.setSize(40 * (locLen + 4), 120);
                locLen--;
                PARController.getInstance().addPoi(label);
            }
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        viewLayoutId = R.layout.panicar_view;
        View view = super.onCreateView(inflater, container, savedInstanceState);
        getRadarView().setRadarRange(500.0F);
        return view;
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        inflater.inflate(R.menu.fragment_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    public boolean onOptionsItemSelected(MenuItem item)
    {
        int i = item.getItemId();
        if (i == 16908332)
        {
            getActivity().finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public PARPoiLabel createPoi(String title, String description, double lat, double lon)
    {
        Location poiLocation = new Location(title);
        poiLocation.setLatitude(lat);
        poiLocation.setLongitude(lon);

        final PARPoiLabel parPoiLabel = new PARPoiLabel(poiLocation, title, description, R.layout.panicar_poilabel, R.drawable.radar_dot);

        parPoiLabel.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View view)
            {
                Toast.makeText(getActivity(), parPoiLabel.getTitle() + " - " + parPoiLabel.getDescription(), Toast.LENGTH_LONG).show();
            }
        });
        return parPoiLabel;
    }
}