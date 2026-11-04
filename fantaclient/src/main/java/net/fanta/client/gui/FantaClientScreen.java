package net.fanta.client.gui;

import net.fanta.client.ConfigManager;
import net.fanta.client.module.*;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Click;
import net.minecraft.client.input.KeyInput;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

/**
 * Compact tabbed GUI – fits 854x480 and scales up.
 * Right Shift to open, ESC / Right Shift to close.
 */
public class FantaClientScreen extends Screen {
    private static final int PANEL_W_MAX = 420;
    private static final int PANEL_H_MAX = 300;
    private static final int HEADER = 26;
    private static final int TAB_H = 18;
    private static final int ROW_H = 20;
    private static final int PAD = 10;

    private static final int BG = 0xF0101018;
    private static final int HEADER_BG = 0xFF0A0A12;
    private static final int ACCENT = 0xFF00E5C0;
    private static final int TAB_BG = 0xFF1A1A24;
    private static final int TAB_ON = 0xFF1A3A30;
    private static final int ROW_HOVER = 0xFF1C1C2A;
    private static final int TEXT = 0xFFE8E8F0;
    private static final int TEXT_DIM = 0xFF8888A0;

    private final List<String> tabs = new ArrayList<>();
    private int activeTab = 0;
    private float openAnim = 0f;

    public FantaClientScreen() {
        super(Text.literal("FantaClient"));
        tabs.add("Movement");
        tabs.add("Render");
        tabs.add("Utility");
        tabs.add("Fun");
        tabs.add("Accessories");
        tabs.add("Settings");
    }

    @Override
    protected void init() {
        openAnim = 0f;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        openAnim = Math.min(1f, openAnim + delta * 0.15f);
        float a = easeOutCubic(openAnim);

        int panelW = Math.min(PANEL_W_MAX, Math.max(300, this.width - 40));
        int panelH = Math.min(PANEL_H_MAX, Math.max(220, this.height - 40));
        int panelX = (this.width - panelW) / 2;
        int panelY = (int) ((this.height - panelH) / 2 * a);

        // Dim backdrop
        context.fill(0, 0, this.width, this.height, (int) (0x90 * a) << 24);

        // Panel
        context.fill(panelX, panelY, panelX + panelW, panelY + panelH, BG);
        context.fill(panelX, panelY, panelX + panelW, panelY + 2, ACCENT);
        context.fill(panelX, panelY + 2, panelX + panelW, panelY + HEADER, HEADER_BG);

        context.drawText(this.textRenderer, "FantaClient", panelX + PAD, panelY + 9, ACCENT, false);
        String close = "ESC";
        context.drawText(this.textRenderer, close,
                panelX + panelW - PAD - this.textRenderer.getWidth(close), panelY + 9, TEXT_DIM, false);

        // Tabs
        int tabY = panelY + HEADER + 4;
        int tabX = panelX + PAD;
        int tabW = Math.max(48, (panelW - PAD * 2 - 4 * (tabs.size() - 1)) / tabs.size());
        for (int i = 0; i < tabs.size(); i++) {
            boolean on = i == activeTab;
            context.fill(tabX, tabY, tabX + tabW, tabY + TAB_H, on ? TAB_ON : TAB_BG);
            String label = tabs.get(i);
            if (this.textRenderer.getWidth(label) > tabW - 4) {
                label = label.substring(0, Math.min(3, label.length()));
            }
            int tw = this.textRenderer.getWidth(label);
            context.drawText(this.textRenderer, label, tabX + (tabW - tw) / 2, tabY + 5, on ? ACCENT : TEXT_DIM, false);
            tabX += tabW + 4;
        }

        // Content
        int contentY = tabY + TAB_H + 6;
        int contentH = panelY + panelH - contentY - 18;
        String tab = tabs.get(activeTab);

        if (tab.equals("Settings")) {
            renderSettings(context, panelX + PAD, contentY, panelW - PAD * 2, contentH, mouseX, mouseY);
        } else {
            List<FantaModule> modules = ModuleManager.getByCategory(tab);
            int rowY = contentY;
            for (FantaModule mod : modules) {
                if (rowY + ROW_H > contentY + contentH) break;
                boolean hover = mouseX >= panelX + PAD && mouseX <= panelX + panelW - PAD
                        && mouseY >= rowY && mouseY <= rowY + ROW_H;
                if (hover) {
                    context.fill(panelX + PAD, rowY, panelX + panelW - PAD, rowY + ROW_H, ROW_HOVER);
                }
                int nameColor = mod.isEnabled() ? TEXT : TEXT_DIM;
                context.drawText(this.textRenderer, mod.getName(), panelX + PAD + 6, rowY + 6, nameColor, false);
                drawToggle(context, panelX + panelW - PAD - 24, rowY + 4, mod.isEnabled());
                rowY += ROW_H;
            }
        }

        // Footer
        String footer = ConfigManager.getLastMessage();
        if (footer == null || footer.isEmpty()) {
            footer = "Profile: " + ConfigManager.getActiveProfile();
        }
        if (footer.length() > 40) footer = footer.substring(0, 40) + "..";
        context.drawText(this.textRenderer, footer, panelX + PAD, panelY + panelH - 14, TEXT_DIM, false);
    }

