package com.alexdev.factory.inventory;

import java.util.ArrayList;
import java.util.List;

public class Inventory {

    private final Item[] slots;

    public Inventory(int size) {
        slots = new Item[size];
    }

    public Item get(int index) {
        if (index < 0 || index >= slots.length) return null;
        return slots[index];
    }

    public void set(int index, Item item) {
        if (index >= 0 && index < slots.length) {
            slots[index] = item;
        }
    }

    /**
     * Ajoute un item à l'inventaire
     * Essaie d'abord de stacker avec des items existants
     */
    public boolean add(Item item) {
        if (item == null) return false;

        // Si l'item peut être stacké, chercher un slot avec le même item
        if (item.canStack()) {
            for (Item existingItem : slots) {
                if (existingItem != null && existingItem.getId().equals(item.getId())) {
                    int transferred = existingItem.mergeWith(item);
                    if (item.isEmpty()) {
                        return true; // Tout a été transféré
                    }
                }
            }
        }

        // Si l'item n'est pas vide, trouver un slot vide
        if (!item.isEmpty()) {
            for (int i = 0; i < slots.length; i++) {
                if (slots[i] == null) {
                    slots[i] = item;
                    return true;
                }
            }
        }

        return false; // Inventaire plein
    }

    /**
     * Ajoute plusieurs items du même type
     */
    public boolean addMultiple(String id, String name, int quantity) {
        Item item = new Item.Builder(id, name)
            .quantity(quantity)
            .build();
        return add(item);
    }

    /**
     * Retire un item à un index spécifique
     */
    public Item remove(int index) {
        if (index < 0 || index >= slots.length) return null;

        Item item = slots[index];
        slots[index] = null;
        return item;
    }

    /**
     * Retire une certaine quantité d'un item spécifique
     */
    public boolean removeById(String itemId, int quantity) {
        int remaining = quantity;

        for (int i = 0; i < slots.length && remaining > 0; i++) {
            Item item = slots[i];
            if (item != null && item.getId().equals(itemId)) {
                int toRemove = Math.min(remaining, item.getQuantity());
                item.removeQuantity(toRemove);
                remaining -= toRemove;

                if (item.isEmpty()) {
                    slots[i] = null;
                }
            }
        }

        return remaining == 0; // True si on a tout retiré
    }

    /**
     * Échange deux slots
     */
    public void swap(int index1, int index2) {
        if (index1 < 0 || index1 >= slots.length ||
            index2 < 0 || index2 >= slots.length) {
            return;
        }

        Item temp = slots[index1];
        slots[index1] = slots[index2];
        slots[index2] = temp;
    }

    /**
     * Compte le nombre d'items dans l'inventaire
     */
    public int getItemCount() {
        int count = 0;
        for (Item item : slots) {
            if (item != null) {
                count++;
            }
        }
        return count;
    }

    /**
     * Compte la quantité totale d'un item spécifique
     */
    public int countItem(String itemId) {
        int total = 0;
        for (Item item : slots) {
            if (item != null && item.getId().equals(itemId)) {
                total += item.getQuantity();
            }
        }
        return total;
    }

    /**
     * Vérifie si l'inventaire contient un item
     */
    public boolean hasItem(String itemId) {
        return countItem(itemId) > 0;
    }

    /**
     * Vérifie si l'inventaire contient une quantité minimale d'un item
     */
    public boolean hasItem(String itemId, int minQuantity) {
        return countItem(itemId) >= minQuantity;
    }

    /**
     * Trouve le premier slot contenant un item spécifique
     */
    public int findItem(String itemId) {
        for (int i = 0; i < slots.length; i++) {
            if (slots[i] != null && slots[i].getId().equals(itemId)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Trouve tous les slots contenant un item spécifique
     */
    public List<Integer> findAllItems(String itemId) {
        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < slots.length; i++) {
            if (slots[i] != null && slots[i].getId().equals(itemId)) {
                indices.add(i);
            }
        }
        return indices;
    }

    /**
     * Retourne tous les items non-null
     */
    public List<Item> getAllItems() {
        List<Item> items = new ArrayList<>();
        for (Item item : slots) {
            if (item != null) {
                items.add(item);
            }
        }
        return items;
    }

    /**
     * Vérifie si l'inventaire est plein
     */
    public boolean isFull() {
        for (Item item : slots) {
            if (item == null) return false;
        }
        return true;
    }

    /**
     * Vérifie si l'inventaire est vide
     */
    public boolean isEmpty() {
        for (Item item : slots) {
            if (item != null) return true;
        }
        return false;
    }

    /**
     * Vide complètement l'inventaire
     */
    public void clear() {
        for (int i = 0; i < slots.length; i++) {
            slots[i] = null;
        }
    }

    /**
     * Taille de l'inventaire
     */
    public int size() {
        return slots.length;
    }

    /**
     * Compte les slots vides
     */
    public int getEmptySlots() {
        return size() - getItemCount();
    }

    /**
     * Trie l'inventaire (items similaires ensemble, vides à la fin)
     */
    public void sort() {
        // Bubble sort simple - compacter les items
        for (int i = 0; i < slots.length - 1; i++) {
            for (int j = 0; j < slots.length - i - 1; j++) {
                if (slots[j] == null && slots[j + 1] != null) {
                    swap(j, j + 1);
                }
            }
        }

        // Grouper les items similaires
        for (int i = 0; i < slots.length - 1; i++) {
            if (slots[i] == null) break;

            for (int j = i + 1; j < slots.length; j++) {
                if (slots[j] != null &&
                    slots[i].getId().equals(slots[j].getId()) &&
                    slots[i].canStack()) {

                    slots[i].mergeWith(slots[j]);
                    if (slots[j].isEmpty()) {
                        slots[j] = null;
                    }
                }
            }
        }

        // Recompacter après fusion
        for (int i = 0; i < slots.length - 1; i++) {
            for (int j = 0; j < slots.length - i - 1; j++) {
                if (slots[j] == null && slots[j + 1] != null) {
                    swap(j, j + 1);
                }
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Inventory [");
        sb.append(getItemCount()).append("/").append(size()).append("]:\n");
        for (int i = 0; i < slots.length; i++) {
            if (slots[i] != null) {
                sb.append("  [").append(i).append("] ").append(slots[i]).append("\n");
            }
        }
        return sb.toString();
    }
}
