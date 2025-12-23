package com.alexdev.factory.map;

import com.badlogic.gdx.graphics.Color;

public class DevMap {

    private final long seed;

    // Biomes constants
    private static final int GRASS = 0;
    private static final int ROAD = 1;
    private static final int WATER = 2;
    private static final int FOREST = 3;
    private static final int SAND = 4;
    private static final int STONE = 5;

    public DevMap(long seed) {
        this.seed = seed;
    }

    /**
     * Retourne le type de tuile à la position (x, y)
     * 0 = herbe, 1 = route, 2 = eau, 3 = forêt, 4 = sable, 5 = pierre
     */
    public int getTile(int x, int y) {
        // Utilisation de plusieurs octaves de bruit pour plus de variété
        float noise = perlinNoise(x * 0.05f, y * 0.05f);
        float detailNoise = perlinNoise(x * 0.2f, y * 0.2f) * 0.3f;
        float finalNoise = noise + detailNoise;

        // Routes horizontales et verticales
        boolean isRoadX = (x % 64 == 0 || x % 64 == 1);
        boolean isRoadY = (y % 64 == 0 || y % 64 == 1);
        if (isRoadX || isRoadY) return ROAD;

        // Génération de biomes basée sur le bruit
        if (finalNoise < -0.3f) return WATER;
        if (finalNoise < -0.1f) return SAND;
        if (finalNoise < 0.2f) return GRASS;
        if (finalNoise < 0.5f) return FOREST;
        return STONE;
    }

    /**
     * Implémentation simplifiée de Perlin Noise
     */
    private float perlinNoise(float x, float y) {
        // Coordonnées de la cellule
        int x0 = (int) Math.floor(x);
        int x1 = x0 + 1;
        int y0 = (int) Math.floor(y);
        int y1 = y0 + 1;

        // Poids d'interpolation
        float sx = x - x0;
        float sy = y - y0;

        // Lissage avec une fonction ease
        sx = fade(sx);
        sy = fade(sy);

        // Gradients aux coins
        float n0 = dotGridGradient(x0, y0, x, y);
        float n1 = dotGridGradient(x1, y0, x, y);
        float ix0 = lerp(n0, n1, sx);

        n0 = dotGridGradient(x0, y1, x, y);
        n1 = dotGridGradient(x1, y1, x, y);
        float ix1 = lerp(n0, n1, sx);

        return lerp(ix0, ix1, sy);
    }

    /**
     * Calcul du produit scalaire entre gradient et vecteur distance
     */
    private float dotGridGradient(int ix, int iy, float x, float y) {
        // Gradient pseudo-aléatoire basé sur la position
        float[] gradient = getGradient(ix, iy);

        // Vecteur distance
        float dx = x - ix;
        float dy = y - iy;

        return dx * gradient[0] + dy * gradient[1];
    }

    /**
     * Génère un gradient pseudo-aléatoire pour une cellule
     */
    private float[] getGradient(int x, int y) {
        // Hash basé sur la position et le seed
        long hash = x * 374761393L + y * 668265263L + seed;
        hash = (hash ^ (hash >> 13)) * 1274126177L;

        // Angle pseudo-aléatoire
        float angle = (hash & 0xFFFF) / (float) 0xFFFF * (float) Math.PI * 2;

        return new float[]{
            (float) Math.cos(angle),
            (float) Math.sin(angle)
        };
    }

    /**
     * Fonction d'interpolation lisse
     */
    private float lerp(float a, float b, float t) {
        return a + t * (b - a);
    }

    /**
     * Fonction de lissage (ease curve)
     */
    private float fade(float t) {
        return t * t * t * (t * (t * 6 - 15) + 10);
    }

    /**
     * Retourne la couleur de la tuile pour le rendu
     */
    public Color getTileColor(int tileType) {
        switch (tileType) {
            case ROAD:   return new Color(0.4f, 0.4f, 0.4f, 1);      // route grise
            case WATER:  return new Color(0.2f, 0.4f, 0.8f, 1);      // eau bleue
            case FOREST: return new Color(0.1f, 0.5f, 0.2f, 1);      // forêt vert foncé
            case SAND:   return new Color(0.9f, 0.8f, 0.5f, 1);      // sable beige
            case STONE:  return new Color(0.5f, 0.5f, 0.5f, 1);      // pierre gris clair
            default:     return new Color(0.3f, 0.7f, 0.3f, 1);      // herbe vert
        }
    }

    /**
     * Vérifie si une tuile est traversable par le joueur
     */
    public boolean isWalkable(int tileType) {
        return tileType != WATER; // Tout sauf l'eau
    }

    /**
     * Retourne le nom du biome
     */
    public String getBiomeName(int tileType) {
        switch (tileType) {
            case ROAD:   return "Route";
            case WATER:  return "Eau";
            case FOREST: return "Forêt";
            case SAND:   return "Plage";
            case STONE:  return "Montagne";
            default:     return "Prairie";
        }
    }
}
