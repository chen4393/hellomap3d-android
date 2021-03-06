package com.nutiteq.advancedmap3;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;

import com.nutiteq.core.MapRange;
import com.nutiteq.datasources.CompressedCacheTileDataSource;
import com.nutiteq.datasources.NutiteqOnlineTileDataSource;
import com.nutiteq.datasources.PersistentCacheTileDataSource;
import com.nutiteq.datasources.TileDataSource;
import com.nutiteq.layers.VectorTileLayer;
import com.nutiteq.utils.AssetUtils;
import com.nutiteq.vectortiles.MBVectorTileDecoder;
import com.nutiteq.vectortiles.MBVectorTileStyleSet;
import com.nutiteq.wrappedcommons.UnsignedCharVector;

/**
 * Base activity for vector map samples. Adds menu with multiple style choices.
 */
public class VectorMapSampleBaseActivity extends MapSampleBaseActivity {

	public static final String MAIN_STYLE = "nutibright-v3";

	protected TileDataSource vectorTileDataSource;
	protected MBVectorTileDecoder vectorTileDecoder;
	protected boolean persistentTileCache = false;

	// Style parameters
	protected String vectorStyleName = MAIN_STYLE + ":default"; // default style name, each style has corresponding .zip asset
	protected String vectorStyleLang = "en"; // default map language

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Update options
		mapView.getOptions().setZoomRange(new MapRange(0, 20));

		// Set default base map - online vector with persistent caching
		updateBaseLayer();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		Menu langMenu = menu.addSubMenu("Language");

		addLanguageMenuOption(langMenu, "English", "en");
		addLanguageMenuOption(langMenu, "German",  "de");
		addLanguageMenuOption(langMenu, "Spanish",  "es");
		addLanguageMenuOption(langMenu, "Italian",  "it");
		addLanguageMenuOption(langMenu, "French",  "fr");
		addLanguageMenuOption(langMenu, "Russian", "ru");
		addLanguageMenuOption(langMenu, "Chinese", "zh");

		Menu styleMenu = menu.addSubMenu("Style");

		addStyleMenuOption(styleMenu, "Basic", "basic");
		addStyleMenuOption(styleMenu, "NutiBright 2D", MAIN_STYLE + ":default");
		addStyleMenuOption(styleMenu, "Nutiteq Dark", MAIN_STYLE + ":nutiteq_dark");
		addStyleMenuOption(styleMenu, "Nutiteq Grey", MAIN_STYLE + ":nutiteq_grey");
		addStyleMenuOption(styleMenu, "NutiBright 3D", "nutibright3d");
		addStyleMenuOption(styleMenu, "Loose Leaf", "looseleaf");

		return true;
	}

	private void addLanguageMenuOption(final Menu menu, String text, final String value) {

		MenuItem menuItem = menu.add(text).setOnMenuItemClickListener(new OnMenuItemClickListener(){
			@Override
			public boolean onMenuItemClick (MenuItem item){
				for (int i = 0; i < menu.size(); i++) {
					MenuItem otherItem = menu.getItem(i);
					if (otherItem == item) {
						otherItem.setIcon(android.R.drawable.checkbox_on_background);
					} else {
						otherItem.setIcon(null);
					}
				}
				vectorStyleLang = value;
				updateBaseLayer();
				return true;
			}
		});

		if (vectorStyleLang.equals(value)) {
			menuItem.setIcon(android.R.drawable.checkbox_on_background);
		}
	}

	private void addStyleMenuOption(final Menu menu, String text, final String value) {
		MenuItem menuItem = menu.add(text).setOnMenuItemClickListener(new OnMenuItemClickListener(){
			@Override
			public boolean onMenuItemClick (MenuItem item){
				for (int i = 0; i < menu.size(); i++) {
					MenuItem otherItem = menu.getItem(i);
					if (otherItem == item) {
						otherItem.setIcon(android.R.drawable.checkbox_on_background);
					} else {
						otherItem.setIcon(null);
					}
				}
				vectorStyleName = value;
				updateBaseLayer();
				return true;
			}
		});

		if (vectorStyleName.equals(value)) {
			menuItem.setIcon(android.R.drawable.checkbox_on_background);
		}
	}

    private void updateBaseLayer() {

		boolean styleBuildings3D = false;

		if (vectorStyleName.contains(":")) {

			String[] split = vectorStyleName.split(":");
			String fileName = split[0];
			String styleName = split[1];

			MBVectorTileStyleSet vectorTileStyleSet = getStyleSet(fileName);
			vectorTileDecoder = new MBVectorTileDecoder(vectorTileStyleSet, styleName);

		} else {

			if (vectorStyleName.equals("nutibright3d")) {
				vectorStyleName = MAIN_STYLE;
				styleBuildings3D = true;
			}

			MBVectorTileStyleSet vectorTileStyleSet = getStyleSet(vectorStyleName);
			vectorTileDecoder = new MBVectorTileDecoder(vectorTileStyleSet);
		}

		// Set language, language-specific texts from vector tiles will be used
		vectorTileDecoder.setStyleParameter("lang", vectorStyleLang);

		// OSM Bright style set supports choosing between 2d/3d buildings. Set corresponding parameter.
		vectorTileDecoder.setStyleParameter("buildings3d", styleBuildings3D ? "1" : "0");
		vectorTileDecoder.setStyleParameter("markers3d",styleBuildings3D ? "1" : "0");
		vectorTileDecoder.setStyleParameter("texts3d",styleBuildings3D ? "1" : "0");

		// Create tile data source for vector tiles
		if (vectorTileDataSource == null) {
			vectorTileDataSource = createTileDataSource();
		}

		// Remove old base layer, create new base layer
		if (baseLayer != null) {
			mapView.getLayers().remove(baseLayer);
		}

		baseLayer = new VectorTileLayer(vectorTileDataSource, vectorTileDecoder);
		mapView.getLayers().insert(0, baseLayer);
    }

	protected MBVectorTileStyleSet getStyleSet(String fileName) {
		String styleAssetName = fileName + ".zip";
		UnsignedCharVector styleBytes = AssetUtils.loadBytes(styleAssetName);
		return new MBVectorTileStyleSet(styleBytes);
	}

    protected TileDataSource createTileDataSource() {
        TileDataSource vectorTileDataSource = new NutiteqOnlineTileDataSource("nutiteq.osm");

        // We don't use vectorTileDataSource directly (this would be also option),
        // but via caching to cache data locally persistently/non-persistently
        // Note that persistent cache requires WRITE_EXTERNAL_STORAGE permission
        TileDataSource cacheDataSource = vectorTileDataSource;
        if (persistentTileCache) {
        	String cacheFile = getExternalFilesDir(null)+"/mapcache.db";
        	Log.i(Const.LOG_TAG,"cacheFile = "+cacheFile);
        	cacheDataSource = new PersistentCacheTileDataSource(vectorTileDataSource, cacheFile);
        } else {
        	cacheDataSource = new CompressedCacheTileDataSource(vectorTileDataSource);
        }
    	return cacheDataSource;
    }
}
