package doonv.entityselectortools;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;
//? if >=1.21.11
import org.jspecify.annotations.NonNull;

import java.net.URI;


public class MissingDepsScreen extends Screen {
    private static final boolean SINGLE_MISSING = EntitySelectorToolsDeps.AMECS_LOADED ^ EntitySelectorToolsDeps.YACL_LOADED;
    private final @Nullable Screen parent;

    public MissingDepsScreen(@Nullable Screen parent) {
        super(Component.translatable("entityselectortools.missingDeps.title"));

        this.parent = parent;
    }

    @Override
    protected void init() {
        assert EntitySelectorToolsDeps.ANY_MISSING;

        int centerX = this.width / 2 - (120 / 2);
        int y = this.height / 2 + 15;
        if (!EntitySelectorToolsDeps.YACL_LOADED)
            this.addRenderableWidget(Button.builder(Component.literal("Yet Another Config Lib"), (btn) -> {
                ConfirmLinkScreen.confirmLinkNow(this, URI.create("https://modrinth.com/mod/yacl"));
            }).bounds(SINGLE_MISSING ? centerX : centerX - 70, y, 120, 20).build());

        if (!EntitySelectorToolsDeps.AMECS_LOADED)
            this.addRenderableWidget(Button.builder(Component.literal("Amecs"), (btn) -> {
                ConfirmLinkScreen.confirmLinkNow(this, URI.create("https://modrinth.com/mod/amecs"));
            }).bounds(SINGLE_MISSING ? centerX : centerX + 70, y, 120, 20).build());

        this.addRenderableWidget(
                Button.builder(Component.translatable("entityselectortools.missingDeps.quit"), (btn) -> {
                    this.minecraft.stop();
                }).bounds(this.width / 2 - (100 / 2) - 55, this.height - 50, 100, 20).build());
        this.addRenderableWidget(Button.builder(
                        Component.translatable("entityselectortools.missingDeps.skip"), (btn) -> {
                            this.minecraft.gui.setScreen(parent);
                        })
                .tooltip(Tooltip.create(Component.translatable("entityselectortools.missingDeps.skipTooltip")))
                .bounds(this.width / 2 - (100 / 2) + 55, this.height - 50, 100, 20).build());
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
        graphics.centeredText(this.font, Component.translatable(
                        "entityselectortools.missingDeps.description" + (SINGLE_MISSING ? "Singular" : "")), x,
                y - 30,
                0xFFFFFFFF);
        graphics.centeredText(this.font,
                Component.translatable("entityselectortools.missingDeps.hint" + (SINGLE_MISSING ? "Singular" : "")), x,
                y - 10,
                0xAAFFFFFF);
    }
    //~ }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }
}
