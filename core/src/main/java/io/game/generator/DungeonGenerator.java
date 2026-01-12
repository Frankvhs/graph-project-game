package io.game.generator;

import io.game.components.Direction;
import io.game.components.RoomTemplate;
import io.game.maps.Room;
import io.game.maps.DungeonConfig;
import io.game.maps.DungeonGraph;

import java.util.*;
import java.util.stream.Collectors;

/**
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
        // Usar una habitación con múltiples puertas para mejor expansión
        RoomTemplate startTpl = RoomTemplate.NESO; // 4 puertas para máxima expansión
        Room start = placeRoom(0, 0, startTpl);
        start.isStart = true;

        // ---- expand hasta target o hasta no poder ----
        int maxAttempts = target * 10; // más intentos para asegurar expansión
        int attempts = 0;
        int stuckCounter = 0; // contador de pasadas sin progreso
        
        while (graph.getRooms().size() < target && !openDoors.isEmpty() && attempts < maxAttempts) {
            boolean anyPlaced = false;
            int pass = openDoors.size();

            for (int i = 0; i < pass && graph.getRooms().size() < target; i++) {
                DoorSlot slot = openDoors.pollFirst();
                if (slot == null) break;

                boolean placed = tryPlaceAtSlot(slot, level);
                if (placed) {
                    anyPlaced = true;
                    stuckCounter = 0; // reset al tener éxito
                } else {
                    openDoors.addLast(slot); // reintentar luego
                }
                
                attempts++;
            }

            if (!anyPlaced) {
                stuckCounter++;
                // Si llevamos 3 pasadas sin colocar nada y aún no llegamos al mínimo, 
                // intentar forzar colocación en posiciones aleatorias
                if (stuckCounter >= 3 && graph.getRooms().size() < target) {
                    boolean forced = tryForceRandomRoom();
                    if (forced) {
                        stuckCounter = 0;
                        anyPlaced = true;
                    } else {
                        break; // definitivamente no podemos expandir más
                    }
                } else if (stuckCounter >= 5) {
                    break; // demasiados intentos fallidos
                }
            }
        }

        // ---- cerrar puertas abiertas (cerrado o ciclos) ----
        closeAllOpenDoorsWithCycles();
        
        // ---- VALIDACIÓN FINAL MEJORADA: asegurar sincronización perfecta ----
        // Realizar múltiples pasadas hasta que no haya cambios
        boolean changesMade = true;
        int validationPasses = 0;
        int maxValidationPasses = 3;
        
        while (changesMade && validationPasses < maxValidationPasses) {
            changesMade = false;
            validationPasses++;
            
            System.out.println("=== Validation pass " + validationPasses + " ===");
            
            for (Room room : graph.getRooms()) {
                // Recopilar SOLO las puertas que están REALMENTE conectadas
                Set<Direction> actualConnections = EnumSet.noneOf(Direction.class);
                for (Direction d : Direction.values()) {
                    if (room.isConnected(d)) {
                        // Verificar que realmente hay una habitación vecina conectada
                        Room neighbor = graph.getRoom(room.x + d.dx, room.y + d.dy);
                        if (neighbor != null && neighbor.isConnected(d.opposite())) {
                            actualConnections.add(d);
                        } else {
                            // La conexión no es válida, limpiarla
                            System.out.println("  Cleaning invalid connection at (" + room.x + "," + room.y + ") direction " + d);
                            room.disconnect(d);
                            changesMade = true;
                        }
                    }
                }
                
                // Comparar con el template actual
                Set<Direction> templateDoors = new HashSet<>(room.getTemplate().getDoors());
                
                if (!templateDoors.equals(actualConnections)) {
                    // Hay desincronización - actualizar template
                    System.out.println("  Room at (" + room.x + "," + room.y + ") - Template doors: " + templateDoors + " vs Actual connections: " + actualConnections);
                    
                    if (actualConnections.isEmpty()) {
                        System.err.println("WARNING: Room at (" + room.x + "," + room.y + ") has no valid connections!");
                        // Buscar al menos una puerta del template original
                        for (Direction d : templateDoors) {
                            actualConnections.add(d);
                            break; // solo una puerta
                        }
                    }
                    
                    RoomTemplate correctedTemplate = findTemplateWithDoors(actualConnections);
                    if (correctedTemplate != null) {
                        System.out.println("  -> Updating template from " + room.getTemplate() + " to " + correctedTemplate);
                        room.setTemplate(correctedTemplate);
                        changesMade = true;
                    } else {
                        System.err.println("ERROR: Could not find template for connections: " + actualConnections + " at room (" + room.x + "," + room.y + ")");
                    }
                }
            }
        }
        
        System.out.println("=== Validation completed after " + validationPasses + " passes ===");
        
        // ---- REPORTE FINAL: verificar que todas las habitaciones son válidas ----
        System.out.println("=== Final dungeon validation ===");
        boolean allValid = true;
        for (Room room : graph.getRooms()) {
            Set<Direction> templateDoors = room.getTemplate().getDoors();
            Set<Direction> connectedDoors = EnumSet.noneOf(Direction.class);
            
            for (Direction d : Direction.values()) {
                if (room.isConnected(d)) {
                    connectedDoors.add(d);
                }
            }
            
            if (!templateDoors.equals(connectedDoors)) {
                System.err.println("ERROR: Room at (" + room.x + "," + room.y + ") has mismatched doors!");
                System.err.println("  Template: " + templateDoors + " vs Connected: " + connectedDoors);
                allValid = false;
            }
        }
        
        if (allValid) {
            System.out.println("✓ All " + graph.getRooms().size() + " rooms are valid (no doors to void)");
        } else {
            System.err.println("✗ Some rooms have invalid doors!");
        }

        // ---- colocar escalera en una hoja (habitacion con 1 conexion) ----
        Room leaf = findFarthestLeaf(start);
        if (leaf != null) leaf.hasStairs = true;
        
        // ---- generar cofres en algunas habitaciones ----
        generateChests(start, leaf);

        // ---- asegurar conectividad (BFS simple) ----
        ensureConnectivity(start);
        
        return new ArrayList<>(graph.getRooms());
    }

    // ----------------------------
    // placeRoom: añade room al grafo y encola sus puertas
    // ----------------------------
    private Room placeRoom(int x, int y, RoomTemplate tpl) {
        return placeRoom(x, y, tpl, true);
    }
    
    private Room placeRoom(int x, int y, RoomTemplate tpl, boolean addDoorsToQueue) {
        Room r = new Room(x, y, tpl);
        graph.addRoom(r);
        if (addDoorsToQueue) {
            for (Direction d : tpl.getDoors()) openDoors.addLast(new DoorSlot(r, d));
        }
        return r;
    }
    
    // ----------------------------
    // tryForceRandomRoom: intenta forzar colocación de una habitación en posición aleatoria
    // para superar bloqueos cuando el algoritmo normal falla
    // ----------------------------
    private boolean tryForceRandomRoom() {
        // Obtener todas las habitaciones existentes
        List<Room> rooms = new ArrayList<>(graph.getRooms());
        if (rooms.isEmpty()) return false;
        
        // Intentar desde habitaciones aleatorias
        Collections.shuffle(rooms, rnd);
        
        for (Room room : rooms) {
            // Intentar en todas las direcciones
            Direction[] dirs = Direction.values();
            List<Direction> shuffledDirs = Arrays.asList(dirs);
            Collections.shuffle(shuffledDirs, rnd);
            
            for (Direction d : shuffledDirs) {
                int nx = room.x + d.dx;
                int ny = room.y + d.dy;
                
                // Verificar que no haya habitación ahí
                if (graph.getRoom(nx, ny) != null) continue;
                
                // Intentar colocar una habitación simple con puerta opuesta
                RoomTemplate simpleTpl = null;
                try {
                    simpleTpl = RoomTemplate.valueOf(d.opposite().name() + "_SINGLE");
                } catch (IllegalArgumentException e) {
                    continue;
                }
                
                if (canPlaceTemplateAt(nx, ny, simpleTpl)) {
                    // Añadir puerta a la habitación origen si no la tiene
                    if (!room.hasDoor(d)) {
                        Set<Direction> newDoors = new HashSet<>(room.getTemplate().getDoors());
                        newDoors.add(d);
                        RoomTemplate newTpl = findTemplateWithDoors(newDoors);
                        if (newTpl != null) {
                            room.setTemplate(newTpl);
                        } else {
                            room.setTemplate(RoomTemplate.NESO); // fallback con todas las puertas
                        }
                    }
                    
                    // Colocar nueva habitación
                    Room newRoom = placeRoom(nx, ny, simpleTpl);
                    room.connect(d);
                    newRoom.connect(d.opposite());
                    graph.connect(room, newRoom, d);
                    removeOpenDoor(room, d);
                    return true;
                }
            }
        }
        
        return false;
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
        List<DoorSlot> unresolved = new ArrayList<>();
        
        while (!openDoors.isEmpty()) {
            DoorSlot slot = openDoors.pollFirst();
            if (slot == null) break;

            Room r = slot.room;
            Direction d = slot.dir;
            
            // si esta puerta ya está conectada, omitir
            if (r.isConnected(d)) continue;

            int nx = r.x + d.dx;
            int ny = r.y + d.dy;

            // si ya hay room, conecta si es posible
            Room neighbor = graph.getRoom(nx, ny);
            if (neighbor != null) {
                if (neighbor.hasDoor(d.opposite()) && !neighbor.isConnected(d.opposite())) {
                    r.connect(d);
                    neighbor.connect(d.opposite());
                    graph.connect(r, neighbor, d);
                    removeOpenDoor(neighbor, d.opposite());
                    continue;
                } else if (!neighbor.hasDoor(d.opposite())) {
                    // el vecino existe pero no tiene la puerta -> forzamos añadirla
                    addDoorToRoomAndConnect(neighbor, d.opposite(), r);
                    continue;
                } else {
                    // la puerta del vecino ya está conectada, no podemos conectar
                    unresolved.add(slot);
                    continue;
                }
            }

            // intentamos colocar SINGLE directamente con la puerta hacia el origen
            RoomTemplate closeTpl = null;
            try {
                closeTpl = RoomTemplate.valueOf(d.opposite().name() + "_SINGLE");
            } catch (IllegalArgumentException e) {
                // si no existe ese template, marcar como no resuelto
                unresolved.add(slot);
                continue;
            }

            if (canPlaceTemplateAt(nx, ny, closeTpl)) {
                // Usar placeRoom sin añadir puertas a la cola
                Room end = placeRoom(nx, ny, closeTpl, false);
                end.connect(d.opposite());
                r.connect(d);
                graph.connect(r, end, d);
                continue;
            }

            // si no cabe SINGLE (no espacio), buscamos una room en línea para formar ciclo
            Room found = findRoomInDirection(nx, ny, d);
            if (found != null) {
                // found está en la línea hacia d; conectar r <-> found creando puerta en found
                Direction dirToFound = oppositeDirectionFrom(found, r);
                if (found.hasDoor(dirToFound) && !found.isConnected(dirToFound)) {
                    addDoorToRoomAndConnect(found, dirToFound, r);
                    continue;
                }
            }
            
            // fallback: si no se encontró nada, intentamos buscar ANY nearby room y forzamos conexión
            Room any = findAnyNearbyRoom(nx, ny);
            if (any != null) {
                Direction dirToAny = oppositeDirectionFrom(any, r);
                // solo conectar si es posible
                if (!any.isConnected(dirToAny)) {
                    addDoorToRoomAndConnect(any, dirToAny, r);
                    continue;
                }
            }
            
            // si definitivamente no podemos conectar, marcar para eliminar
            unresolved.add(slot);
        }
        
        // cerrar puertas que definitivamente van al vacío cambiando su template
        for (DoorSlot slot : unresolved) {
            removeDoorFromRoom(slot.room, slot.dir);
        }
    }
    
    // ----------------------------
    // validateAndFixAllRooms: elimina puertas no conectadas de todas las habitaciones
    // ----------------------------
    private void validateAndFixAllRooms() {
        for (Room room : graph.getRooms()) {
            Set<Direction> connectedDoors = EnumSet.noneOf(Direction.class);
            
            // recopilar solo las puertas que están realmente conectadas
            for (Direction d : Direction.values()) {
                if (room.isConnected(d)) {
                    connectedDoors.add(d);
                }
            }
            
            // SIEMPRE actualizar el template para que coincida exactamente con las conexiones
            Set<Direction> templateDoors = room.getTemplate().getDoors();
            
            if (!templateDoors.equals(connectedDoors)) {
                if (connectedDoors.isEmpty()) {
                    // Si no hay conexiones, algo salió mal - mantener al menos una puerta
                    System.err.println("WARNING: Room at (" + room.x + "," + room.y + ") has no connections!");
                    // Buscar si tiene alguna puerta en el template y mantenerla
                    if (!templateDoors.isEmpty()) {
                        connectedDoors.add(templateDoors.iterator().next());
                    }
                }
                
                if (!connectedDoors.isEmpty()) {
                    RoomTemplate newTpl = findTemplateWithDoors(connectedDoors);
                    if (newTpl != null) {
                        room.setTemplate(newTpl);
                    } else {
                        System.err.println("ERROR: Could not find template for doors: " + connectedDoors);
                    }
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
    // removeDoorFromRoom: elimina una puerta del template de una habitación
    // ----------------------------
    private void removeDoorFromRoom(Room room, Direction dir) {
        Set<Direction> currentDoors = new HashSet<>(room.getTemplate().getDoors());
        if (!currentDoors.contains(dir)) return; // no tiene esa puerta
        
        // si ya está conectada, no eliminar
        if (room.isConnected(dir)) return;
        
        currentDoors.remove(dir);
        
        if (currentDoors.isEmpty()) {
            // si se quedaría sin puertas, buscar puertas conectadas
            Set<Direction> connectedDoors = EnumSet.noneOf(Direction.class);
            for (Direction d : Direction.values()) {
                if (room.isConnected(d)) {
                    connectedDoors.add(d);
                }
            }
            
            if (!connectedDoors.isEmpty()) {
                currentDoors = connectedDoors;
            } else {
                // Último recurso: mantener template original
                System.err.println("WARNING: Cannot remove door from room at (" + room.x + "," + room.y + ") - no other doors");
                return;
            }
        }
        
        RoomTemplate newTpl = findTemplateWithDoors(currentDoors);
        if (newTpl != null) {
            room.setTemplate(newTpl);
        } else {
            System.err.println("ERROR: Could not find template for remaining doors: " + currentDoors);
        }
    }

    // ----------------------------
    // Busca la primera room existente en la misma línea (en dirección d)
    // empezando desde (nx,ny) y yendo en pasos de 1 celda.
    // ----------------------------
    private Room findRoomInDirection(int nx, int ny, Direction d) {
        int step = 1;
        while (step < 50) { // límite razonable para no infinite loop
            Room r = graph.getRoom(nx + d.dx * step, ny + d.dy * step);
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
    // generateChests: coloca cofres y llaves en habitaciones aleatorias
    // ----------------------------
    private void generateChests(Room start, Room stairsRoom) {
        List<Room> allRooms = new ArrayList<>(graph.getRooms());
        // No colocar cofres en la habitación inicial ni en la que tiene escaleras
        allRooms.remove(start);
        if (stairsRoom != null) {
            allRooms.remove(stairsRoom);
        }
        
        // Colocar cofres en 30-50% de las habitaciones restantes
        int numChests = Math.max(1, (int)(allRooms.size() * (0.3 + rnd.nextFloat() * 0.2)));
        Collections.shuffle(allRooms, rnd);
        
        List<Room> roomsWithChests = new ArrayList<>();
        for (int i = 0; i < Math.min(numChests, allRooms.size()); i++) {
            allRooms.get(i).hasChest = true;
            roomsWithChests.add(allRooms.get(i));
        }
        
        // Generar llaves en habitaciones diferentes a las de los cofres
        List<Room> roomsWithoutChests = new ArrayList<>(allRooms);
        roomsWithoutChests.removeAll(roomsWithChests);
        
        if (!roomsWithoutChests.isEmpty()) {
            // Generar una llave por cada cofre (o al menos una)
            int numKeys = Math.max(1, numChests);
            Collections.shuffle(roomsWithoutChests, rnd);
            
            for (int i = 0; i < Math.min(numKeys, roomsWithoutChests.size()); i++) {
                roomsWithoutChests.get(i).hasKey = true;
            }
        }
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
