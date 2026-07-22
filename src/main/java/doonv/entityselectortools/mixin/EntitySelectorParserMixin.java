package doonv.entityselectortools.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import doonv.entityselectortools.predicate.PredicateKeyHolder;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.commands.arguments.selector.EntitySelectorParser;
import net.minecraft.commands.arguments.selector.options.EntitySelectorOptions;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/// Captures the predicate [ResourceKey] when parsing `@e[predicate=...]` and transfers it to the [EntitySelector].
@Mixin(EntitySelectorParser.class)
public abstract class EntitySelectorParserMixin implements PredicateKeyHolder {
    @Unique
    private ResourceKey<LootItemCondition> predicateKey;

    @WrapOperation(
            method = "parseOptions",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/commands/arguments/selector/options/EntitySelectorOptions$Modifier;handle(Lnet/minecraft/commands/arguments/selector/EntitySelectorParser;)V"
            )
    )
            //~ if >=26.1 'ordinal = 0' -> 'name = "key"'
    private void capturePredicateOption(EntitySelectorOptions.Modifier handler, EntitySelectorParser parser, Operation<Void> original, @Local(ordinal = 0) String key) {
        if (!"predicate".equals(key)) {
            original.call(handler, parser);
            return;
        }
        int cursor = parser.getReader().getCursor();
        original.call(handler, parser);
        String consumed = parser.getReader().getString().substring(cursor, parser.getReader().getCursor()).trim();
        if (consumed.startsWith("!")) consumed = consumed.substring(1).trim();
        if (Identifier.tryParse(consumed) instanceof Identifier identifier)
            this.predicateKey = ResourceKey.create(Registries.PREDICATE, identifier);
    }

    @Inject(method = "parse", at = @At("RETURN"))
    private void transferPredicateKey(CallbackInfoReturnable<EntitySelector> cir) {
        if (this.predicateKey != null) {
            EntitySelector selector = cir.getReturnValue();
            if (selector instanceof PredicateKeyHolder holder) {
                holder.entitySelectorTools$setPredicateKey(this.predicateKey);
            }
        }
    }

    @Override
    public void entitySelectorTools$setPredicateKey(ResourceKey<LootItemCondition> key) {
        this.predicateKey = key;
    }
}
