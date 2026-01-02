package io.game.generator;

import io.game.components.RoomTemplate;
import io.game.components.Direction;
import io.game.maps.Room;
import io.game.maps.DungeonConfig;
import io.game.maps.DungeonGraph;
import java.util.*;

public class DungeonGenerator {
    
    private int currentLevel;
    private DungeonGraph graph;
    private Room startRoom;
    private Random random;
    
    public DungeonGenerator(int initialLevel) {
        this.currentLevel = initialLevel;
        this.graph = new DungeonGraph();
        this.random = new Random();
    }
    
    /**
     * Genera una nueva mazmorra para el nivel actual
     */
    public void generate() {
        graph.clear();
        
        // Obtener configuración para el nivel actual
        DungeonConfig config = DungeonConfig.forLevel(currentLevel);
        int targetRooms = config.minRooms; // 6, 7, 8, etc.
        
        // 1. Crear habitación inicial (siempre SINGLE)
        createInitialRoom();
        
        // 2. Crear las habitaciones adicionales
        createAdditionalRooms(targetRooms - 1); // -1 porque ya creamos la inicial
        
        // 3. Conectar las habitaciones entre sí
        connectRooms();
        
        // 4. Cerrar puertas abiertas con SINGLEs
        closeOpenDoorsWithSingles();
        
        // 5. Colocar escaleras en una habitación SINGLE (no la inicial)
        placeStairsInSingleRoom();
    }
    
    /**
     * Crear habitación inicial (siempre SINGLE)
     */
    private void createInitialRoom() {
        Direction[] directions = {Direction.N, Direction.E, Direction.S, Direction.O};
        Direction startDir = directions[random.nextInt(directions.length)];
        
        RoomTemplate startTemplate = getSingleTemplateForDirection(startDir);
        startRoom = new Room(0, 0, startTemplate);
        startRoom.isStart = true;
        graph.addRoom(startRoom);
    }
    
    /**
     * Crear habitaciones adicionales
     */
    private void createAdditionalRooms(int count) {
        // Posiciones ya ocupadas
        Set<String> occupiedPositions = new HashSet<>();
        occupiedPositions.add("0,0"); // La posición inicial
        
        // Crear las habitaciones en posiciones aleatorias cercanas
        for (int i = 0; i < count; i++) {
            int attempts = 0;
            boolean placed = false;
            
            while (!placed && attempts < 100) {
                attempts++;
                
                // Elegir una habitación existente aleatoria
                List<Room> existingRooms = new ArrayList<>(graph.getRooms());
                Room baseRoom = existingRooms.get(random.nextInt(existingRooms.size()));
                
                // Elegir una dirección aleatoria
                Direction[] directions = Direction.values();
                Direction dir = directions[random.nextInt(directions.length)];
                
                // Calcular nueva posición
                int newX = baseRoom.x + dir.dx;
                int newY = baseRoom.y + dir.dy;
                
                // Verificar que no esté ocupada
                String posKey = newX + "," + newY;
                if (!occupiedPositions.contains(posKey)) {
                    // Crear una plantilla aleatoria (no SINGLE)
                    RoomTemplate template = createRandomNonSingleTemplate();
                    Room newRoom = new Room(newX, newY, template);
                    
                    // Añadir al grafo
                    graph.addRoom(newRoom);
                    occupiedPositions.add(posKey);
                    placed = true;
                }
            }
            
            // Si no se pudo colocar después de muchos intentos, usar una posición aleatoria
            if (!placed) {
                int x, y;
                String posKey;
                do {
                    x = random.nextInt(10) - 5; // Entre -5 y 5
                    y = random.nextInt(10) - 5;
                    posKey = x + "," + y;
                } while (occupiedPositions.contains(posKey));
                
                RoomTemplate template = createRandomNonSingleTemplate();
                Room newRoom = new Room(x, y, template);
                graph.addRoom(newRoom);
                occupiedPositions.add(posKey);
            }
        }
    }
    
