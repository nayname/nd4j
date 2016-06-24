package org.nd4j.serde.jackson;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.junit.BeforeClass;
import org.junit.Test;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.serde.jackson.ndarray.NDArrayDeSerializer;
import org.nd4j.serde.jackson.ndarray.NDArraySerializer;

import static org.junit.Assert.assertEquals;

/**
 * Created by agibsonccc on 6/23/16.
 */
public class VectorSerializeTest {
    private static ObjectMapper objectMapper;


    @BeforeClass
    public static void before() {
        objectMapper = objectMapper();
    }



    @Test
    public void testSerde() throws Exception {
        String json = objectMapper.writeValueAsString(Nd4j.create(2,2));
        INDArray assertion = Nd4j.create(2,2);
        INDArray test = objectMapper.readValue(json,INDArray.class);
        assertEquals(assertion,test);
    }


    private static ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule nd4j = new SimpleModule("nd4j");
        nd4j.addDeserializer(INDArray.class, new VectorDeSerializer());
        nd4j.addSerializer(INDArray.class, new VectorSerializer());
        mapper.registerModule(nd4j);
        return mapper;

    }
}