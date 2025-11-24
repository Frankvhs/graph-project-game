package io.game.managers;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;

/**
 * Wrapper de AssetManager para gestionar por metodos estaticos la carga de recursos
 */
public class Resources {
    private static AssetManager assets = new AssetManager();
    
    /**
     * Cargar lista de recursos actuales
     */
    public static void finish() {
    	assets.finishLoading();
    }
    
    /**
     * Cargar texturas
     */
    public static void loadTexture(String path) {
    	assets.load(path, Texture.class);
    }
    public static void loadTexture(String name, String basePath) {
    	loadTexture(basePath + '/' + name + ".png");
    }
    
    /**
     * Obtener texturas previamente cargadas y finalizada la carga
     */
    public static Texture getTexture(String path) {
        return assets.get(path, Texture.class);
    }
    public static Texture getTexture(String name, String basePath) {
    	return getTexture(basePath + '/' + name + ".png");
    }

    public static void dispose() {
        assets.dispose();
    }
}
