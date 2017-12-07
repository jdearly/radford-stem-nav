package com.radfordstemnav;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DataParser {

    /**
     * Parses the JSONObject to obtain the necessary data to generate a route.
     * @param jObject
     * @return the routes in a simple List<List<HashMap<String, String>>>
     */
    public List<List<HashMap<String, String>>> parse(JSONObject jObject) {

        List<List<HashMap<String, String>>> routes = new ArrayList<>();
        JSONArray jRoutes;
        JSONArray jLegs;
        JSONArray jSteps;

        try {

            jRoutes = jObject.getJSONArray("routes");

            // All routes
            for (int i = 0; i < jRoutes.length(); i++) {
                jLegs = ((JSONObject) jRoutes.get(i)).getJSONArray("legs");
                List path = new ArrayList<>();

                // All legs
                for (int j = 0; j < jLegs.length(); j++) {
                    jSteps = ((JSONObject) jLegs.get(j)).getJSONArray("steps");

                    // All steps
                    for (int k = 0; k < jSteps.length(); k++) {
                        String polyline = "";
                        polyline = (String) ((JSONObject) ((JSONObject) jSteps.get(k)).get("polyline")).get("points");
                        List<LatLng> list = decodePoly(polyline);

                        // All points
                        for (int l = 0; l < list.size(); l++) {
                            HashMap<String, String> hm = new HashMap<>();
                            hm.put("lat", Double.toString((list.get(l)).latitude));
                            hm.put("lng", Double.toString((list.get(l)).longitude));
                            path.add(hm);
                        }
                    }
                    routes.add(path);
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception e) {
        }


        return routes;
    }

    /**
     *  The following applies the same strategy as the the parse above, just collects direction data.
     *  The two could possibly be combined to collect all the data at once, but the separation saves
     *  memory if the one or the other is never used.
     * @param jObject
     * @return the route directions details in a List<ArrayList<String>>
     */
    public List<ArrayList<String>> parseDirections(JSONObject jObject) {

        List<ArrayList<String>> dir_routes = new ArrayList<>();
        JSONArray jRoutes;
        JSONArray jLegs;
        JSONArray jSteps;

        try {

            jRoutes = jObject.getJSONArray("routes");

            // All routes
            for (int i = 0; i < jRoutes.length(); i++) {
                jLegs = ((JSONObject) jRoutes.get(i)).getJSONArray("legs");
                ArrayList dir_path = new ArrayList<>();

                // All legs
                for (int j = 0; j < jLegs.length(); j++) {
                    String duration = "";
                    jSteps = ((JSONObject) jLegs.get(j)).getJSONArray("steps");
                    if (((JSONObject) ((JSONObject) jLegs.get(j)).get("duration")).get("text") != null)
                        duration = "Trip duration: " + ((JSONObject) ((JSONObject) jSteps.get(j)).get("duration")).getString("text");
                        dir_path.add(duration);
                        dir_path.add("");

                    // All steps
                    for (int k = 0; k < jSteps.length(); k++) {
                        String navig1 = "";
                        String distance = "";
                        if (((JSONObject) ((JSONObject) jSteps.get(k)).get("distance")).get("text") != null)
                            distance = "In " + ((JSONObject) ((JSONObject) jSteps.get(k)).get("distance")).getString("text");
                        if (((JSONObject) jSteps.get(k)).get("html_instructions") != null)
                            navig1 = ((JSONObject) jSteps.get(k)).getString("html_instructions");
                        dir_path.add(distance);
                        dir_path.add(navig1);

                    }
                }
                dir_routes.add(dir_path);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception e) {
        }

        return dir_routes;
    }


    /**
     * The following code was borrowed from a much more knowledgeable developer. Credit link provided.
     * http://jeffreysambells.com/2010/05/27/decoding-polylines-from-google-maps-direction-api-with-java
     *
     * Seems to be the most common method for handling drawing poly-lines once the points are obtained.
     */
    private List<LatLng> decodePoly(String encoded) {

        List<LatLng> poly = new ArrayList<>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }

        return poly;
    }
}


