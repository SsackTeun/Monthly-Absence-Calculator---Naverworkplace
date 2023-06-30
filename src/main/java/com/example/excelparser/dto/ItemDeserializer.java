package com.example.excelparser.dto;

import com.example.excelparser.dto.Item;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ItemDeserializer extends JsonDeserializer<Object> {

    @Override
    public Object deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
        if (jsonParser.isExpectedStartArrayToken()) {
            return deserializeList(jsonParser, deserializationContext);
        } else {
            return deserializeObject(jsonParser, deserializationContext);
        }
    }

    private List<Object> deserializeList(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
        List<Object> itemList = new ArrayList<>();
        while (jsonParser.nextToken() != null && !jsonParser.isExpectedStartArrayToken()) {
            Object item = deserializeObject(jsonParser, deserializationContext);
            itemList.add(item);
        }
        return itemList;
    }

    private Object deserializeObject(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
        // Here, you can implement your custom deserialization logic for the Item class
        // You can use ObjectMapper to deserialize the JSON into an instance of Item class
        // For simplicity, let's assume you are directly deserializing into Item class
        return jsonParser.readValueAs(Item.class);
    }
}
