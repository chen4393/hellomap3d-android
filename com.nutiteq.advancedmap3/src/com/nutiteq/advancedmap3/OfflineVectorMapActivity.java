package com.nutiteq.advancedmap3;

import java.io.IOException;

import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

import com.nutiteq.core.MapRange;
import com.nutiteq.datasources.MBTilesTileDataSource;
import com.nutiteq.hellomap3.util.AssetCopy;
import com.nutiteq.layers.VectorTileLayer;

/**
 * Uses sample offline vector map package from assets
 * 
 * @author jaak
 *
 */
public class OfflineVectorMapActivity extends MapSampleBaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // MapSampleBaseActivity creates and configures mapView  
        super.onCreate(savedInstanceState);
        
        // offline map data source
        String mbTileFile = "world_ntvt_0_4.mbtiles";

        try {
            String localDir = getExternalFilesDir(null).toString();
            AssetCopy.copyAssetToSDCard(getAssets(), mbTileFile, localDir);
            Log.i(Const.LOG_TAG,"copy done to " + localDir + "/"
                    + mbTileFile);
            MBTilesTileDataSource vectorTileDataSource = new MBTilesTileDataSource(0, 4, localDir + "/"
                    + mbTileFile);
            mapView.getLayers().remove(baseLayer);
            baseLayer = new VectorTileLayer(
                    vectorTileDataSource, vectorTileDecoder);
            mapView.getLayers().add(baseLayer);
            
            mapView.getOptions().setZoomRange(new MapRange(0,5));
            
        } catch (IOException e) {
            Log.e(Const.LOG_TAG, "mbTileFile cannot be copied: "+mbTileFile);
            Log.e(Const.LOG_TAG, e.getLocalizedMessage());
        }
        
    }
}
