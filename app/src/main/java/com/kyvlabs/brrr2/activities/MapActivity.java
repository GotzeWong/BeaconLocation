package com.kyvlabs.brrr2.activities;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;

import com.kyvlabs.brrr2.R;
import com.kyvlabs.brrr2.utils.AssetsHelper;
import com.kyvlabs.brrr2.utils.Compass;
import com.kyvlabs.brrr2.utils.RotateDegreeChangeListener;
import com.kyvlabs.brrr2.utils.overlay.BitmapOverlay;
import com.kyvlabs.brrr2.utils.svgmapview.SVGMapView;
import com.kyvlabs.brrr2.utils.svgmapview.SVGMapViewListener;
import com.kyvlabs.brrr2.utils.svgmapview.overlay.SVGMapLocationOverlay;

import java.util.Map;

public class MapActivity extends AppCompatActivity {
    private SVGMapView mapView;
    private Compass compass;
    private static int x = 1320 ,y= 1090;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        mapView = (SVGMapView) findViewById(R.id.location_mapview);
        compass = new Compass(this);

        mapView.registerMapViewListener(new SVGMapViewListener()
        {
            @Override
            public void onMapLoadComplete()
            {
                BitmapOverlay beaconLocationOverlay = new BitmapOverlay(mapView);
                mapView.getOverLays().add(beaconLocationOverlay);
                mapView.getController().sparkAtPoint(new PointF(1350,260), 100, ContextCompat.getColor(MapActivity.this, R.color.colorPrimary), 10);
                mapView.getController().sparkAtPoint(new PointF(2250,750), 100, ContextCompat.getColor(MapActivity.this, R.color.colorPrimary), 10);

                final SVGMapLocationOverlay locationOverlay = new SVGMapLocationOverlay(mapView);
                locationOverlay.setIndicatorArrowBitmap(BitmapFactory.decodeResource(getResources(), R.mipmap.indicator_arrow));
                locationOverlay.setPosition(new PointF(x, y));
                locationOverlay.setIndicatorCircleRotateDegree(90);
                locationOverlay.setMode(SVGMapLocationOverlay.MODE_COMPASS);
                locationOverlay.setIndicatorArrowRotateDegree(-90);

                compass.arrowView = locationOverlay;
                compass.registeRotateDegreeChangeListener(new RotateDegreeChangeListener() {
                    @Override
                    public void onRotateDegreeChange() {
                        mapView.refresh();
                    }
                });

                mapView.getOverLays().add(locationOverlay);
                mapView.refresh();
            }

            @Override
            public void onMapLoadError()
            {
            }

            @Override
            public void onGetCurrentMap(Bitmap bitmap)
            {
            }
        });
        mapView.loadMap(AssetsHelper.getContent(this, "sample1.svg"));
    }

    @Override
    protected void onStart() {
        super.onStart();
        compass.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        compass.stop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        compass.start();
    }

    @Override
    protected void onStop() {
        super.onStop();
        compass.stop();
    }

}
