package com.alexdev.factory;

import com.alexdev.factory.input.MouseScrollHandler;
import com.alexdev.factory.inventory.Inventory;
import com.alexdev.factory.inventory.Item;
import com.alexdev.factory.inventory.ui.InventoryUI;
import com.alexdev.factory.map.DevMap;
import com.alexdev.factory.resource.ResourceManager;
import com.alexdev.factory.resource.ResourceNode;
import com.alexdev.factory.ui.Hud;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Main extends ApplicationAdapter {
    private ShapeRenderer shapeRenderer;
    private OrthographicCamera camera;
    private DevMap mapGenerator;
    private ResourceManager resourceManager;
    private Stage stage;
    private Hud hud;
    private Inventory inventory;
    private InventoryUI inventoryUI;
    private Skin skin;

    private float playerX, playerY;
    private float playerSize = 50;
    private float speed = 200;
    private float miningRange = 100f;
    private float miningSpeed = 1.5f;

    private ResourceNode currentMiningNode;
    private int lastPlayerTileX = 0;
    private int lastPlayerTileY = 0;

    private static class Collectible {
        float x, y;
        float size = 30;

        Collectible(float x, float y) {
            this.x = x;
            this.y = y;
        }
    }

    private ArrayList<Collectible> collectibles;
    private Random random;
    private int score;

    @Override
    public void create() {
        shapeRenderer = new ShapeRenderer();
        mapGenerator = new DevMap(21343124L);
        resourceManager = new ResourceManager(mapGenerator, 21343124L);

        stage = new Stage(new ScreenViewport());
        hud = new Hud();

        camera = new OrthographicCamera();
        camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.zoom = 1.0f;

        playerX = Gdx.graphics.getWidth() / 2f;
        playerY = Gdx.graphics.getHeight() / 2f;

        collectibles = new ArrayList<>();
        random = new Random();
        score = 0;

        skin = new Skin(Gdx.files.internal("ui/uiskin.json"));

        // Créer l'inventaire avec quelques items de test
        inventory = new Inventory(30);
        inventory.add(Item.createPotion());
        inventory.add(Item.createSword());

        // Créer l'UI de l'inventaire
        inventoryUI = new InventoryUI(inventory, skin);
        stage.addActor(inventoryUI);
        inventoryUI.centerOnScreen();
        inventoryUI.setVisible(false);

        // Générer les ressources autour du joueur au démarrage
        int tileSize = 32;
        int playerTileX = (int)(playerX / tileSize);
        int playerTileY = (int)(playerY / tileSize);
        resourceManager.generateResourcesInArea(playerTileX, playerTileY, 50);
        lastPlayerTileX = playerTileX;
        lastPlayerTileY = playerTileY;

        // Configuration de l'input - ORDRE IMPORTANT!
        InputMultiplexer multiplexer = new InputMultiplexer();
        multiplexer.addProcessor(stage); // Stage EN PREMIER pour l'UI
        multiplexer.addProcessor(new MouseScrollHandler(camera));
        Gdx.input.setInputProcessor(multiplexer);
    }

    @Override
    public void render() {
        float delta = Gdx.graphics.getDeltaTime();

        handleInput(delta);
        updateResourceGeneration();
        updateMining(delta);
        updateCamera();
        renderScene();

        // Rendu du Stage (UI)
        stage.act(delta);
        stage.draw();
    }

    private void handleInput(float delta) {
        // Toggle inventaire avec I ou ESC
        if (Gdx.input.isKeyJustPressed(Input.Keys.I) ||
            Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            inventoryUI.toggle();
        }

        // Bloquer le mouvement du joueur quand l'inventaire est ouvert
        if (inventoryUI.isVisible()) {
            return; // Ne pas traiter les mouvements
        }

        float moveSpeed = speed * delta;

        // Déplacement avec les flèches
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT))  playerX -= moveSpeed;
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) playerX += moveSpeed;
        if (Gdx.input.isKeyPressed(Input.Keys.UP))    playerY += moveSpeed;
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN))  playerY -= moveSpeed;

        // Alternative WASD
        if (Gdx.input.isKeyPressed(Input.Keys.A)) playerX -= moveSpeed;
        if (Gdx.input.isKeyPressed(Input.Keys.D)) playerX += moveSpeed;
        if (Gdx.input.isKeyPressed(Input.Keys.W)) playerY += moveSpeed;
        if (Gdx.input.isKeyPressed(Input.Keys.S)) playerY -= moveSpeed;
    }

    /**
     * Génère de nouvelles ressources quand le joueur se déplace
     */
    private void updateResourceGeneration() {
        int tileSize = 32;
        int playerTileX = (int)(playerX / tileSize);
        int playerTileY = (int)(playerY / tileSize);

        // Générer de nouvelles ressources si le joueur s'est déplacé assez loin
        int dx = Math.abs(playerTileX - lastPlayerTileX);
        int dy = Math.abs(playerTileY - lastPlayerTileY);

        if (dx > 20 || dy > 20) {
            resourceManager.generateResourcesInArea(playerTileX, playerTileY, 50);
            lastPlayerTileX = playerTileX;
            lastPlayerTileY = playerTileY;
        }
    }

    /**
     * Gère le minage des ressources
     */
    private void updateMining(float delta) {
        // Arrêter le minage du node précédent si on ne mine plus
        if (currentMiningNode != null && !Gdx.input.isKeyPressed(Input.Keys.SPACE)) {
            currentMiningNode.stopMining();
            currentMiningNode = null;
        }

        // Commencer à miner si on appuie sur ESPACE
        if (Gdx.input.isKeyPressed(Input.Keys.SPACE) && !inventoryUI.isVisible()) {
            if (currentMiningNode == null) {
                currentMiningNode = resourceManager.findNearestNode(playerX, playerY, miningRange);
            }

            if (currentMiningNode != null) {
                boolean extracted = currentMiningNode.mine(delta, miningSpeed);

                if (extracted) {
                    // Ajouter la ressource à l'inventaire
                    String itemId = currentMiningNode.getType().name().toLowerCase();
                    String itemName = currentMiningNode.getType().displayName;

                    Item resource = new Item.Builder(itemId, itemName)
                        .type("material")
                        .quantity(1)
                        .maxStack(99)
                        .build();

                    inventory.add(resource);
                    System.out.println("Extrait: " + itemName);
                }

                // Si le node est épuisé, on arrête
                if (currentMiningNode.isDepleted()) {
                    currentMiningNode = null;
                }
            }
        }
    }

    private void updateCamera() {
        // Centrer la caméra sur le joueur
        camera.position.set(
            playerX + playerSize / 2,
            playerY + playerSize / 2,
            0
        );
        camera.update();
    }

    private void renderScene() {
        // Nettoyage de l'écran
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.15f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Gestion du redimensionnement / fullscreen
        if (Gdx.graphics.getWidth() != camera.viewportWidth ||
            Gdx.graphics.getHeight() != camera.viewportHeight) {
            camera.viewportWidth = Gdx.graphics.getWidth();
            camera.viewportHeight = Gdx.graphics.getHeight();
            camera.update();
        }

        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        renderMap();
        renderResources();
        renderPlayer();
        renderCollectibles();

        shapeRenderer.end();
    }

    private void renderMap() {
        int tileSize = 32;
        int viewRadius = 100;
        int playerTileX = (int)(playerX / tileSize);
        int playerTileY = (int)(playerY / tileSize);

        for (int x = playerTileX - viewRadius; x <= playerTileX + viewRadius; x++) {
            for (int y = playerTileY - viewRadius; y <= playerTileY + viewRadius; y++) {
                int type = mapGenerator.getTile(x, y);
                shapeRenderer.setColor(mapGenerator.getTileColor(type));
                shapeRenderer.rect(x * tileSize, y * tileSize, tileSize, tileSize);
            }
        }
    }

    private void renderPlayer() {
        shapeRenderer.setColor(Color.RED);
        shapeRenderer.rect(playerX, playerY, playerSize, playerSize);

        // Bordure pour mieux voir le joueur
        shapeRenderer.end();
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.rect(playerX, playerY, playerSize, playerSize);

        // Cercle de portée de minage si on mine
        if (currentMiningNode != null) {
            shapeRenderer.setColor(1, 1, 0, 0.3f);
            shapeRenderer.circle(playerX + playerSize / 2, playerY + playerSize / 2, miningRange, 30);
        }

        shapeRenderer.end();
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
    }

    /**
     * Dessine les ressources visibles
     */
    private void renderResources() {
        // Calculer la zone visible
        float camX = camera.position.x;
        float camY = camera.position.y;
        float camWidth = camera.viewportWidth * camera.zoom;
        float camHeight = camera.viewportHeight * camera.zoom;

        List<ResourceNode> visibleNodes = resourceManager.getNodesInArea(
            camX - camWidth / 2, camY - camHeight / 2,
            camX + camWidth / 2, camY + camHeight / 2
        );

        for (ResourceNode node : visibleNodes) {
            Color color = node.getDisplayColor();
            shapeRenderer.setColor(color);

            float size = node.getDisplaySize();
            float x = node.getX() + (node.getSize() - size) / 2;
            float y = node.getY() + (node.getSize() - size) / 2;

            // Dessiner le node comme un cercle
            shapeRenderer.circle(x + size / 2, y + size / 2, size / 2, 16);

            // Bordure plus foncée
            shapeRenderer.end();
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            shapeRenderer.setColor(color.r * 0.5f, color.g * 0.5f, color.b * 0.5f, 1);
            shapeRenderer.circle(x + size / 2, y + size / 2, size / 2, 16);
            shapeRenderer.end();
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

            // Barre de progression si en cours de minage
            if (node.isBeingMined()) {
                float progress = node.getMiningProgress();
                shapeRenderer.setColor(0, 0, 0, 0.7f);
                shapeRenderer.rect(x, y + size + 5, size, 6);
                shapeRenderer.setColor(0, 1, 0, 1);
                shapeRenderer.rect(x + 1, y + size + 6, (size - 2) * progress, 4);
            }
        }
    }

    private void renderCollectibles() {
        shapeRenderer.setColor(Color.YELLOW);
        for (Collectible c : collectibles) {
            shapeRenderer.rect(c.x, c.y, c.size, c.size);
        }
    }

    @Override
    public void dispose() {
        if (shapeRenderer != null) shapeRenderer.dispose();
        if (stage != null) stage.dispose();
        if (skin != null) skin.dispose();
    }

    @Override
    public void resize(int width, int height) {
        // Mise à jour du viewport quand la fenêtre change de taille
        camera.viewportWidth = width;
        camera.viewportHeight = height;
        camera.update();

        // Mise à jour du Stage et recentrage de l'inventaire
        stage.getViewport().update(width, height, true);
        inventoryUI.centerOnScreen();
    }

    // Getter pour le score (si besoin)
    public int getScore() {
        return score;
    }
}
