package com.wtm.util;

import java.util.*;

/**
 * Small dependency-free JSON parser used so the application can ship as one
 * plain Java JAR. It supports JSON objects, arrays, strings, numbers, booleans
 * and null, which is sufficient for the public APIs used by this application.
 */
public final class MiniJson {
    private MiniJson() {}

    public static Object parse(String json) { return new Parser(json).parseValue(); }

    @SuppressWarnings("unchecked") public static Map<String,Object> obj(Object o) { return (Map<String,Object>) o; }
    @SuppressWarnings("unchecked") public static List<Object> arr(Object o) { return (List<Object>) o; }
    public static String str(Object o) { return o == null ? "" : String.valueOf(o); }
    public static double num(Object o) { return o instanceof Number n ? n.doubleValue() : Double.parseDouble(String.valueOf(o)); }
    public static int integer(Object o) { return (int)Math.round(num(o)); }

    private static final class Parser {
        private final String s; private int i;
        Parser(String s) { this.s = s; }
        Object parseValue() {
            ws(); if (i >= s.length()) return null; char c=s.charAt(i);
            return switch (c) {
                case '{' -> object(); case '[' -> array(); case '"' -> string();
                case 't' -> literal("true", true); case 'f' -> literal("false", false); case 'n' -> literal("null", null);
                default -> number();
            };
        }
        Map<String,Object> object() {
            Map<String,Object> m=new LinkedHashMap<>(); i++; ws();
            if (peek('}')) { i++; return m; }
            while (true) {
                ws(); String k=string(); ws(); expect(':'); Object v=parseValue(); m.put(k,v); ws();
                if (peek('}')) { i++; break; } expect(',');
            } return m;
        }
        List<Object> array() {
            List<Object> a=new ArrayList<>(); i++; ws();
            if (peek(']')) { i++; return a; }
            while (true) { a.add(parseValue()); ws(); if (peek(']')) { i++; break; } expect(','); }
            return a;
        }
        String string() {
            expect('"'); StringBuilder b=new StringBuilder();
            while (i<s.length()) { char c=s.charAt(i++); if(c=='"') break; if(c=='\\') { char e=s.charAt(i++); switch(e) {
                case '"','\\','/' -> b.append(e); case 'b'->b.append('\b'); case 'f'->b.append('\f'); case 'n'->b.append('\n'); case 'r'->b.append('\r'); case 't'->b.append('\t');
                case 'u' -> { String h=s.substring(i,i+4); b.append((char)Integer.parseInt(h,16)); i+=4; }
                default -> b.append(e);
            }} else b.append(c); } return b.toString();
        }
        Object number() {
            int st=i; while(i<s.length() && "-+0123456789.eE".indexOf(s.charAt(i))>=0) i++;
            String n=s.substring(st,i); return (n.contains(".")||n.contains("e")||n.contains("E")) ? Double.parseDouble(n) : Long.parseLong(n);
        }
        Object literal(String text,Object v){ if(!s.startsWith(text,i)) throw new IllegalArgumentException("Bad JSON at "+i); i+=text.length(); return v; }
        void ws(){ while(i<s.length() && Character.isWhitespace(s.charAt(i))) i++; }
        boolean peek(char c){ return i<s.length() && s.charAt(i)==c; }
        void expect(char c){ ws(); if(!peek(c)) throw new IllegalArgumentException("Expected "+c+" at "+i); i++; }
    }
}
