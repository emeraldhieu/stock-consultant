package com.emeraldhieu.closeprice;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.json.JSONArray;
import org.json.JSONObject;

public class CacheFilterTest {

    protected List<List<String>> dateCloseList;

    public void setUp(String folder, String ticker) throws Exception {
        Path responseFilePath = Paths.get(ClassLoader.getSystemResource("responses/" + folder + "/" + ticker + ".json").toURI());
        String closePriceJsonResponse = new String(Files.readAllBytes(responseFilePath));
        JSONObject jsonObject = new JSONObject(closePriceJsonResponse);
        JSONObject datasetDataObject = jsonObject.getJSONObject("dataset_data");
        JSONArray dataArray = datasetDataObject.getJSONArray("data");
        Iterable<Object> iterable = () -> dataArray.iterator();
        dateCloseList = StreamSupport.stream(iterable.spliterator(), false)
                .map(JSONArray.class::cast)
                .map(closePrice -> Arrays.asList(closePrice.getString(0), String.valueOf(closePrice.getDouble(1))))
                .collect(Collectors.toList());
    }
}
