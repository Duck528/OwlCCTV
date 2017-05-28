package com.duck.owlcctv.util;


import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

/**
 * HTTP Request GET / POST 요청을 날릴 수 있는 Wrapper Class
 */
public class HttpUtil {

    // ObjectMapper - Thread Safe 하기 때문에 공유 변수로 선언한다
    // 출처: http://stackoverflow.com/questions/3907929/should-i-declare-jacksons-objectmapper-as-a-static-field
    private static final ObjectMapper mapper = new ObjectMapper();

    public static String post(String uri, Map<String, Object> dataParam) {

        DataOutputStream writer = null;
        BufferedReader reader = null;
        try {
            URL url = new URL(uri);
            HttpURLConnection httpConn = (HttpURLConnection)url.openConnection();
            httpConn.setRequestMethod("POST");
            httpConn.setRequestProperty("Content-Type", "application/json");

            // 서버로 데이터를 전송한다
            String json = mapper.writeValueAsString(dataParam);
            httpConn.setDoOutput(true);
            writer = new DataOutputStream(httpConn.getOutputStream());
            writer.writeBytes(json);

            // 서버로부터 요청의 응답을 받아온다
            StringBuilder builder = new StringBuilder();
            reader = new BufferedReader(new InputStreamReader(httpConn.getInputStream()));
            String buf;
            while ((buf = reader.readLine()) != null) {
                builder.append(buf);
            }
            return builder.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                if (writer != null)
                    writer.close();
                if (reader != null)
                    reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
