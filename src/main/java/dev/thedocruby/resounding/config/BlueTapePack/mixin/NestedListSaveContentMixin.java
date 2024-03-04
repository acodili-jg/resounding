package dev.thedocruby.resounding.config.BlueTapePack.mixin;

import me.shedaniel.clothconfig2.api.ReferenceProvider;
import me.shedaniel.clothconfig2.gui.entries.AbstractListListEntry;
import me.shedaniel.clothconfig2.gui.entries.NestedListListEntry;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;

@SuppressWarnings("ALL")
@Mixin(NestedListListEntry.class)
public abstract class NestedListSaveContentMixin extends AbstractListListEntry {
    @Shadow
    @Final
    private List<ReferenceProvider<?>> referencableEntries;

    public NestedListSaveContentMixin(
        final Text fieldName,
        final List value,
        final boolean defaultExpanded,
        final Supplier tooltipSupplier,
        final Consumer saveConsumer,
        final Supplier defaultValue,
        final Text resetButtonKey,
        final boolean requiresRestart,
        final boolean deleteButtonEnabled,
        final boolean insertInFront,
        final BiFunction createNewCell
    ) {
        super(
            fieldName,
            value,
            defaultExpanded,
            tooltipSupplier,
            saveConsumer,
            defaultValue,
            resetButtonKey,
            requiresRestart,
            deleteButtonEnabled,
            insertInFront,
            createNewCell
        );
    }

    @Override
    public void save(){
        try {
            referencableEntries.forEach(e -> e.provideReferenceEntry().save());
        } catch (final Exception ignored) {}
        super.save();
    }
}
