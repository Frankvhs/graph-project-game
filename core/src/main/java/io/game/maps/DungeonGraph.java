package io.game.maps;

import io.game.components.Direction;
import java.util.*;

/**
 * Grafo simple que mapea (x,y) -> Node(room) y guarda vecinos.
 */
public class DungeonGraph {

    public static class Node {
        public final Room room;
        public final EnumMap<Direction, Node> neighbors = new EnumMap<>(Direction.class);

        public Node(Room r) { this.room = r; }
    }

    private final Map<Long, Node> graph = new HashMap<>();

    private long key(int x, int y) {
        return (((long) x) << 32) ^ (y & 0xffffffffL);
    }

    /** Limpia todo el grafo (mantiene la instancia) */
    public void clear() {
        graph.clear();
    }

    public Node addRoom(Room r) {
        Node n = new Node(r);
        graph.put(key(r.x, r.y), n);
        return n;
    }

    public void connect(Room a, Room b, Direction d) {
        Node na = graph.get(key(a.x, a.y));
        Node nb = graph.get(key(b.x, b.y));
        if (na == null || nb == null) return;
        na.neighbors.put(d, nb);
        nb.neighbors.put(d.opposite(), na);
    }

    public Room getRoom(int x, int y) {
        Node n = graph.get(key(x, y));
        return n == null ? null : n.room;
    }

    public Collection<Room> getRooms() {
        List<Room> out = new ArrayList<>();
        for (Node n : graph.values()) out.add(n.room);
        return out;
    }

    public Node getNode(Room r) {
        return graph.get(key(r.x, r.y));
    }
}
