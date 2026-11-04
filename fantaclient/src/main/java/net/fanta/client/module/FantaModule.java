package net.fanta.client.module;

/**
 * Base interface for all FantaClient modules.
 */
public interface FantaModule {
    String getName();
    String getCategory();
    String getDescription();
    boolean isEnabled();
    void setEnabled(boolean enabled);
    default void toggle() {
        setEnabled(!isEnabled());
    }
    default void onEnable() {}
    default void onDisable() {}
}