    /**
     * Conectar las habitaciones entre sí
     */
    private void connectRooms() {
        List<Room> rooms = new ArrayList<>(graph.getRooms());
        
        // Para cada habitación, intentar conectar con vecinos
        for (Room room : rooms) {
            // Verificar las 4 direcciones
            for (Direction dir : Direction.values()) {
                int neighborX = room.x + dir.dx;
                int neighborY = room.y + dir.dy;
                
                Room neighbor = graph.getRoom(neighborX, neighborY);
                if (neighbor != null) {
                    // Hay un vecino, conectar si es posible
                    connectTwoRooms(room, neighbor, dir);
                }
            }
        }
    }
    
    /**
     * Conectar dos habitaciones
     */
    private void connectTwoRooms(Room room1, Room room2, Direction dirFrom1To2) {
        Direction dirFrom2To1 = dirFrom1To2.opposite();
        
        // Asegurar que ambas habitaciones tengan las puertas necesarias
        ensureRoomHasDoor(room1, dirFrom1To2);
        ensureRoomHasDoor(room2, dirFrom2To1);
        
        // Conectar en el grafo
        graph.connect(room1, room2, dirFrom1To2);
        
        // Marcar como conectadas
        room1.connect(dirFrom1To2);
        room2.connect(dirFrom2To1);
    }
    
    /**
     * Asegurar que una habitación tenga una puerta en una dirección
     */
    private void ensureRoomHasDoor(Room room, Direction dir) {
        if (!room.getTemplate().hasDoor(dir)) {
            RoomTemplate newTemplate = room.getTemplate().withDoor(dir);
            room.setTemplate(newTemplate);
        }
    }
    
    /**
     * Cerrar puertas abiertas con habitaciones SINGLE
     */
    private void closeOpenDoorsWithSingles() {
        List<Room> rooms = new ArrayList<>(graph.getRooms());
        
        for (Room room : rooms) {
            // Para cada puerta que tenga la habitación
            for (Direction dir : room.getTemplate().getDoors()) {
                // Si no está conectada, cerrarla
                if (!room.isConnected(dir)) {
                    closeDoorWithSingle(room, dir);
                }
            }
        }
    }
    
    /**
     * Cerrar una puerta con una habitación SINGLE
     */
    private void closeDoorWithSingle(Room source, Direction dir) {
        int targetX = source.x + dir.dx;
        int targetY = source.y + dir.dy;
        
        // Verificar si ya hay una habitación en esa posición
        Room existing = graph.getRoom(targetX, targetY);
        
        if (existing != null) {
            // Ya hay una habitación, intentar conectarla
            tryConnectExistingRoom(source, dir, existing);
        } else {
            // Crear una nueva habitación SINGLE
            createSingleRoomToClose(source, dir, targetX, targetY);
        }
    }
    
    /**
     * Intentar conectar con una habitación existente
     */
    private void tryConnectExistingRoom(Room source, Direction dir, Room target) {
        Direction opposite = dir.opposite();
        
        // Si el objetivo ya tiene la puerta opuesta, conectar
        if (target.hasDoor(opposite)) {
            connectTwoRooms(source, target, dir);
        } else {
            // Intentar añadir la puerta al objetivo
            tryAddDoorToRoom(target, opposite);
            
            // Si ahora tiene la puerta, conectar
            if (target.hasDoor(opposite)) {
                connectTwoRooms(source, target, dir);
            } else {
                // No se pudo añadir la puerta, quitar la puerta de la fuente
                removeDoorFromRoom(source, dir);
            }
        }
    }
    
    /**
     * Intentar añadir una puerta a una habitación
     */
    private void tryAddDoorToRoom(Room room, Direction dir) {
        RoomTemplate newTemplate = room.getTemplate().withDoor(dir);
        room.setTemplate(newTemplate);
    }
    
    /**
     * Quitar una puerta de una habitación
     */
    private void removeDoorFromRoom(Room room, Direction dir) {
        Set<Direction> doors = new HashSet<>(room.getTemplate().getDoors());
        doors.remove(dir);
        
        // Buscar una plantilla que se ajuste
        RoomTemplate newTemplate = findTemplateWithDoors(doors);
        room.setTemplate(newTemplate);
    }
    
