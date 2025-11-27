package io.game.managers;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;

/**
 * Clase global para gestionar recursos del juego (texturas y animaciones)
 * Usa AssetManager internamente y permite insertar en una cola las animaciones para ser
 * construidas al llamar a finish().
 * 
 * @author Rodny Estrada <rrodnyestrada1@gmail.com>
 */
public class Resources {

    private static final AssetManager assets = new AssetManager();
    private static final Map<String, Animation<TextureRegion>> animations = new HashMap<>();
    private static final Queue<AnimationJob> animationQueue = new LinkedList<>();

    /**
     * finaliza la carga de todos los recursos pendientes y construye las animaciones en cola
     * Este metodo debe llamarse despues de cargar todos los recursos y antes de usarlos
     */
    public static void finish() {
        assets.finishLoading();

        // construir las animaciones
        while (!animationQueue.isEmpty()) {
            AnimationJob job = animationQueue.poll();
            buildAnimation(job);
        }
    }

    // ============================================================
    // ANIMACIONES
    // ============================================================
    
    /**
     * Carga una animación desde un archivo de textura, dividiéndola en frames automáticamente.
     */
    public static void loadAnimation(String path, float frameWidth, float frameHeight, float speed) {
        loadAnimation(path, frameWidth, frameHeight, speed, 0, 0, PlayMode.LOOP);
    }

    /**
     * Carga una animación desde un archivo de textura con padding entre frames.
     * Encola la construcción de la animación para ser procesada al llamar a finish()
     */
    public static void loadAnimation(String path, float frameWidth, float frameHeight,
                                     float speed, float padX, float padY, PlayMode playMode) {

        loadTexture(path);
        animationQueue.add(new AnimationJob(
                path, frameWidth, frameHeight, speed, padX, padY, playMode
        ));
    }

    /**
     * Carga una animacion usando nombre y ruta base, construyendo la ruta automaticamente
     */
    public static void loadAnimation(String name, String basePath,
                                     float frameWidth, float frameHeight, float speed) {
        loadAnimation(name, basePath, frameWidth, frameHeight, speed, 0, 0, PlayMode.LOOP);
    }

    /**
     * Carga una animación usando nombre y ruta base con padding entre frames.
     */
    public static void loadAnimation(String name, String basePath,
                                     float frameWidth, float frameHeight,
                                     float speed, float padX, float padY, PlayMode mode) {

        loadAnimation(joinPath(name, basePath), frameWidth, frameHeight, speed, padX, padY, mode);
    }

    /**
     * Construye la animación usando la textura ya cargada y los parámetros del job.
     * Divide la textura en frames y crea la animación con modo LOOP.
     */
    private static void buildAnimation(AnimationJob job) {
        Texture texture = getTexture(job.path);

        Array<TextureRegion> frames = new Array<>();

        int texWidth = texture.getWidth();
        int texHeight = texture.getHeight();

        int cols = (int) (texWidth / job.frameWidth);
        int rows = (int) (texHeight / job.frameHeight);

        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {

                int cellX = (int)(x * job.frameWidth);
                int cellY = (int)(y * job.frameHeight);

                TextureRegion region = new TextureRegion(
                        texture,
                        (int) (cellX + job.padX),
                        (int) (cellY + job.padY),
                        (int) (job.frameWidth - job.padX * 2),
                        (int) (job.frameHeight - job.padY * 2)
                );

                frames.add(region);
            }
        }

        Animation<TextureRegion> animation = new Animation<>(job.speed, frames, job.playMode);
        animations.put(job.path, animation);
    }

    /**
     * Obtiene una animación previamente cargada por su ruta completa.
     */
    public static Animation<TextureRegion> getAnimation(String path) {
        Animation<TextureRegion> a = animations.get(path);
        if (a == null) throw new IllegalArgumentException("Animation not found: " + path);
        return a;
    }

    /**
     *obtiene una animación previamente cargada por nombre y ruta base
     */
    public static Animation<TextureRegion> getAnimation(String name, String basePath) {
        return getAnimation(joinPath(name, basePath));
    }

    // ============================================================
    // MeTODOS PARA TEXTURAS
    // ============================================================
    
    /**
     * Carga una textura desde la ruta especificada.
     */
    public static void loadTexture(String path) {
        assets.load(path, Texture.class);
    }

    /**
     * Carga una textura usando nombre y ruta base.
     */
    public static void loadTexture(String name, String basePath) {
        loadTexture(joinPath(name, basePath));
    }

    /**
     * Obtiene una textura previamente cargada por su ruta completa
     */
    public static Texture getTexture(String path) {
        return assets.get(path, Texture.class);
    }

    /**
     * Obtiene una textura previamente cargada por nombre y ruta bas
     */
    public static Texture getTexture(String name, String basePath) {
        return getTexture(joinPath(name, basePath));
    }

    // ============================================================
    // METODOS AUXILIARES
    // ============================================================
    
    /**
     * Une un nombre de archivo con una ruta base, añadiendo la extensión .png si nos hace falta necesario
     */
    private static String joinPath(String name, String basePath) {
        String filename = name.endsWith(".png") ? name : name + ".png";
        return basePath.endsWith("/") ? basePath + filename : basePath + "/" + filename;
    }

    /**
     * Libera todos los recursos cargados por el AssetManager
     */
    public static void dispose() {
        assets.dispose();
    }

    // ============================================================
    // CLASE INTERNA ANIMATIONJOB
    // ============================================================
    
    /**
     * Clase interna que representa un trabajo de construcción de animación en cola.
     * Almacena todos los parámetros necesarios para construir una animación cuando se llame a finish().
     */
    private static class AnimationJob {
        final String path;
        final float frameWidth;
        final float frameHeight;
        final float speed;
        final float padX;
        final float padY;
        final PlayMode playMode;

        AnimationJob(String path, float frameWidth, float frameHeight,
                     float speed, float padX, float padY, PlayMode playMode) {

            this.path = path;
            this.playMode = playMode;
            this.frameWidth = frameWidth;
            this.frameHeight = frameHeight;
            this.speed = speed;
            this.padX = padX;
            this.padY = padY;
        }
    }
}