    private void renderSettings(DrawContext context, int x, int y, int w, int h, int mouseX, int mouseY) {
        int yy = y;
        context.drawText(this.textRenderer, "Block ESP target", x, yy, TEXT_DIM, false);
        yy += 12;
        String[] labels = {"Diamond", "Debris", "Chest", "Spawner"};
        Block[] blocks = {Blocks.DIAMOND_ORE, Blocks.ANCIENT_DEBRIS, Blocks.CHEST, Blocks.SPAWNER};
        int bx = x;
        for (int i = 0; i < labels.length; i++) {
            boolean on = BlockEspModule.INSTANCE.getSelectedBlock() == blocks[i];
            int bw = 70;
            context.fill(bx, yy, bx + bw, yy + 14, on ? TAB_ON : TAB_BG);
            context.drawText(this.textRenderer, labels[i], bx + 4, yy + 3, TEXT, false);
            bx += bw + 4;
        }
        yy += 22;

        context.drawText(this.textRenderer, "Config / Share", x, yy, TEXT_DIM, false);
        yy += 12;
        context.drawText(this.textRenderer, "Active: " + ConfigManager.getActiveProfile(), x, yy, ACCENT, false);
        yy += 14;

        String[] actions = {"Save Profile", "Next Profile", "Export Share", "Import Next", "Reset All"};
        for (String action : actions) {
            if (yy + 16 > y + h) break;
            context.fill(x, yy, x + w, yy + 15, TAB_BG);
            context.drawText(this.textRenderer, action, x + 6, yy + 4, TEXT, false);
            yy += 17;
        }
    }

    private void drawToggle(DrawContext context, int x, int y, boolean on) {
        context.fill(x, y, x + 18, y + 10, on ? 0xFF00A888 : 0xFF333344);
        int knob = on ? x + 9 : x + 1;
        context.fill(knob, y + 1, knob + 8, y + 9, 0xFFE8E8F0);
    }

    private float easeOutCubic(float t) {
        return 1f - (float) Math.pow(1 - t, 3);
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        double mouseX = click.x();
        double mouseY = click.y();
        int button = click.button();
        if (button != 0) return super.mouseClicked(click, doubled);

        int panelW = Math.min(PANEL_W_MAX, Math.max(300, this.width - 40));
        int panelH = Math.min(PANEL_H_MAX, Math.max(220, this.height - 40));
        int panelX = (this.width - panelW) / 2;
        int panelY = (this.height - panelH) / 2;

        // Tabs
        int tabY = panelY + HEADER + 4;
        int tabX = panelX + PAD;
        int tabW = Math.max(48, (panelW - PAD * 2 - 4 * (tabs.size() - 1)) / tabs.size());
        for (int i = 0; i < tabs.size(); i++) {
            if (mouseX >= tabX && mouseX <= tabX + tabW && mouseY >= tabY && mouseY <= tabY + TAB_H) {
                activeTab = i;
                return true;
            }
            tabX += tabW + 4;
        }

        int contentY = tabY + TAB_H + 6;
        int contentH = panelY + panelH - contentY - 18;
        String tab = tabs.get(activeTab);

        if (tab.equals("Settings")) {
            int yy = contentY + 12;
            String[] labels = {"Diamond", "Debris", "Chest", "Spawner"};
            Block[] blocks = {Blocks.DIAMOND_ORE, Blocks.ANCIENT_DEBRIS, Blocks.CHEST, Blocks.SPAWNER};
            int bx = panelX + PAD;
            for (int i = 0; i < labels.length; i++) {
                int bw = 70;
                if (mouseX >= bx && mouseX <= bx + bw && mouseY >= yy && mouseY <= yy + 14) {
                    BlockEspModule.INSTANCE.setSelectedBlock(blocks[i]);
                    ConfigManager.save();
                    return true;
                }
                bx += bw + 4;
            }
            yy += 22 + 12 + 14;
            String[] actions = {"Save Profile", "Next Profile", "Export Share", "Import Next", "Reset All"};
            for (int i = 0; i < actions.length; i++) {
                if (mouseX >= panelX + PAD && mouseX <= panelX + panelW - PAD && mouseY >= yy && mouseY <= yy + 15) {
                    switch (i) {
                        case 0 -> {
                            String name = ConfigManager.getActiveProfile();
                            if ("default".equals(name)) name = "main";
                            ConfigManager.saveProfile(name);
                        }
                        case 1 -> ConfigManager.cycleProfile(1);
                        case 2 -> ConfigManager.exportShare();
                        case 3 -> {
                            var files = ConfigManager.listShareFiles();
                            if (!files.isEmpty()) ConfigManager.importShare(files.get(0));
                            else {
                                var profiles = ConfigManager.listProfiles();
                                if (!profiles.isEmpty()) ConfigManager.loadProfile(profiles.get(0));
                            }
                        }
                        case 4 -> ConfigManager.resetDefaults();
                    }
                    return true;
                }
                yy += 17;
            }
            return true;
        }

        List<FantaModule> modules = ModuleManager.getByCategory(tab);
        int rowY = contentY;
        for (FantaModule mod : modules) {
            if (rowY + ROW_H > contentY + contentH) break;
            if (mouseX >= panelX + PAD && mouseX <= panelX + panelW - PAD
                    && mouseY >= rowY && mouseY <= rowY + ROW_H) {
                mod.toggle();
                ConfigManager.save();
                return true;
            }
            rowY += ROW_H;
        }
        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        int keyCode = input.key();
        if (keyCode == GLFW.GLFW_KEY_ESCAPE || keyCode == GLFW.GLFW_KEY_RIGHT_SHIFT) {
            this.close();
            return true;
        }
        // Number keys 1-6 switch tabs
        if (keyCode >= GLFW.GLFW_KEY_1 && keyCode <= GLFW.GLFW_KEY_6) {
            int idx = keyCode - GLFW.GLFW_KEY_1;
            if (idx < tabs.size()) activeTab = idx;
            return true;
        }
        return super.keyPressed(input);
    }

    @Override
    public void close() {
        try { ConfigManager.save(); } catch (Throwable ignored) {}
        super.close();
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
