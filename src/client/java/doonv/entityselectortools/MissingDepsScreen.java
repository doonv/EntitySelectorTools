package doonv.entityselectortools;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
//? if >=1.21.11
import org.jspecify.annotations.NonNull;

import java.net.URI;


public class MissingDepsScreen extends Screen {
    public static final boolean YACL_LOADED = FabricLoader.getInstance().isModLoaded("yet_another_config_lib_v3");
    public static final boolean AMECS_LOADED = FabricLoader.getInstance().isModLoaded("amecs_key_modifiers");

    private static final boolean ONE_MISSING = AMECS_LOADED || YACL_LOADED;

    public MissingDepsScreen(Component title) {
        super(title);
    }

    @Override
    protected void init() {
        assert !YACL_LOADED || !AMECS_LOADED;

        int centerX = this.width / 2 - (120 / 2);
        int y = this.height / 2 + 15;
        if (!YACL_LOADED)
            this.addRenderableWidget(Button.builder(Component.literal("Yet Another Config Lib"), (btn) -> {
                ConfirmLinkScreen.confirmLinkNow(this, URI.create("https://modrinth.com/mod/yacl"));
            }).bounds(ONE_MISSING ? centerX : centerX + 70, y, 120, 20).build());

        if (!AMECS_LOADED)
            this.addRenderableWidget(Button.builder(Component.literal("Amecs"), (btn) -> {
                ConfirmLinkScreen.confirmLinkNow(this, URI.create("https://modrinth.com/mod/amecs"));
            }).bounds(ONE_MISSING ? centerX : centerX - 70, y, 120, 20).build());

        this.addRenderableWidget(
                Button.builder(Component.translatable("entityselectortools.missingDeps.quit"), (btn) -> {
                    Minecraft.getInstance().stop();
                }).bounds(this.width / 2 - (100 / 2), this.height - 50, 100, 20).build());
    }

    //~ if >=26.1 'render' -> 'extractRenderState', 'drawCenteredString' -> 'centeredText' {
    @Override
            //~ if >=1.21.11 '/*@NonNull*/' -> '@NonNull'
    public void extractRenderState(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractRenderState(graphics, mouseX, mouseY, delta);

        int x = this.width / 2;
        int y = this.height / 2;
        graphics.centeredText(this.font, Component.translatable("entityselectortools.missingDeps.title"),
                this.width / 2, y - 60, 0xFFFFFFFF);
        graphics.centeredText(this.font, Component.translatable("entityselectortools.missingDeps.description" + (ONE_MISSING ? "Singular" : "")), x,
                y - 30,
                0xFFFFFFFF);
        graphics.centeredText(this.font, Component.translatable("entityselectortools.missingDeps.hint" + (ONE_MISSING ? "Singular" : "")), x, y - 10,
                0xAAFFFFFF);
    }
    //~ }

}
