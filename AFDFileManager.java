package TeriaComputacion.io;

import TeriaComputacion.modelo.AFD;
import TeriaComputacion.modelo.State;
import TeriaComputacion.modelo.Transition;

import java.awt.Point;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

/**
  Formato a realizar:
  {
    "name": "...",
    "states": [
      {"id":"q0","name":"q0","initial":true,"final":false,"x":100,"y":150},
      ...
    ],
    "transitions": [
      {"from":"q0","to":"q1","symbol":"a"},
      ...
    ]
  }
 */
public class AFDFileManager {

    public static void save(AFD afd, File file) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("  \"name\": \"").append(escape(afd.getName())).append("\",\n");
        sb.append("  \"states\": [\n");

        List<State> states = new ArrayList<>(afd.getStates());
        for (int i = 0; i < states.size(); i++) {
            State s = states.get(i);
            sb.append("    {");
            sb.append("\"id\":\"").append(escape(s.getId())).append("\",");
            sb.append("\"name\":\"").append(escape(s.getName())).append("\",");
            sb.append("\"initial\":").append(s.isInitial()).append(",");
            sb.append("\"final\":").append(s.isFinal()).append(",");
            sb.append("\"x\":").append(s.getPosition().x).append(",");
            sb.append("\"y\":").append(s.getPosition().y);
            sb.append("}");
            if (i < states.size() - 1) sb.append(",");
            sb.append("\n");
        }
        sb.append("  ],\n");
        sb.append("  \"transitions\": [\n");

        List<Transition> transitions = new ArrayList<>(afd.getTransitions());
        for (int i = 0; i < transitions.size(); i++) {
            Transition t = transitions.get(i);
            sb.append("    {");
            sb.append("\"from\":\"").append(escape(t.getFrom().getId())).append("\",");
            sb.append("\"to\":\"").append(escape(t.getTo().getId())).append("\",");
            sb.append("\"symbol\":\"").append(escape(t.getSymbol())).append("\"");
            sb.append("}");
            if (i < transitions.size() - 1) sb.append(",");
            sb.append("\n");
        }
        sb.append("  ]\n");
        sb.append("}\n");

        Files.writeString(file.toPath(), sb.toString(), StandardCharsets.UTF_8);
    }

    public static AFD load(File file) throws IOException {
        String content = Files.readString(file.toPath(), StandardCharsets.UTF_8);
        return parse(content);
    }

    private static AFD parse(String json) throws IOException {
        try {
            AFD afd = new AFD();
            String name = extractString(json, "name");
            if (name != null) afd.setName(name);
            String statesArr = extractArray(json, "states");
            Map<String, State> stateMap = new LinkedHashMap<>();
            if (statesArr != null) {
                List<String> stateObjs = splitObjects(statesArr);
                for (String obj : stateObjs) {
                    String id      = extractString(obj, "id");
                    String sname = extractString(obj, "name");
                    boolean init = "true".equals(extractBool(obj, "initial"));
                    boolean fin = "true".equals(extractBool(obj, "final"));
                    int x = extractInt(obj, "x", 100);
                    int y = extractInt(obj, "y", 100);
                    if (id == null) id = sname;
                    if (sname == null) sname = id;
                    State s = new State(id, sname, init, fin, new Point(x, y));
                    stateMap.put(id, s);
                    afd.addState(s);
                }
            }

            String transArr = extractArray(json, "transitions");
            if (transArr != null) {
                List<String> transObjs = splitObjects(transArr);
                for (String obj : transObjs) {
                    String from   = extractString(obj, "from");
                    String to     = extractString(obj, "to");
                    String symbol = extractString(obj, "symbol");
                    if (from == null || to == null || symbol == null) continue;
                    State sf = stateMap.get(from);
                    State st = stateMap.get(to);
                    if (sf == null || st == null) continue;
                    afd.addTransition(new Transition(sf, st, symbol));
                }
            }

            return afd;
        } catch (Exception e) {
            throw new IOException("Error al parsear el archivo AFD: " + e.getMessage(), e);
        }
    }

    private static String extractString(String json, String key) {
        String pattern = "\"" + key + "\"";
        int idx = json.indexOf(pattern);
        if (idx < 0) return null;
        int colon = json.indexOf(":", idx + pattern.length());
        if (colon < 0) return null;
        int q1 = json.indexOf("\"", colon + 1);
        if (q1 < 0) return null;
        int q2 = json.indexOf("\"", q1 + 1);
        if (q2 < 0) return null;
        return json.substring(q1 + 1, q2);
    }

    private static String extractBool(String json, String key) {
        String pattern = "\"" + key + "\"";
        int idx = json.indexOf(pattern);
        if (idx < 0) return null;
        int colon = json.indexOf(":", idx + pattern.length());
        if (colon < 0) return null;
        int start = colon + 1;
        while (start < json.length() && json.charAt(start) == ' ') start++;
        if (json.startsWith("true", start)) return "true";
        if (json.startsWith("false", start)) return "false";
        return null;
    }

    private static int extractInt(String json, String key, int def) {
        String pattern = "\"" + key + "\"";
        int idx = json.indexOf(pattern);
        if (idx < 0) return def;
        int colon = json.indexOf(":", idx + pattern.length());
        if (colon < 0) return def;
        int start = colon + 1;
        while (start < json.length() && (json.charAt(start) == ' ' || json.charAt(start) == '\n')) start++;
        int end = start;
        while (end < json.length() && (Character.isDigit(json.charAt(end)) || json.charAt(end) == '-')) end++;
        if (start == end) return def;
        try { return Integer.parseInt(json.substring(start, end)); }
        catch (NumberFormatException e) { return def; }
    }

    private static String extractArray(String json, String key) {
        String pattern = "\"" + key + "\"";
        int idx = json.indexOf(pattern);
        if (idx < 0) return null;
        int bracket = json.indexOf("[", idx + pattern.length());
        if (bracket < 0) return null;
        int depth = 0, end = bracket;
        while (end < json.length()) {
            char c = json.charAt(end);
            if (c == '[') depth++;
            else if (c == ']') { depth--; if (depth == 0) break; }
            end++;
        }
        return json.substring(bracket + 1, end);
    }

    private static List<String> splitObjects(String arr) {
        List<String> list = new ArrayList<>();
        int depth = 0, start = -1;
        for (int i = 0; i < arr.length(); i++) {
            char c = arr.charAt(i);
            if (c == '{') { if (depth == 0) start = i; depth++; }
            else if (c == '}') { depth--; if (depth == 0 && start >= 0) { list.add(arr.substring(start, i + 1)); start = -1; } }
        }
        return list;
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
