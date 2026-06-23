package doonv.entityselectortools.config;

import com.google.gson.GsonBuilder;
import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder;
import doonv.entityselectortools.EntitySelectorTools;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.awt.*;

public class ClientConfig {
    public static ConfigClassHandler<ClientConfig> HANDLER = ConfigClassHandler.createBuilder(ClientConfig.class)
            .id(Identifier.fromNamespaceAndPath(EntitySelectorTools.MOD_ID, "config"))
            .serializer(config -> GsonConfigSerializerBuilder.create(config)
                    .setPath(FabricLoader.getInstance().getConfigDir().resolve(EntitySelectorTools.MOD_ID + ".json5"))
                    .appendGsonBuilder(GsonBuilder::setPrettyPrinting)
                    .setJson5(true)
                    .build())
            .build();

    @SerialEntry(comment = """
            Entity selector volumes used by command blocks.
            These are created client-side by reading the command block's command and looking for entity selectors.
            The command parser is text-only, and quite primitive. So it has a few limitations:
             - execute at is not supported.
             - Anything to do with rotations (^, execute rotated/facing) is not supported.""")
    public boolean commandBlockVolumesEnabled = true;

    @SerialEntry
    public Color commandBlockBoxColor = new Color(0x4DCCE6);

    @SerialEntry
    public Color commandBlockSphereMinColor = new Color(0x4D5AE6);

    @SerialEntry
    public Color commandBlockSphereMaxColor = new Color(0xAE4DE6);

    @SerialEntry(comment = """
            Entity selector volumes used by the server, mostly datapacks.
            These are dynamic and only render when the server just checked that volume.
            They are always accurate, at the cost of not showing inactive functions,
            and they also need to be sent by the server every single tick.
            They require the server to also have Entity Selector Tools installed.""")
    public boolean serverVolumesEnabled = true;

    // -----

    @SerialEntry
    public Color serverBoxColor = new Color(0xFF9900);

    @SerialEntry
    public Color serverSphereMinColor = new Color(0xC8FF00);

    @SerialEntry
    public Color serverSphereMaxColor = new Color(0x00FF04);

    @SerialEntry(comment = """
            Server volumes are sent to the client every tick by the server.
            This config changes how long to render server volumes for
            after getting them from the server in milliseconds.
            Increase this value on unstable connections.""")
    public long serverVolumeExpiryTimeMillis = 2 * (1000 / 20);

    @SerialEntry(comment = "Controls the resolution of the rendered spheres. Higher values make smoother spheres but may impact performance.")
    public int sphereSegments = 48;

    @SerialEntry(comment = "Controls the opacity of the backgrounds of rendered spheres.")
    public float sphereBackgroundTransparency = 0.2f;

    @SerialEntry(comment = "Reduces sphere detail when the sphere is far away from the camera to improve performance.")
    public boolean adaptiveSphereResolution = true;

    // ========== CREATION ==========

    @SerialEntry
    public boolean boxCreationWithWand = true;

    @SerialEntry(comment = "The item used to select the corners of a box volume.")
    public Item wandItem = Items.BREEZE_ROD;

    @SerialEntry(comment = "Registers the box creation tool as a builder tool in Axiom's 10th hotbar slot (requires Axiom and a full game restart).")
    public boolean boxCreationWithAxiomTool = false;

    @SerialEntry(comment = """
            If a block is within this distance, it will be picked;
            otherwise, a point is placed in mid-air at exactly this distance.""")
    public double airPlaceDistance = 8;

    @SerialEntry
    public Color selectionColor = new Color(0x804dff);

    /// A shortcut for `HANDLER.instance()`
    ///
    /// @return the config's working instance, used for getting/setting fields.
    public static ClientConfig get() {
        return HANDLER.instance();
    }
}