    /**
     * Crear una habitación SINGLE para cerrar una puerta
     */
    private void createSingleRoomToClose(Room source, Direction dir, int x, int y) {
        RoomTemplate singleTemplate = getSingleTemplateForDirection(dir.opposite());
        Room closingRoom = new Room(x, y, singleTemplate);
        
        // Añadir al grafo
        graph.addRoom(closingRoom);
        
        // Conectar
        connectTwoRooms(source, closingRoom, dir);
    }
    
    /**
     * Colocar escaleras en una habitación SINGLE (no la inicial)
     */
    private void placeStairsInSingleRoom() {
        List<Room> singleRooms = new ArrayList<>();
        
        // Buscar habitaciones SINGLE
        for (Room room : graph.getRooms()) {
            if (room != startRoom && room.getTemplate().doorCount() == 1) {
                singleRooms.add(room);
            }
        }
        
        // Si hay SINGLEs, elegir una aleatoria
        if (!singleRooms.isEmpty()) {
            Room stairsRoom = singleRooms.get(random.nextInt(singleRooms.size()));
            stairsRoom.hasStairs = true;
        } else {
            // Si no hay, buscar la habitación más lejana
            Room farthest = findFarthestRoom();
            if (farthest != null) {
                farthest.hasStairs = true;
            }
        }
    }
    
    /**
     * Encuentra la habitación más lejana de la inicial
     */
    private Room findFarthestRoom() {
        Room farthest = null;
        double maxDistance = 0;
        
        for (Room room : graph.getRooms()) {
            if (room == startRoom) continue;
            
            double distance = Math.sqrt(
                Math.pow(room.x - startRoom.x, 2) + 
                Math.pow(room.y - startRoom.y, 2)
            );
            
            if (distance > maxDistance) {
                maxDistance = distance;
                farthest = room;
            }
        }
        
        return farthest;
    }
    
    /**
     * Obtener plantilla SINGLE para una dirección
     */
    private RoomTemplate getSingleTemplateForDirection(Direction dir) {
        switch (dir) {
            case N: return RoomTemplate.N_SINGLE;
            case E: return RoomTemplate.E_SINGLE;
            case S: return RoomTemplate.S_SINGLE;
            case O: return RoomTemplate.O_SINGLE;
            default: return RoomTemplate.N_SINGLE;
        }
    }
    
    /**
     * Crear una plantilla aleatoria que NO sea SINGLE
     */
    private RoomTemplate createRandomNonSingleTemplate() {
        List<RoomTemplate> nonSingleTemplates = new ArrayList<>();
        
        for (RoomTemplate template : RoomTemplate.values()) {
            if (template.doorCount() > 1) {
                nonSingleTemplates.add(template);
            }
        }
        
        if (nonSingleTemplates.isEmpty()) {
            return RoomTemplate.NESO; // Fallback
        }
        
        return nonSingleTemplates.get(random.nextInt(nonSingleTemplates.size()));
    }
    
    /**
     * Encontrar plantilla con un conjunto específico de puertas
     */
    private RoomTemplate findTemplateWithDoors(Set<Direction> doors) {
        // Buscar coincidencia exacta
        for (RoomTemplate template : RoomTemplate.values()) {
            if (template.getDoors().equals(doors)) {
                return template;
            }
        }
        
        // Buscar superset más pequeño
        RoomTemplate best = null;
        int bestCount = Integer.MAX_VALUE;
        
        for (RoomTemplate template : RoomTemplate.values()) {
            if (template.getDoors().containsAll(doors)) {
                int count = template.doorCount();
                if (count < bestCount) {
                    bestCount = count;
                    best = template;
                }
            }
        }
        
        return best != null ? best : RoomTemplate.NESO;
    }
    
    /**
     * Cuando el jugador encuentra las escaleras
     */
    public void onStairsFound() {
        currentLevel++;
        generate();
    }
    
    // Métodos de acceso
    
    public DungeonGraph getGraph() {
        return graph;
    }
    
    public Room getStartRoom() {
        return startRoom;
    }
    
    public int getCurrentLevel() {
        return currentLevel;
    }
    
    public Collection<Room> getRooms() {
        return graph.getRooms();
    }
}