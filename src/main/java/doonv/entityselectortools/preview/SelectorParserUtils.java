package doonv.entityselectortools.preview;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.advancements.predicates.MinMaxBounds;
import net.minecraft.commands.arguments.selector.EntitySelectorParser;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SelectorParserUtils {
    public static List<EntitySelectorVolume> parseSelectors(String command, BlockPos pos) {
        // rip BlockPos::getCenter 🥀 idk why moyang removed you, you were useful
        return parseSelectors(command, Vec3.atCenterOf(pos));
    }

    public static List<VolumeTemplate> parseSelectors(String command) {
        List<VolumeTemplate> volumes = new ArrayList<>();
        StringReader reader = new StringReader(command);
        boolean atWordStart = true;

        VolumeTemplate.Axis[] defaults = {
                new VolumeTemplate.Axis(true, false, 0),
                new VolumeTemplate.Axis(true, false, 0),
                new VolumeTemplate.Axis(true, false, 0)
        };

        while (reader.canRead()) {
            char c = reader.peek();

            if (c == '"' || c == '\'') {
                skipQuotedString(reader);
                atWordStart = false;
                continue;
            }

            if (c == '@') {
                int cursorAtStartOfSelector = reader.getCursor();

                try {
                    EntitySelectorParser sr = new EntitySelectorParser(reader, true);
                    sr.parse();

                    VolumeTemplate.Axis x = sr.getX() == null ? defaults[0] : new VolumeTemplate.Axis(false, false,
                            sr.getX());
                    VolumeTemplate.Axis y = sr.getY() == null ? defaults[1] : new VolumeTemplate.Axis(false, false,
                            sr.getY());
                    VolumeTemplate.Axis z = sr.getZ() == null ? defaults[2] : new VolumeTemplate.Axis(false, false,
                            sr.getZ());

                    Optional<Vec3> boxDelta = getBoxDelta(sr);

                    var distance = sr.getDistance();

                    volumes.add(new VolumeTemplate(x, y, z, boxDelta, Optional.ofNullable(distance)
                            .map(MinMaxBounds.Doubles::bounds).orElse(MinMaxBounds.Doubles.ANY.bounds())));

                } catch (CommandSyntaxException e) {
                    reader.setCursor(cursorAtStartOfSelector + 1);
                }
                atWordStart = false;
                continue;
            }

            if (atWordStart && Character.isLetter(c)) {
                String word = readWord(reader);

                if (word.equalsIgnoreCase("positioned")) {
                    VolumeTemplate.Axis[] axes = parsePosition(reader);
                    if (axes != null) {
                        defaults[0] = new VolumeTemplate.Axis(axes[0].isRelative(), false, axes[0].value());
                        defaults[1] = new VolumeTemplate.Axis(axes[1].isRelative(), false, axes[1].value());
                        defaults[2] = new VolumeTemplate.Axis(axes[2].isRelative(), false, axes[2].value());
                    }
                } else if (word.equalsIgnoreCase("align")) {
                    String axisStr = readWord(reader);
                    if (axisStr.matches("^[xyz]{1,3}$")) {
                        if (axisStr.indexOf('x') >= 0)
                            defaults[0] = new VolumeTemplate.Axis(defaults[0].isRelative(), true, defaults[0].value());
                        if (axisStr.indexOf('y') >= 0)
                            defaults[1] = new VolumeTemplate.Axis(defaults[1].isRelative(), true, defaults[1].value());
                        if (axisStr.indexOf('z') >= 0)
                            defaults[2] = new VolumeTemplate.Axis(defaults[2].isRelative(), true, defaults[2].value());
                    }
                }

                atWordStart = false;
                continue;
            }

            atWordStart = Character.isWhitespace(c);
            reader.skip();
        }
        return volumes;
    }

    private static Optional<Vec3> getBoxDelta(EntitySelectorParser sr) {
        Optional<Vec3> boxDelta;
        if (sr.getDeltaX() == null && sr.getDeltaY() == null && sr.getDeltaZ() == null) {
            return Optional.empty();
        } else {
            return Optional.of(new Vec3(
                    Optional.ofNullable(sr.getDeltaX()).orElse(0.0) + 1,
                    Optional.ofNullable(sr.getDeltaY()).orElse(0.0) + 1,
                    Optional.ofNullable(sr.getDeltaZ()).orElse(0.0) + 1
            ));
        }
    }

    public static List<EntitySelectorVolume> parseSelectors(String command, Vec3 pos) {
        return parseSelectors(command).stream()
                .map(template -> template.evaluate(pos))
                .toList();
    }

    private static void skipQuotedString(StringReader reader) {
        char quote = reader.read();
        boolean escaped = false;
        while (reader.canRead()) {
            char c = reader.read();
            if (escaped) {
                escaped = false;
            } else if (c == '\\') {
                escaped = true;
            } else if (c == quote) {
                return;
            }
        }
    }

    private static String readWord(StringReader reader) {
        skipWhitespace(reader);
        int start = reader.getCursor();
        while (reader.canRead() && !Character.isWhitespace(reader.peek())) {
            reader.skip();
        }
        return reader.getString().substring(start, reader.getCursor());
    }

    private static void skipWhitespace(StringReader reader) {
        while (reader.canRead() && Character.isWhitespace(reader.peek())) {
            reader.skip();
        }
    }

    private static VolumeTemplate.Axis[] parsePosition(StringReader reader) {
        int saved = reader.getCursor();
        VolumeTemplate.Axis[] axes = new VolumeTemplate.Axis[3];

        for (int i = 0; i < 3; i++) {
            while (reader.canRead() && Character.isWhitespace(reader.peek())) reader.skip();
            if (!reader.canRead()) {
                reader.setCursor(saved);
                return null;
            }

            if (reader.peek() == '^') {
                reader.setCursor(saved);
                return null;
            }

            VolumeTemplate.Axis axis = parseCoordinate(reader);
            if (axis == null) {
                reader.setCursor(saved);
                return null;
            }
            axes[i] = axis;
        }

        return axes;
    }

    private static VolumeTemplate.Axis parseCoordinate(StringReader reader) {
        if (reader.peek() == '~') {
            reader.skip();
            int beforeOffset = reader.getCursor();
            if (reader.canRead() && !Character.isWhitespace(reader.peek())) {
                try {
                    return new VolumeTemplate.Axis(true, false, reader.readDouble());
                } catch (CommandSyntaxException e) {
                    reader.setCursor(beforeOffset);
                }
            }
            return new VolumeTemplate.Axis(true, false, 0);
        }

        try {
            return new VolumeTemplate.Axis(false, false, reader.readDouble());
        } catch (CommandSyntaxException e) {
            return null;
        }
    }

}
