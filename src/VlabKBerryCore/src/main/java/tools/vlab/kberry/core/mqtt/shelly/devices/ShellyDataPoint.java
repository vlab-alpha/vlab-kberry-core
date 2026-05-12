package tools.vlab.kberry.core.mqtt.shelly.devices;

import tools.vlab.kberry.core.RGBW;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public record ShellyDataPoint(Map<String, Object> json) {

    public static ShellyDataPoint from(String rawJson) {
        return new ShellyDataPoint(JsonParser.parse(rawJson));
    }

    public static ShellyDataPoint bool(boolean value) {
        Map<String, Object> params = new HashMap<>();
        params.put("id", 0);
        params.put("on", value);

        Map<String, Object> root = new HashMap<>();
        root.put("params", params);

        return new ShellyDataPoint(root);
    }

    public static ShellyDataPoint rgbw(boolean on, RGBW color, int brightness) {
        var params = new java.util.HashMap<String, Object>();
        params.put("id", 0);
        params.put("on", on);
        params.put("red", color.r());
        params.put("green", color.g());
        params.put("blue", color.b());
        params.put("white", color.w());
        params.put("brightness", brightness);

        var root = new java.util.HashMap<String, Object>();
        root.put("params", params);
        return new ShellyDataPoint(root);
    }

    @SuppressWarnings("unchecked")
    public ShellyDataPoint getDataPoint(String path) {
        Object value = resolve(path);
        if (!(value instanceof java.util.Map<?, ?> map)) return null;
        return new ShellyDataPoint((java.util.Map<String, Object>) map);
    }

    @SuppressWarnings("unchecked")
    private Object resolve(String path) {
        String[] parts = path.split("\\.");
        Object current = json;

        for (String part : parts) {
            if (current == null) return null;

            if (current instanceof Map<?, ?> map) {
                current = ((Map<String, Object>) map).get(part);
            } else {
                return null;
            }
        }

        return current;
    }

    public Optional<Boolean> getBoolean(String attribute) {
        Object value = resolve(attribute);
        return switch (value) {
            case Boolean b -> Optional.of(b);
            case Number n -> Optional.of(n.intValue() != 0);
            case String s -> switch (s.toLowerCase()) {
                case "true", "1" -> Optional.of(true);
                case "false", "0" -> Optional.of(false);
                default -> Optional.empty();
            };
            case null, default -> Optional.empty();
        };

    }

    public Optional<Integer> getInt(String attribute) {
        Object value = resolve(attribute);
        if (value instanceof Number n) return Optional.of(n.intValue());
        if (value instanceof String s) {
            try { return Optional.of(Integer.parseInt(s)); }
            catch (Exception ignored) {}
        }
        return Optional.empty();
    }

    public Optional<Double> getDouble(String attribute) {
        Object value = resolve(attribute);
        if (value instanceof Number n) return Optional.of(n.doubleValue());
        if (value instanceof String s) {
            try { return Optional.of(Double.parseDouble(s)); }
            catch (Exception ignored) {}
        }
        return Optional.empty();
    }

    public Optional<String> getString(String attribute) {
        Object value = resolve(attribute);
        return value != null ? Optional.of(value.toString()) : Optional.empty();
    }

    public Map<String, Object> raw() {
        return json;
    }

    public String toJson() {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, Object> entry : json.entrySet()) {
            if (!first) sb.append(",");
            sb.append("\"").append(entry.getKey()).append("\":");
            sb.append(toJsonValue(entry.getValue()));
            first = false;
        }
        return sb.toString();
    }

    @SuppressWarnings("unchecked")
    private static String toJsonValue(Object value) {
        return switch (value) {
            case null -> "null";
            case Map<?, ?> map -> {
                StringBuilder sb = new StringBuilder("{");
                boolean first = true;
                for (Map.Entry<?, ?> entry : map.entrySet()) {
                    if (!first) sb.append(",");
                    sb.append("\"").append(entry.getKey()).append("\":");
                    sb.append(toJsonValue(entry.getValue()));
                    first = false;
                }
                yield sb.append("}").toString();
            }
            case java.util.List<?> list -> {
                StringBuilder sb = new StringBuilder("[");
                boolean first = true;
                for (Object item : list) {
                    if (!first) sb.append(",");
                    sb.append(toJsonValue(item));
                    first = false;
                }
                yield sb.append("]").toString();
            }
            case String s -> "\"" + s.replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r")
                    .replace("\t", "\\t") + "\"";
            case Boolean b -> b.toString();
            case Number n  -> n.toString();
            default -> "\"" + value + "\"";
        };
    }


    // ---------------------------------------------------------------
    // Minimaler JSON-Parser (kein Dependency nötig)
    // ---------------------------------------------------------------
    static class JsonParser {

        private final String src;
        private int pos;

        private JsonParser(String src) {
            this.src = src.strip();
        }

        public static Map<String, Object> parse(String json) {
            JsonParser p = new JsonParser(json);
            Object result = p.parseValue();
            if (result instanceof Map<?, ?> m) {
                @SuppressWarnings("unchecked")
                Map<String, Object> typed = (Map<String, Object>) m;
                return typed;
            }
            throw new IllegalArgumentException("Top-level JSON must be an object");
        }

        private Object parseValue() {
            skipWhitespace();
            char c = src.charAt(pos);
            return switch (c) {
                case '{' -> parseObject();
                case '[' -> parseArray();
                case '"' -> parseString();
                case 't', 'f' -> parseBoolean();
                case 'n' -> parseNull();
                default  -> parseNumber();
            };
        }

        private Map<String, Object> parseObject() {
            Map<String, Object> map = new HashMap<>();
            pos++; // skip '{'
            skipWhitespace();

            while (src.charAt(pos) != '}') {
                String key = parseString();
                skipWhitespace();
                pos++; // skip ':'
                Object value = parseValue();
                map.put(key, value);
                skipWhitespace();
                if (src.charAt(pos) == ',') pos++;
                skipWhitespace();
            }

            pos++; // skip '}'
            return map;
        }

        private java.util.List<Object> parseArray() {
            java.util.List<Object> list = new java.util.ArrayList<>();
            pos++; // skip '['
            skipWhitespace();

            while (src.charAt(pos) != ']') {
                list.add(parseValue());
                skipWhitespace();
                if (src.charAt(pos) == ',') pos++;
                skipWhitespace();
            }

            pos++; // skip ']'
            return list;
        }

        private String parseString() {
            skipWhitespace();
            pos++; // skip opening '"'
            StringBuilder sb = new StringBuilder();

            while (pos < src.length()) {
                char c = src.charAt(pos++);
                if (c == '"') break;
                if (c == '\\' && pos < src.length()) {
                    char esc = src.charAt(pos++);
                    sb.append(switch (esc) {
                        case '"'  -> '"';
                        case '\\' -> '\\';
                        case '/'  -> '/';
                        case 'n'  -> '\n';
                        case 'r'  -> '\r';
                        case 't'  -> '\t';
                        default   -> esc;
                    });
                } else {
                    sb.append(c);
                }
            }

            return sb.toString();
        }

        private Boolean parseBoolean() {
            if (src.startsWith("true", pos))  { pos += 4; return true;  }
            if (src.startsWith("false", pos)) { pos += 5; return false; }
            throw new IllegalArgumentException("Invalid boolean at pos " + pos);
        }

        private Object parseNull() {
            if (src.startsWith("null", pos)) { pos += 4; return null; }
            throw new IllegalArgumentException("Invalid null at pos " + pos);
        }

        private Number parseNumber() {
            int start = pos;
            if (src.charAt(pos) == '-') pos++;
            while (pos < src.length() && (Character.isDigit(src.charAt(pos)) || src.charAt(pos) == '.' || src.charAt(pos) == 'e' || src.charAt(pos) == 'E' || src.charAt(pos) == '+' || src.charAt(pos) == '-')) {
                pos++;
            }
            String num = src.substring(start, pos);
            return num.contains(".") || num.contains("e") || num.contains("E")
                    ? Double.parseDouble(num)
                    : Long.parseLong(num);
        }

        private void skipWhitespace() {
            while (pos < src.length() && Character.isWhitespace(src.charAt(pos))) pos++;
        }
    }
}