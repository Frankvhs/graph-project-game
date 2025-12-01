package io.game.generator;

import io.game.components.Direction;
import io.game.components.RoomTemplate;
import io.game.maps.Room;
import io.game.maps.DungeonConfig;
import io.game.maps.DungeonGraph;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Generador adaptado a tu especificación:
 * - objetivo mínimo de habitaciones: 6 + (level-1)
 * - generación expansiva desde una start room
 * - al final, puertas "sueltas" se cierran: primero intentando colocar
 *   un SINGLE en la dirección; si no cabe, se crea un ciclo "forzando"
 *   la adición de la puerta en la habitación con la que colisiona.
 *
 * Devuelve lista de rooms a partir del grafo interno.
 */
public class DungeonGenerator {

    private final Random rnd = new Random();
    private final LinkedList<DoorSlot> openDoors = new LinkedList<>();

    private final DungeonGraph graph = new DungeonGraph();
    private DungeonConfig config;

    public DungeonGraph getGraph() { return graph; }

    public List<Room> generate(int level) {
        // limpiar
        graph.clear();
        openDoors.clear();

        config = DungeonConfig.forLevel(level);
        int target = config.minRooms; // mínimo obligatorio (6,7,8...)

        // ---- start room ----
        RoomTemplate startTpl = RoomTemplate.N_SINGLE; // simplificamos: start north single
        Room start = placeRoom(0, 0, startTpl);
        start.isStart = true;

        // ---- expand hasta target o hasta no poder ----
        while (graph.getRooms().size() < target && !openDoors.isEmpty()) {
            boolean anyPlaced = false;
            int pass = openDoors.size();

            for (int i = 0; i < pass; i++) {
                DoorSlot slot = openDoors.pollFirst();
                if (slot == null) break;

                boolean placed = tryPlaceAtSlot(slot, level);
                if (placed) anyPlaced = true;
                else openDoors.addLast(slot); // reintentar luego

                if (graph.getRooms().size() >= target) break;
            }

            if (!anyPlaced) break; // no se pudieron colocar más
        }

        // ---- cerrar puertas abiertas (cerrado o ciclos) ----
        closeAllOpenDoorsWithCycles();

        // ---- colocar escalera en una hoja (habitacion con 1 conexion) ----
        Room leaf = findFarthestLeaf(start);
        if (leaf != null) leaf.hasStairs = true;

        // ---- asegurar conectividad (BFS simple) ----
        ensureConnectivity(start);

        return new ArrayList<>(graph.getRooms());
    }

    // ----------------------------
    // placeRoom: añade room al grafo y encola sus puertas
    // ----------------------------
    private Room placeRoom(int x, int y, RoomTemplate tpl) {
        Room r = new Room(x, y, tpl);
        graph.addRoom(r);
        for (Direction d : tpl.getDoors()) openDoors.addLast(new DoorSlot(r, d));
        return r;
    }

    // ----------------------------
    // tryPlaceAtSlot: intenta colocar una room compatible en el slot
    // ----------------------------
    private boolean tryPlaceAtSlot(DoorSlot slot, int level) {
        Room src = slot.room;
        Direction d = slot.dir;
        int nx = src.x + d.dx;
        int ny = src.y + d.dy;

        Room existing = graph.getRoom(nx, ny);
        if (existing != null) {
            // si la existente tiene la puerta opuesta conectada, unir ambas
            if (existing.hasDoor(d.opposite())) {
                src.connect(d);
                existing.connect(d.opposite());
                graph.connect(src, existing, d);
                removeOpenDoor(src, d);
                removeOpenDoor(existing, d.opposite());
                return true;
            }
            return false;
        }

        // candidatos que incluyen puerta opuesta
        List<RoomTemplate> candidates = Arrays.stream(RoomTemplate.values())
                .filter(t -> t.hasDoor(d.opposite()))
                .collect(Collectors.toList());

        Collections.shuffle(candidates, rnd);

        for (RoomTemplate tpl : candidates) {
            if (canPlaceTemplateAt(nx, ny, tpl)) {
                Room newRoom = placeRoom(nx, ny, tpl);
                src.connect(d);
                newRoom.connect(d.opposite());
                graph.connect(src, newRoom, d);
                removeOpenDoor(src, d);
                autoConnectNeighbors(newRoom);
                return true;
            }
        }
        return false;
    }

