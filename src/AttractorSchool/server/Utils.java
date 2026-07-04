package AttractorSchool.server;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Utils {
    public static Map<String, String> parseUrlEncoded(String raw, String delimeter) {
        String[] pairs = raw.split(delimeter);
        Stream<Map.Entry<String, String>> stream = Arrays.stream(pairs)
                .map(Utils::decode)
                .filter(Optional::isPresent)
                .map(Optional::get);
        return stream.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

    }

    private static Optional<Map.Entry<String, String>> decode(String kv) {
        if (!kv.contains("=")) {
            return Optional.empty();
        }
        String[] pair = kv.split("=");
        if (pair.length != 2) {
            return Optional.empty();
        }

        Charset utf8 = StandardCharsets.UTF_8;
        String key = URLDecoder.decode(pair[0], utf8);
        String value = URLDecoder.decode(pair[1], utf8);
        return Optional.of(Map.entry(key, value));


    }

    public static Map<String, String> parseQuery(String query) {
        Map<String, String> params = new HashMap<>();
        if (query == null || query.isEmpty()) {
            return params;
        }

        String[] pairs = query.split("&");
        for (String pair : pairs) {
            String[] keyValue = pair.split("=");
            if (keyValue.length == 2) {
                params.put(keyValue[0], keyValue[1]);
            }
        }
        return params;
    }

    public static String readBody(HttpExchange exchange) throws IOException {
        return new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
    }

    public static Map<String, String> parseQueryParams(HttpExchange exchange) {
        String query = exchange.getRequestURI().getQuery();
        String queryString = Objects.nonNull(query) ? query : "";
        return parseUrlEncoded(queryString, "&");
    }
}
