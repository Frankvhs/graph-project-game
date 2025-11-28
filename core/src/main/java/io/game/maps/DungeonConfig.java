package io.game.maps;

/**
 * Configuración simple: el "mínimo obligatorio" por nivel es
 * 6 + (level - 1). El generador intentará crear al menos ese número;
 * podrá añadir habitaciones extra si es necesario para cerrar puertas.
 */
public class DungeonConfig {

    public final int minRooms;

    public DungeonConfig(int min) {
        this.minRooms = min;
    }

    public static DungeonConfig forLevel(int level) {
        int min = 6 + (level - 1); // nivel1 -> 6, nivel2 -> 7, nivel3 -> 8, ...
        return new DungeonConfig(min);
    }
}
