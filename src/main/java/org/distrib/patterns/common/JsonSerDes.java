package org.distrib.patterns.common;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.cbor.CBORFactory;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import org.distrib.patterns.gossip.NodeId;
import org.distrib.patterns.net.InetAddressAndPort;

import java.io.IOException;


public class JsonSerDes {

    public static byte[] serialize(Object obj) {
        var objectMapper = new ObjectMapper(new JsonFactory());
        try {
            objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
            objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
            return objectMapper.writeValueAsBytes(obj);

        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T deserialize(byte[] json, Class<T> clazz) {
        try {
            var objectMapper = new ObjectMapper(new JsonFactory());
            objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            objectMapper
                    .registerModule(new ParameterNamesModule(JsonCreator.Mode.DEFAULT));
            var module = new SimpleModule();
            module.addKeyDeserializer(InetAddressAndPort.class, new InetAddressAndPortKeyDeserializer());
            module.addKeyDeserializer(NodeId.class, new NodeIdDeserializer());
            objectMapper.registerModule(module);
            return objectMapper.readValue(json, clazz);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    static class InetAddressAndPortKeyDeserializer extends KeyDeserializer {
        @Override
        public Object deserializeKey(String key, DeserializationContext deserializationContext) throws IOException {
            if (key.startsWith("[") && key.endsWith("]")) {
                return InetAddressAndPort.parse(key);
            }

            throw new IllegalArgumentException(key + "is not valid InetAddressAndPort");
        }
    }

    static class NodeIdDeserializer extends KeyDeserializer {
        @Override
        public Object deserializeKey(String key, DeserializationContext deserializationContext) throws IOException {
            if (key.startsWith("[") && key.endsWith("]")) {
                return new NodeId(key.substring(1, key.length() - 1));
            }

            throw new IllegalArgumentException(key + "is not valid InetAddressAndPort");
        }
    }
}
