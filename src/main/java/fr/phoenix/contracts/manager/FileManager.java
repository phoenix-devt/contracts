package fr.phoenix.contracts.manager;

public interface FileManager {
    public void load();

    public void save(boolean clearBefore);
}
