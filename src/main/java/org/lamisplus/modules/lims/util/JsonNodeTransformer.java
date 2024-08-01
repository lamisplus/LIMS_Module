package org.lamisplus.modules.lims.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.springframework.stereotype.Component;

@Component
public class JsonNodeTransformer {

    public String getNodeValue(Object obj, String arrayName, String value, Boolean array){
        String jsonStr = obj.toString();
        ObjectMapper mapper = new ObjectMapper();
        if(array) {
            ArrayNode arrayNode;
            try {
                arrayNode = (ArrayNode) mapper.readTree(jsonStr).get(arrayName);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
            if (arrayNode.isArray()) {
                for (JsonNode jsonNode : arrayNode) {
                    if (jsonNode.get("value") != null) {
                        return jsonNode.get("value").asText("none");
                    }
                }
            }
        } else {
            JsonNode jsonNode;
            try {
                jsonNode = mapper.readTree(obj.toString());
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
            return jsonNode.get(value).asText();
        }
        return null;
    }
}
