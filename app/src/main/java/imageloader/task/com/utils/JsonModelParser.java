package imageloader.task.com.utils;

import com.google.gson.Gson;
import com.google.gson.internal.$Gson$Types;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;

/**
 * Created by dineshsingh on 7/13/2018
 */

public class JsonModelParser {

    private Type mType;

    public <T> T parse(String data, Type tTypeOf) {
        mType = tTypeOf;
        if (data == null || data.isEmpty()) {
            return null;
        }
        return convertSingle(data);
    }

    private <T> T convertSingle(String data) {
        if (null == data)
            return null;
        return (T) new Gson().fromJson(data, mType);
    }

    private <T> ArrayList<T> convertCollection(String data) {
        if (null == data)
            return null;
        return (ArrayList<T>) new Gson().fromJson(data, getTokenType());
    }

    public <T> ArrayList<T> parseCollection(String data, Type tTypeOf) {
        mType = tTypeOf;
        if (data == null || data.isEmpty()) {
            return null;
        }
        return convertCollection(data);
    }

    private Type getTokenType() {
        return $Gson$Types.newParameterizedTypeWithOwner(null, ArrayList.class, mType);
    }

    public Object parseKeyData(String jsonData, String dataKey, Type tTypeOf) {
        mType = tTypeOf;
        try {
            JSONObject jsonRes = new JSONObject(jsonData);
            if (jsonRes.get(dataKey) instanceof JSONArray) {
                return convertCollection(jsonRes.getJSONArray(dataKey).toString());
            } else if (jsonRes.get(dataKey) instanceof JSONObject) {
                return convertSingle(jsonRes.get(dataKey).toString());
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

}