    // ----------------------------
    // Comprueba si tpl es compatible en (x,y) con vecinos ya existentes
    // ----------------------------
    private boolean canPlaceTemplateAt(int x, int y, RoomTemplate tpl) {
        for (Direction d : Direction.values()) {
            Room n = graph.getRoom(x + d.dx, y + d.dy);
            if (n != null) {
                boolean tplHas = tpl.hasDoor(d);
                boolean nHas = n.hasDoor(d.opposite());
                if (tplHas != nHas) return false;
            }
        }
        return true;
    }

    // ----------------------------
    // autoConnectNeighbors: si el nuevo cuarto tiene vecinos compatibles, conecta
    // ----------------------------
    private void autoConnectNeighbors(Room r) {
        int x = r.x, y = r.y;
        for (Direction d : Direction.values()) {
            Room n = graph.getRoom(x + d.dx, y + d.dy);
            if (n != null && r.hasDoor(d) && n.hasDoor(d.opposite())) {
                r.connect(d);
                n.connect(d.opposite());
                graph.connect(r, n, d);
                removeOpenDoor(r, d);
                removeOpenDoor(n, d.opposite());
            }
        }
    }

    private void removeOpenDoor(Room r, Direction d) {
        Iterator<DoorSlot> it = openDoors.iterator();
        while (it.hasNext()) {
            DoorSlot s = it.next();
            if (s.room == r && s.dir == d) {
                it.remove();
                return;
            }
        }
    }

    // ----------------------------
    // closeAllOpenDoorsWithCycles:
    //   - intenta colocar SINGLE en dirección abierta
    //   - si no cabe (colisión), busca una room en esa dirección y
    //     * añade la puerta necesaria en esa room (modificando su template) y
    //     * crea la conexión (ciclo)
    // ----------------------------
    private void closeAllOpenDoorsWithCycles() {
        while (!openDoors.isEmpty()) {
            DoorSlot slot = openDoors.pollFirst();
            if (slot == null) break;

            Room r = slot.room;
            Direction d = slot.dir;

            int nx = r.x + d.dx;
            int ny = r.y + d.dy;

            // si ya hay room, conecta si es posible
            Room neighbor = graph.getRoom(nx, ny);
            if (neighbor != null) {
                if (neighbor.hasDoor(d.opposite())) {
                    r.connect(d);
                    neighbor.connect(d.opposite());
                    graph.connect(r, neighbor, d);
                    removeOpenDoor(r, d);
                    removeOpenDoor(neighbor, d.opposite());
                    continue;
                } else {
                    // el vecino existe pero no tiene la puerta -> forzamos añadirla
                    addDoorToRoomAndConnect(neighbor, d.opposite(), r);
                    continue;
                }
            }

            // intentamos colocar SINGLE directamente
            RoomTemplate closeTpl = RoomTemplate.valueOf(d.opposite().name() + "_SINGLE");

            if (canPlaceTemplateAt(nx, ny, closeTpl)) {
                Room end = placeRoom(nx, ny, closeTpl);
                end.connect(d.opposite());
                r.connect(d);
                graph.connect(r, end, d);
                removeOpenDoor(end, d.opposite());
                continue;
            }

            // si no cabe SINGLE (no espacio), buscamos una room en línea para formar ciclo
            Room found = findRoomInDirection(nx, ny, d);
            if (found != null) {
                // found está en la línea hacia d; conectar r <-> found creando puerta en found
                addDoorToRoomAndConnect(found, oppositeDirectionFrom(found, r), r);
            } else {
                // fallback: si no se encontró nada, intentamos buscar ANY nearby room y forzamos conexión
                Room any = findAnyNearbyRoom(nx, ny);
                if (any != null) {
                    addDoorToRoomAndConnect(any, oppositeDirectionFrom(any, r), r);
                }
            }
        }
    }

    // ----------------------------
    // Añade la puerta 'dir' a 'target' modificando su template
    // y conecta target <-> source en ese dir (dir es la dirección desde target hacia source)
    // ----------------------------
    private void addDoorToRoomAndConnect(Room target, Direction dir, Room source) {
        // crear nuevo Door set = union entre target.template.getDoors() y dir
        Set<Direction> union = new HashSet<>(target.getTemplate().getDoors());
        union.add(dir);

        // buscar RoomTemplate que tenga exactamente ese conjunto
        RoomTemplate newTpl = findTemplateWithDoors(union);
        if (newTpl != null) {
            target.setTemplate(newTpl);
        } else {
            // si no se encuentra (no debería pasar con tu enum), usamos NESO (all doors) como fallback
            target.setTemplate(RoomTemplate.NESO);
        }

        // conectar lógicamente y en grafo
        target.connect(dir);
        source.connect(dir.opposite());
        graph.connect(target, source, dir);
        // eliminar posibles open doors redundantes
        removeOpenDoor(target, dir);
        removeOpenDoor(source, dir.opposite());
    }

