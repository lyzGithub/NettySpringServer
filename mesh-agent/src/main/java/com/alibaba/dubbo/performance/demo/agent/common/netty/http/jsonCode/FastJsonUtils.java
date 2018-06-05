package com.alibaba.dubbo.performance.demo.agent.common.netty.http.jsonCode;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.JSONSerializer;
import com.alibaba.fastjson.serializer.SerializeWriter;
import com.alibaba.fastjson.serializer.SerializerFeature;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * @author ken.lj
 * @date 02/04/2018
 */
public class FastJsonUtils {


    public static String convertObjectToJSON(Object obj){

        String jsonString = "";
        jsonString = JSON.toJSONString(obj);
        //String s = JSON.toJSONString("ABCDEFG");
        //System.out.println(s);
        //System.out.println("convertObjectToJSONb jsonString: " + jsonString);
        return jsonString;

    }

    public static Object convertJSONToObject(String jsonString, Class<?> clazz){

        Object obj = null;
        //System.out.println("jsonString: "+jsonString + ", clazz: "+clazz.toString());
        obj = JSON.parseObject(jsonString, clazz);

        return obj;

    }

    public static void writeObject(Object obj, PrintWriter writer) throws IOException {
        SerializeWriter out = new SerializeWriter();
        JSONSerializer serializer = new JSONSerializer(out);
        serializer.config(SerializerFeature.WriteEnumUsingToString, true);
        serializer.write(obj);
        out.writeTo(writer);
        out.close(); // for reuse SerializeWriter buf
        writer.println();
        writer.flush();
    }

    public static void writeBytes(byte[] b, PrintWriter writer) {
        writer.print(new String(b));
        writer.flush();
    }
}
