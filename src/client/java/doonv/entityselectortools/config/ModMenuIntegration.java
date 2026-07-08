package doonv.entityselectortools.config;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.OptionEventListener;
import dev.isxander.yacl3.api.OptionGroup;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.api.controller.ColorControllerBuilder;
import dev.isxander.yacl3.api.controller.DoubleSliderControllerBuilder;
import dev.isxander.yacl3.api.controller.FloatSliderControllerBuilder;
import dev.isxander.yacl3.api.controller.IntegerSliderControllerBuilder;
import dev.isxander.yacl3.api.controller.ItemControllerBuilder;
import dev.isxander.yacl3.api.controller.LongFieldControllerBuilder;
import dev.isxander.yacl3.api.controller.TickBoxControllerBuilder;
import doonv.entityselectortools.compat.AxiomCompat;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;

import java.awt.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ModMenuIntegration implements ModMenuApi {
    private static ConfigCategory buildPreviewCategory(ClientConfig defaults, ClientConfig config) {
        return ConfigCategory.createBuilder()
                .name(Component.translatable("yacl3.config.entityselectortools:config.category.preview"))
                .group(buildCommandBlockGroup(defaults, config))
                .group(buildServerGroup(defaults, config))
                .group(buildSphereRenderingGroup(defaults, config))
                .build();
    }

    private static OptionGroup buildCommandBlockGroup(ClientConfig defaults, ClientConfig config) {
        Option<Boolean> master = Option.<Boolean>createBuilder()
                .name(Component.translatable("yacl3.config.entityselectortools:config.commandBlockVolumesEnabled"))
                .description(OptionDescription.of(Component.translatable(
                        "yacl3.config.entityselectortools:config.commandBlockVolumesEnabled.desc")))
                .binding(defaults.commandBlockVolumesEnabled, () -> config.commandBlockVolumesEnabled,
                        v -> config.commandBlockVolumesEnabled = v)
                .controller(TickBoxControllerBuilder::create)
                .build();

        Option<Color> boxColor = colorOption(
                "yacl3.config.entityselectortools:config.commandBlockBoxColor",
                defaults.commandBlockBoxColor,
                () -> config.commandBlockBoxColor,
                v -> config.commandBlockBoxColor = v,
                master.pendingValue(),
                null
        );

        Option<Color> sphereMinColor = colorOption(
                "yacl3.config.entityselectortools:config.commandBlockSphereMinColor",
                defaults.commandBlockSphereMinColor,
                () -> config.commandBlockSphereMinColor,
                v -> config.commandBlockSphereMinColor = v,
                master.pendingValue(),
                null
        );

        Option<Color> sphereMaxColor = colorOption(
                "yacl3.config.entityselectortools:config.commandBlockSphereMaxColor",
                defaults.commandBlockSphereMaxColor,
                () -> config.commandBlockSphereMaxColor,
                v -> config.commandBlockSphereMaxColor = v,
                master.pendingValue(),
                null
        );

        Option<Color> predicateColor = colorOption(
                "yacl3.config.entityselectortools:config.commandBlockPredicateBoxColor",
                defaults.commandBlockPredicateBoxColor,
                () -> config.commandBlockPredicateBoxColor,
                v -> config.commandBlockPredicateBoxColor = v,
                master.pendingValue(),
                "yacl3.config.entityselectortools:config.predicateBoxColor.desc"
        );

        master.addEventListener((opt, event) -> {
            if (event == OptionEventListener.Event.STATE_CHANGE) {
                boolean enabled = opt.pendingValue();
                boxColor.setAvailable(enabled);
                sphereMinColor.setAvailable(enabled);
                sphereMaxColor.setAvailable(enabled);
                predicateColor.setAvailable(enabled);
            }
        });

        return OptionGroup.createBuilder()
                .name(Component.translatable(
                        "yacl3.config.entityselectortools:config.category.preview.group.commandBlock"))
                .option(master)
                .option(boxColor)
                .option(sphereMinColor)
                .option(sphereMaxColor)
                .option(predicateColor)
                .build();
    }

    private static OptionGroup buildServerGroup(ClientConfig defaults, ClientConfig config) {
        Option<Boolean> master = Option.<Boolean>createBuilder()
                .name(Component.translatable("yacl3.config.entityselectortools:config.serverVolumesEnabled"))
                .description(OptionDescription.of(
                        Component.translatable("yacl3.config.entityselectortools:config.serverVolumesEnabled.desc")))
                .binding(defaults.serverVolumesEnabled, () -> config.serverVolumesEnabled,
                        v -> config.serverVolumesEnabled = v)
                .controller(TickBoxControllerBuilder::create)
                .build();

        Option<Color> boxColor = colorOption(
                "yacl3.config.entityselectortools:config.serverBoxColor",
                defaults.serverBoxColor,
                () -> config.serverBoxColor,
                v -> config.serverBoxColor = v,
                master.pendingValue(),
                null
        );

        Option<Color> sphereMinColor = colorOption(
                "yacl3.config.entityselectortools:config.serverSphereMinColor",
                defaults.serverSphereMinColor,
                () -> config.serverSphereMinColor,
                v -> config.serverSphereMinColor = v,
                master.pendingValue(),
                null
        );

        Option<Color> sphereMaxColor = colorOption(
                "yacl3.config.entityselectortools:config.serverSphereMaxColor",
                defaults.serverSphereMaxColor,
                () -> config.serverSphereMaxColor,
                v -> config.serverSphereMaxColor = v,
                master.pendingValue(),
                null
        );

        Option<Color> predicateColor = colorOption(
                "yacl3.config.entityselectortools:config.serverPredicateBoxColor",
                defaults.serverPredicateBoxColor,
                () -> config.serverPredicateBoxColor,
                v -> config.serverPredicateBoxColor = v,
                master.pendingValue(),
                "yacl3.config.entityselectortools:config.predicateBoxColor.desc"
        );

        Option<Long> expiryTime = Option.<Long>createBuilder()
                .name(Component.translatable("yacl3.config.entityselectortools:config.serverVolumeExpiryTimeMillis"))
                .description(OptionDescription.of(Component.translatable(
                        "yacl3.config.entityselectortools:config.serverVolumeExpiryTimeMillis.desc")))
                .binding(defaults.serverVolumeExpiryTimeMillis, () -> config.serverVolumeExpiryTimeMillis,
                        v -> config.serverVolumeExpiryTimeMillis = v)
                .controller(opt -> LongFieldControllerBuilder.create(opt).min(50L).formatValue(
                        v -> Component.literal(String.format("%dms", v))))
                .available(master.pendingValue())
                .build();

        master.addEventListener((opt, event) -> {
            if (event == OptionEventListener.Event.STATE_CHANGE) {
                boolean enabled = opt.pendingValue();
                boxColor.setAvailable(enabled);
                predicateColor.setAvailable(enabled);
                sphereMinColor.setAvailable(enabled);
                sphereMaxColor.setAvailable(enabled);
                expiryTime.setAvailable(enabled);
            }
        });

        return OptionGroup.createBuilder()
                .name(Component.translatable(
                        "yacl3.config.entityselectortools:config.category.preview.group.server"))
                .option(master)
                .option(boxColor)
                .option(sphereMinColor)
                .option(sphereMaxColor)
                .option(predicateColor)
                .option(expiryTime)
                .build();
    }

    private static OptionGroup buildSphereRenderingGroup(ClientConfig defaults, ClientConfig config) {
        Option<Integer> segments = Option.<Integer>createBuilder()
                .name(Component.translatable("yacl3.config.entityselectortools:config.sphereSegments"))
                .description(OptionDescription.of(
                        Component.translatable("yacl3.config.entityselectortools:config.sphereSegments.desc")))
                .binding(defaults.sphereSegments, () -> config.sphereSegments, v -> config.sphereSegments = v)
                .controller(opt -> IntegerSliderControllerBuilder.create(opt).range(4, 128).step(4))
                .build();

        Option<Float> transparency = Option.<Float>createBuilder()
                .name(Component.translatable("yacl3.config.entityselectortools:config.sphereBackgroundTransparency"))
                .description(OptionDescription.of(Component.translatable(
                        "yacl3.config.entityselectortools:config.sphereBackgroundTransparency.desc")))
                .binding(defaults.sphereBackgroundTransparency, () -> config.sphereBackgroundTransparency,
                        v -> config.sphereBackgroundTransparency = v)
                .controller(opt -> FloatSliderControllerBuilder.create(opt).range(0.0f, 1.0f).step(0.05f).formatValue(
                        v -> Component.literal(String.format("%.0f%%", v * 100))))
                .build();

        Option<Boolean> adaptive = Option.<Boolean>createBuilder()
                .name(Component.translatable("yacl3.config.entityselectortools:config.adaptiveSphereResolution"))
                .description(OptionDescription.of(Component.translatable(
                        "yacl3.config.entityselectortools:config.adaptiveSphereResolution.desc")))
                .binding(defaults.adaptiveSphereResolution, () -> config.adaptiveSphereResolution,
                        v -> config.adaptiveSphereResolution = v)
                .controller(TickBoxControllerBuilder::create)
                .build();

        return OptionGroup.createBuilder()
                .name(Component.translatable(
                        "yacl3.config.entityselectortools:config.category.preview.group.sphereRendering"))
                .option(segments)
                .option(transparency)
                .option(adaptive)
                .build();
    }

    private static ConfigCategory buildCreationCategory(ClientConfig defaults, ClientConfig config) {
        return ConfigCategory.createBuilder()
                .name(Component.translatable("yacl3.config.entityselectortools:config.category.creation"))
                .group(buildBoxCreationGroup(defaults, config))
                .build();
    }

    private static OptionGroup buildBoxCreationGroup(ClientConfig defaults, ClientConfig config) {
        Option<Boolean> wandMaster = Option.<Boolean>createBuilder()
                .name(Component.translatable("yacl3.config.entityselectortools:config.boxCreationWithWand"))
                .binding(defaults.boxCreationWithWand, () -> config.boxCreationWithWand,
                        v -> config.boxCreationWithWand = v)
                .controller(TickBoxControllerBuilder::create)
                .build();

        Option<Item> wandItem = Option.<Item>createBuilder()
                .name(Component.translatable("yacl3.config.entityselectortools:config.wandItem"))
                .description(OptionDescription.of(
                        Component.translatable("yacl3.config.entityselectortools:config.wandItem.desc")))
                .binding(defaults.wandItem, () -> config.wandItem, v -> config.wandItem = v)
                .controller(ItemControllerBuilder::create)
                .available(wandMaster.pendingValue())
                .build();

        Option<Boolean> axiomTool = Option.<Boolean>createBuilder()
                .name(Component.translatable("yacl3.config.entityselectortools:config.boxCreationWithAxiomTool"))
                .description(OptionDescription.of(
                        Component.translatable("yacl3.config.entityselectortools:config.boxCreationWithAxiomTool.desc")
                                .append("\n\nAxiom is currently ")
                                .append(AxiomCompat.isAxiomLoaded() ? Component.literal("loaded").withStyle(
                                        ChatFormatting.GREEN) : Component.literal("NOT loaded").withStyle(
                                        ChatFormatting.RED))
                                .append(".")
                ))
                .binding(defaults.boxCreationWithAxiomTool, () -> config.boxCreationWithAxiomTool,
                        v -> config.boxCreationWithAxiomTool = v)
                .controller(TickBoxControllerBuilder::create)
                .available(AxiomCompat.isAxiomLoaded())
                .build();

        Option<Double> airDistance = Option.<Double>createBuilder()
                .name(Component.translatable("yacl3.config.entityselectortools:config.airPlaceDistance"))
                .description(OptionDescription.of(
                        Component.translatable("yacl3.config.entityselectortools:config.airPlaceDistance.desc")))
                .binding(defaults.airPlaceDistance, () -> config.airPlaceDistance, v -> config.airPlaceDistance = v)
                .controller(opt -> DoubleSliderControllerBuilder.create(opt)
                        .range(0.0, 256.0).step(1.0)
                        .formatValue(v -> Component.literal(String.format("%.0f blocks", v))))
                .available(wandMaster.pendingValue() || axiomTool.pendingValue())
                .build();

        Option<Color> selectionColor = colorOption(
                "yacl3.config.entityselectortools:config.selectionColor",
                defaults.selectionColor,
                () -> config.selectionColor,
                v -> config.selectionColor = v,
                wandMaster.pendingValue() || axiomTool.pendingValue(),
                null
        );

        wandMaster.addEventListener((opt, event) -> {
            if (event == OptionEventListener.Event.STATE_CHANGE) {
                boolean enabled = opt.pendingValue();
                wandItem.setAvailable(enabled);
                airDistance.setAvailable(enabled || axiomTool.pendingValue());
                selectionColor.setAvailable(enabled || axiomTool.pendingValue());
            }
        });
        axiomTool.addEventListener((opt, event) -> {
            if (event == OptionEventListener.Event.STATE_CHANGE) {
                boolean enabled = opt.pendingValue();
                airDistance.setAvailable(enabled || wandMaster.pendingValue());
                selectionColor.setAvailable(enabled || wandMaster.pendingValue());
            }
        });

        return OptionGroup.createBuilder()
                .name(Component.translatable(
                        "yacl3.config.entityselectortools:config.category.creation.group.boxCreation"))
                .option(wandMaster)
                .option(wandItem)
                .option(axiomTool)
                .option(airDistance)
                .option(selectionColor)
                .build();
    }

    private static Option<Color> colorOption(
            String translationKey,
            Color def,
            Supplier<Color> getter,
            Consumer<Color> setter,
            boolean available,
            String descriptionKey
    ) {
        var builder = Option.<Color>createBuilder()
                .name(Component.translatable(translationKey))
                .binding(def, getter, setter)
                .controller(opt -> ColorControllerBuilder.create(opt).allowAlpha(true))
                .available(available);
        if (descriptionKey != null) {
            builder.description(OptionDescription.of(Component.translatable(descriptionKey)));
        }
        return builder.build();
    }

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> YetAnotherConfigLib.create(ClientConfig.HANDLER, (defaults, config, builder) ->
                builder
                        .title(Component.translatable("yacl3.config.entityselectortools:config.title"))
                        .category(buildPreviewCategory(defaults, config))
                        .category(buildCreationCategory(defaults, config))
        ).generateScreen(parent);
    }
}