    // ----------------------------
    // findTemplateWithDoors: busca enum cuya getDoors() == set
    // ----------------------------
    private RoomTemplate findTemplateWithDoors(Set<Direction> doors) {
        EnumSet<Direction> check = EnumSet.noneOf(Direction.class);
        check.addAll(doors);
        for (RoomTemplate tpl : RoomTemplate.values()) {
            if (tpl.getDoors().equals(check)) return tpl;
        }
        return null;
    }

    // ----------------------------
    // Busca la primera room existente en la misma línea (en dirección d)
    // empezando desde (nx,ny) y yendo en pasos de 1 celda.
    // ----------------------------
    private Room findRoomInDirection(int nx, int ny, Direction d) {
        int step = 1;
        while (step < 50) { // límite razonable para no infinite loop
            Room r = graph.getRoom(nx + d.dx * (step - 1), ny + d.dy * (step - 1));
            if (r != null) return r;
            step++;
        }
        return null;
    }

    // ----------------------------
    // fallback: busca cualquier room cercana (radio pequeño)
    // ----------------------------
    private Room findAnyNearbyRoom(int x, int y) {
        for (int dx = -4; dx <= 4; dx++) {
            for (int dy = -4; dy <= 4; dy++) {
                Room r = graph.getRoom(x + dx, y + dy);
                if (r != null) return r;
            }
        }
        return null;
    }

    // ----------------------------
    // Calcula la dirección desde 'near' hacia 'fromRoom' (útil para conectar)
    // ----------------------------
    private Direction oppositeDirectionFrom(Room near, Room fromRoom) {
        int dx = fromRoom.x - near.x;
        int dy = fromRoom.y - near.y;
        if (dx == 1 && dy == 0) return Direction.E;
        if (dx == -1 && dy == 0) return Direction.O;
        if (dx == 0 && dy == 1) return Direction.N;
        if (dx == 0 && dy == -1) return Direction.S;
        // fallback: si no está exactamente alineada, escoger una dirección aproximada
        if (Math.abs(dx) >= Math.abs(dy)) return dx > 0 ? Direction.E : Direction.O;
        return dy > 0 ? Direction.N : Direction.S;
    }

    // ----------------------------
    // findFarthestLeaf: hoja más lejana desde start (hoja = conexiones == 1)
    // ----------------------------
    private Room findFarthestLeaf(Room start) {
        Collection<Room> all = graph.getRooms();
        if (all.isEmpty()) return start;

        Queue<Room> q = new LinkedList<>();
        Map<Room, Integer> dist = new HashMap<>();
        q.add(start);
        dist.put(start, 0);

        while (!q.isEmpty()) {
            Room r = q.poll();
            int d0 = dist.get(r);
            for (Direction dir : Direction.values()) {
                if (r.isConnected(dir)) {
                    Room n = graph.getRoom(r.x + dir.dx, r.y + dir.dy);
                    if (n != null && !dist.containsKey(n)) {
                        dist.put(n, d0 + 1);
                        q.add(n);
                    }
                }
            }
        }

        int max = -1;
        List<Room> best = new ArrayList<>();
        for (Room r : all) {
            int connections = 0;
            for (Direction d : Direction.values()) if (r.isConnected(d)) connections++;
            if (connections == 1) {
                Integer dists = dist.get(r);
                if (dists == null) continue;
                if (dists > max) { max = dists; best.clear(); best.add(r); }
                else if (dists == max) best.add(r);
            }
        }

        if (!best.isEmpty()) return best.get(rnd.nextInt(best.size()));

        // fallback: choose farthest in dist map
        Room far = start;
        for (Map.Entry<Room, Integer> e : dist.entrySet()) {
            if (dist.get(far) == null || e.getValue() > dist.get(far)) far = e.getKey();
        }
        return far;
    }

    private void ensureConnectivity(Room start) {
        Queue<Room> q = new LinkedList<>();
        Set<Room> seen = new HashSet<>();

        q.add(start);
        seen.add(start);
        while (!q.isEmpty()) {
            Room r = q.poll();
            for (Direction d : Direction.values()) {
                if (r.isConnected(d)) {
                    Room n = graph.getRoom(r.x + d.dx, r.y + d.dy);
                    if (n != null && seen.add(n)) q.add(n);
                }
            }
        }
    }

    private static class DoorSlot {
        final Room room;
        final Direction dir;
        DoorSlot(Room r, Direction d) { this.room = r; this.dir = d; }
    }
}
