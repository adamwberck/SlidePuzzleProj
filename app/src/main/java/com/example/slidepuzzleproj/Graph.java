package com.example.slidepuzzleproj;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Graph<T> {
    private Map<T, List<T>> map = new HashMap<>();
    private T root;

    public Graph(T root){
        this.root = root;
    }

    public void addVertex(T s) {
        map.put(s, new LinkedList<T>());
    }

    public boolean addEdge(T source,T destination) {
        if (!map.containsKey(source)) {
            return false;
        }

        if (!map.containsKey(destination)) {
            return false;
        }

        map.get(source).add(destination);
        return true;
    }

}

