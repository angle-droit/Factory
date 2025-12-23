package com.alexdev.factory.resource;

import com.alexdev.factory.map.DevMap;
import com.badlogic.gdx.math.MathUtils;
import java.util.ArrayList;
import java.util.List;

public class ResourceManager {

    private List<ResourceNode> nodes;
    private DevMap map;
    private long seed;

    public ResourceManager(DevMap map, long seed) {
        this.nodes = new ArrayList<>();
        this.map = map;
        this.seed = seed;
    }

    /**
     * Génère des ressources dans une zone donnée
     */
    public void generateResourcesInArea(int centerX, int centerY, int radius) {
        int tileSize = 32;
        int spacing = 8; // Checker tous les X tiles au lieu de tous

        for (int x = centerX - radius; x <= centerX + radius; x += spacing) {
            for (int y = centerY - radius; y <= centerY + radius; y += spacing) {
                // Vérifier si cette position a déjà des ressources
                if (hasResourceAt(x * tileSize, y * tileSize)) continue;

                // Hash déterministe basé sur la position
                long hash = (long) x * 374761393L + (long) y * 668265263L + seed;
                float chance = ((hash & 0xFFFF) / (float) 0xFFFF);

                ResourceNode.ResourceType type = null;

                // Probabilités de spawn selon le biome
                int tileType = map.getTile(x, y);

                // 0 = herbe, 1 = route, 2 = eau, 3 = forêt, 4 = sable, 5 = pierre
                switch (tileType) {
                    case 5: // Montagne/Pierre
                        if (chance < 0.08f) type = ResourceNode.ResourceType.IRON;
                        else if (chance < 0.14f) type = ResourceNode.ResourceType.COPPER;
                        else if (chance < 0.20f) type = ResourceNode.ResourceType.STONE;
                        break;

                    case 3: // Forêt
                        if (chance < 0.06f) type = ResourceNode.ResourceType.COAL;
                        else if (chance < 0.10f) type = ResourceNode.ResourceType.COPPER;
                        break;

                    case 0: // Prairie
                        if (chance < 0.03f) type = ResourceNode.ResourceType.IRON;
                        else if (chance < 0.05f) type = ResourceNode.ResourceType.COAL;
                        break;

                    case 4: // Sable/Plage
                        if (chance < 0.10f) type = ResourceNode.ResourceType.OIL;
                        break;
                }

                // Créer le node si un type a été déterminé
                if (type != null) {
                    float worldX = x * tileSize + MathUtils.random(-15, 15);
                    float worldY = y * tileSize + MathUtils.random(-15, 15);
                    nodes.add(new ResourceNode(type, worldX, worldY));
                }
            }
        }
    }

    /**
     * Vérifie si une ressource existe déjà à cette position
     */
    private boolean hasResourceAt(float x, float y) {
        for (ResourceNode node : nodes) {
            float dx = node.getX() - x;
            float dy = node.getY() - y;
            if (dx * dx + dy * dy < 150 * 150) { // Distance minimum augmentée
                return true;
            }
        }
        return false;
    }

    /**
     * Trouve le node le plus proche du joueur dans une certaine portée
     */
    public ResourceNode findNearestNode(float playerX, float playerY, float maxRange) {
        ResourceNode nearest = null;
        float minDistance = maxRange;

        for (ResourceNode node : nodes) {
            if (node.isDepleted()) continue;

            float dx = (node.getX() + node.getSize() / 2) - (playerX + 25);
            float dy = (node.getY() + node.getSize() / 2) - (playerY + 25);
            float distance = (float) Math.sqrt(dx * dx + dy * dy);

            if (distance < minDistance) {
                minDistance = distance;
                nearest = node;
            }
        }

        return nearest;
    }

    /**
     * Retourne tous les nodes visibles dans une zone
     */
    public List<ResourceNode> getNodesInArea(float minX, float minY, float maxX, float maxY) {
        List<ResourceNode> visible = new ArrayList<>();

        for (ResourceNode node : nodes) {
            if (node.getX() + node.getSize() >= minX &&
                node.getX() <= maxX &&
                node.getY() + node.getSize() >= minY &&
                node.getY() <= maxY) {
                visible.add(node);
            }
        }

        return visible;
    }

    /**
     * Nettoie les nodes épuisés (optionnel, pour économiser la mémoire)
     */
    public void cleanupDepleted() {
        nodes.removeIf(ResourceNode::isDepleted);
    }

    /**
     * Compte le nombre de nodes par type
     */
    public int countNodesByType(ResourceNode.ResourceType type) {
        int count = 0;
        for (ResourceNode node : nodes) {
            if (node.getType() == type && !node.isDepleted()) {
                count++;
            }
        }
        return count;
    }

    public List<ResourceNode> getAllNodes() {
        return nodes;
    }

    public int getTotalNodes() {
        return nodes.size();
    }

    public int getActiveNodes() {
        int count = 0;
        for (ResourceNode node : nodes) {
            if (!node.isDepleted()) count++;
        }
        return count;
    }
}
