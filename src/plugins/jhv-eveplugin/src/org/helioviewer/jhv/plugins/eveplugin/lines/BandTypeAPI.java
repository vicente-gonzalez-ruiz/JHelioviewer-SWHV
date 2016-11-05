package org.helioviewer.jhv.plugins.eveplugin.lines;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.helioviewer.jhv.JHVDirectory;
import org.helioviewer.jhv.base.DownloadStream;
import org.helioviewer.jhv.base.FileUtils;
import org.helioviewer.jhv.base.logging.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class BandTypeAPI {

    private static BandTypeAPI singletonInstance;

    private static final String baseURL = "http://swhv.oma.be/datasets/index.php";

    private static final HashMap<String, BandGroup> groups = new HashMap<>();
    private static final ArrayList<BandGroup> orderedGroups = new ArrayList<>();

    public static BandTypeAPI getSingletonInstance() {
        if (singletonInstance == null) {
            singletonInstance = new BandTypeAPI();
        }
        return singletonInstance;
    }

    private BandTypeAPI() {
        String jsonString = readJSON();
        if (jsonString != null) {
            try {
                JSONObject jsonmain = new JSONObject(jsonString);
                JSONArray jsonGroupArray = (JSONArray) jsonmain.get("groups");
                updateBandGroups(jsonGroupArray);
                JSONArray jsonObjectArray = (JSONArray) jsonmain.get("objects");
                updateBandTypes(jsonObjectArray);
            } catch (JSONException e) {
                Log.error("JSON parsing error", e);
            }
        }
    }

    private String readJSON() {
        URL url = null;
        try {
            url = new URL(baseURL);
        } catch (MalformedURLException e) {
            Log.error("Malformed URL", e);
        }

        File dstFile = new File(JHVDirectory.PLUGINS.getPath() + "/EVEPlugin/datasets.json");
        try {
            FileUtils.save(new DownloadStream(url).getInput(), dstFile);
        } catch (UnknownHostException e) {
            Log.debug("Unknown host, network down?", e);
        } catch (IOException e) {
            Log.debug("Error downloading the bandtypes", e);
        }

        try {
            return FileUtils.read(dstFile);
        } catch (IOException e) {
            Log.debug("Error reading the bandtypes", e);
        }

        return null;
    }

    private void updateBandTypes(JSONArray jsonObjectArray) {
        BandType[] bandtypes = new BandType[jsonObjectArray.length()];
        try {
            for (int i = 0; i < jsonObjectArray.length(); i++) {
                bandtypes[i] = new BandType();
                JSONObject job = (JSONObject) jsonObjectArray.get(i);

                if (job.has("label")) {
                    bandtypes[i].setLabel((String) job.get("label"));
                }
                if (job.has("name")) {
                    bandtypes[i].setName((String) job.get("name"));
                }
                if (job.has("range")) {
                    JSONArray rangeArray = job.getJSONArray("range");
                    Double v0 = rangeArray.getDouble(0);
                    Double v1 = rangeArray.getDouble(1);
                    bandtypes[i].setMin(v0);
                    bandtypes[i].setMax(v1);
                }
                if (job.has("unitLabel")) {
                    bandtypes[i].setUnitLabel((String) job.get("unitLabel"));
                }
                if (job.has("baseUrl")) {
                    bandtypes[i].setBaseURL((String) job.get("baseUrl"));
                }
                if (job.has("scale")) {
                    bandtypes[i].setScale(job.getString("scale"));
                }
                if (job.has("warnLevels")) {
                    JSONArray warnLevels = job.getJSONArray("warnLevels");
                    for (int j = 0; j < warnLevels.length(); j++) {
                        JSONObject helpobj = (JSONObject) warnLevels.get(j);
                        bandtypes[i].warnLevels.put((String) helpobj.get("warnLabel"), helpobj.getDouble("warnValue"));
                    }
                }
                if (job.has("group")) {
                    BandGroup group = groups.get(job.getString("group"));
                    group.add(bandtypes[i]);
                }
            }
        } catch (JSONException e) {
            Log.error("JSON parsing error", e);
        }
    }

    private void updateBandGroups(JSONArray jsonGroupArray) {
        try {
            for (int i = 0; i < jsonGroupArray.length(); i++) {
                BandGroup group = new BandGroup();
                JSONObject job = (JSONObject) jsonGroupArray.get(i);
                if (job.has("groupLabel")) {
                    group.setGroupLabel(job.getString("groupLabel"));
                }
                if (job.has("key")) {
                    groups.put(job.getString("key"), group);
                    orderedGroups.add(group);
                }
            }
        } catch (JSONException e) {
            Log.error("JSON parsing error", e);
        }
    }

    public BandType[] getBandTypes(BandGroup group) {
        return group.bandtypes.toArray(new BandType[group.bandtypes.size()]);
    }

    public List<BandGroup> getOrderedGroups() {
        return orderedGroups;
    }

}
