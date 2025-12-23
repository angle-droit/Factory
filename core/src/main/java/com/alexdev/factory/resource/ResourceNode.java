package com.alexdev.factory.resource;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;

public class ResourceNode {

    public enum ResourceType {
        IRON(new Color(0.6f, 0.6f, 0.7f, 1), 50, 100, "Fer"),
        COPPER(new Color(0.8f, 0.5f, 0.3f, 1), 40, 80, "Cuivre"),
        COAL(new Color(0.2f, 0.2f, 0.2f, 1), 60, 120, "Charbon"),
        STONE(new Color(0.5f, 0.5f, 0.5f, 1), 70, 150, "Pierre"),
        OIL(new Color(0.1f, 0.1f, 0.1f, 1), 0, 0, "Pétrole"); // Infini

        public final Color color;
        public final int minAmount;
        public final int maxAmount;
        public final String displayName;

        ResourceType(Color color, int minAmount, int maxAmount, String displayName) {
            this.color = color;
            this.minAmount = minAmount;
            this.maxAmount = maxAmount;
            this.displayName = displayName;
        }
    }

    private final ResourceType type;
    private final float x, y;
    private final float size;
    private int amount;
    private final int maxAmount;
    private boolean depleted;
    private float miningProgress;
    private boolean beingMined;

    public ResourceNode(ResourceType type, float x, float y) {
        this.type = type;
        this.x = x;
        this.y = y;
        this.size = 40 + MathUtils.random(20); // Taille variable

        // Le pétrole est infini
        if (type == ResourceType.OIL) {
            this.maxAmount = Integer.MAX_VALUE;
            this.amount = Integer.MAX_VALUE;
        } else {
            this.maxAmount = MathUtils.random(type.minAmount, type.maxAmount);
            this.amount = maxAmount;
        }

        this.depleted = false;
        this.miningProgress = 0;
        this.beingMined = false;
    }

    /**
     * Mine la ressource (appelé à chaque frame quand le joueur mine)
     * @param delta temps écoulé
     * @param miningSpeed vitesse de minage (0-1)
     * @return true si on a extrait une ressource
     */
    public boolean mine(float delta, float miningSpeed) {
        if (depleted) return false;

        beingMined = true;
        miningProgress += delta * miningSpeed;

        // Il faut 1 seconde pour extraire une unité (ajustable)
        if (miningProgress >= 1.0f) {
            miningProgress = 0;
            amount--;

            if (amount <= 0 && type != ResourceType.OIL) {
                depleted = true;
            }

            return true;
        }

        return false;
    }

    /**
     * Arrête le minage (réinitialise la progression)
     */
    public void stopMining() {
        beingMined = false;
        miningProgress = 0;
    }

    /**
     * Vérifie si le joueur est assez proche pour miner
     */
    public boolean isInRange(float playerX, float playerY, float range) {
        float dx = (x + size / 2) - (playerX + 25); // 25 = moitié du joueur
        float dy = (y + size / 2) - (playerY + 25);
        float distance = (float) Math.sqrt(dx * dx + dy * dy);
        return distance <= range;
    }

    /**
     * Retourne la couleur en fonction de la quantité restante
     */
    public Color getDisplayColor() {
        if (depleted) {
            return new Color(0.3f, 0.3f, 0.3f, 0.5f);
        }

        if (type == ResourceType.OIL) {
            return type.color;
        }

        // Assombrir la couleur quand il y a moins de ressources
        float ratio = (float) amount / maxAmount;
        return new Color(
            type.color.r * (0.5f + ratio * 0.5f),
            type.color.g * (0.5f + ratio * 0.5f),
            type.color.b * (0.5f + ratio * 0.5f),
            1
        );
    }

    /**
     * Retourne la taille affichée (plus petit quand épuisé)
     */
    public float getDisplaySize() {
        if (depleted) return size * 0.5f;

        if (type == ResourceType.OIL) return size;

        float ratio = (float) amount / maxAmount;
        return size * (0.5f + ratio * 0.5f);
    }

    // Getters
    public ResourceType getType() { return type; }
    public float getX() { return x; }
    public float getY() { return y; }
    public float getSize() { return size; }
    public int getAmount() { return amount; }
    public int getMaxAmount() { return maxAmount; }
    public boolean isDepleted() { return depleted; }
    public float getMiningProgress() { return miningProgress; }
    public boolean isBeingMined() { return beingMined; }

    /**
     * Retourne un texte d'info pour l'UI
     */
    public String getInfoText() {
        if (type == ResourceType.OIL) {
            return type.displayName + " (Infini)";
        }
        return type.displayName + ": " + amount + "/" + maxAmount;
    }
}
