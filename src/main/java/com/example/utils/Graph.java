package com.example.utils;

import java.util.*;


/**
 * implementation of a bidirectional graph
 */
public class Graph<T> {
    private Map<T, List<T>> map = new HashMap<>();

    public void addVertex(T n) {
        map.put(n, new LinkedList<>());
    }

    public void addEdge(T s, T d) {
        if (!map.containsKey(s)) {
            addVertex(s);
        }
        if (!map.containsKey(d)) {
            addVertex(d);
        }
        map.get(s).add(d);
        map.get(d).add(s);
    }

    public void removeEdge(T s, T d) {
        if (hasEdge(s, d)) {
            map.get(s).remove(d);
            map.get(d).remove(s);
        }
    }

    public void removeVertex(T s){
        map.remove(s);
        for(Map.Entry<T, List<T>> list: map.entrySet()){
            list.getValue().remove(s);
        }
    }

    public boolean hasEdge(T s, T d) {
        return map.get(s).contains(d);
    }

    public Set<T> getVertex() {
        return map.keySet();
    }

    public List<T> getEdges(T s) {
        if (!map.containsKey(s)) {
            return new LinkedList<>();
        }
        return map.get(s);
    }


    private void dfs(T vertex, Map<T, Boolean> visited, List<T> all) {
        visited.put(vertex, true);
        all.add(vertex);
        for (T next : getEdges(vertex)) {
            if (!visited.get(next)) {
                dfs(next, visited, all);
            }
        }
    }

    public List<List<T>> components() {
        List<List<T>> components = new ArrayList<>();
        Set<T> vertex = getVertex();
        Map<T, Boolean> visited = new HashMap<>();
        vertex.forEach((t) -> {
            visited.put(t, false);
        });

        vertex.forEach(t -> {
            List<T> all = new ArrayList<>();
            if (!visited.get(t)) {
                dfs(t, visited, all);
                components.add(all);
            }
        });
        return components;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (T v : map.keySet()) {
            builder.append(v.toString()).append(": ");
            for (T w : map.get(v)) {
                builder.append(w.toString()).append(" ");
            }
            builder.append('\n');
        }
        return builder.toString();
    }

    public void delete(){
        map.clear();
    }
}
