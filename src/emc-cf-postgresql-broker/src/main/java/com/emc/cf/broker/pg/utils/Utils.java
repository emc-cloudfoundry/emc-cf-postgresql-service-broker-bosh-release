package com.emc.cf.broker.pg.utils;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.lang.reflect.Type;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Map;

/**
 * Created by liuc11 on 8/18/15.
 */
public class Utils {
    private static final Logger logger = LoggerFactory.getLogger(Utils.class);

    public static String convertMapToJson(Map<String, Object> map) {
        if(map == null || map.isEmpty()) {
            return null;
        }
        return new Gson().toJson(map);
    }

    public static Map<String, Object> convertJsonToMap(String json) {
        if(json == null || json.length() == 0) {
            return null;
        }
        Type typeOfT = new TypeToken<Map<String, Object>>(){}.getType();
        Map<String, Object> map = new Gson().fromJson(json, typeOfT);
        return map;
    }

    public static String generatePassword() {
        SecureRandom random = new SecureRandom();
        String passwd = new BigInteger(130, random).toString(32);
        return passwd;
    }

    public static String convertToDatabaseIdentifier(String str) {
        str = "id_" + str.replaceAll("-", "_");
        return str;
    }
}
