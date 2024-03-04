package dev.thedocruby.resounding.mixin;

import dev.thedocruby.resounding.Cache;
import dev.thedocruby.resounding.Engine;
import dev.thedocruby.resounding.Material;
import dev.thedocruby.resounding.raycast.Branch;
import dev.thedocruby.resounding.toolbox.ChunkChain;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.world.ClientChunkManager;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.ChunkData;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.*;
import net.minecraft.world.gen.chunk.BlendingData;
import net.minecraft.world.tick.ChunkTickScheduler;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.stream.Stream;

@Environment(EnvType.CLIENT)
@Mixin(WorldChunk.class)
public abstract class WorldChunkMixin extends Chunk implements ChunkChain {
    @Shadow
    @Final
    World world;

    @Shadow
    public abstract ChunkStatus getStatus();

    @Shadow @Final
    static Logger LOGGER;

    @Shadow
    public abstract BlockState getBlockState(BlockPos pos);

    public int yOffset = 1; // 1.18 (0- -16(y) >> 4)

    public Map<Long, VoxelShape> shapes = new ConcurrentHashMap<>(48);

    public @NotNull Map<Long, VoxelShape> getShapes() {
        return this.shapes;
    }

    public @NotNull Branch[] branches = new Branch[0];

    public boolean loaded = true;

    @Override
    public Branch getBranch(final int y) {
        return ArrayUtils.get(this.branches, y + this.yOffset, null);
    }

    public ChunkChain[]   xPlane = {null, this, null};
    public ChunkChain[]   zPlane = {null, this, null};
    public ChunkChain[][] planes = {xPlane, zPlane};

    public ChunkChain set(final int plane, final ChunkChain negative, final ChunkChain positive) {
        this.planes[plane][0] = negative;
        this.planes[plane][2] = positive;
        return this;
    }

    public ChunkChain set(final int plane, final int index, final ChunkChain link) {
        this.planes[plane][1 + index] = link;
        return this;
    }

    public ChunkChain get(final int plane, final int index) {
        return this.planes[plane][1 + index];
    }

    // one-dimensional traversal
    public ChunkChain traverse(final int d, final int plane) {
        final var dx = (int) Math.signum(d);
        if (dx == 0) { return this; }

        @Nullable
        final var next = this.planes[plane][1 + dx];
        if (next == null) {
            return null;
        } else {
            return next.traverse(d - dx, plane);
        }
    }

    public ChunkChain access_(final int tx, final int tz) {
        final var next = traverse(tx, 0);
        if (next == null) {
            return null;
        } else {
            return next.traverse(tz, 1);
        }
    }

    public ChunkChain access(final int x, final int z) {
        final var pos = this.getPos();
        return access_(x - pos.x, z - pos.z); // TODO - validate
    }

    // pass along to super {
    public WorldChunkMixin(
        final ChunkPos pos,
        final UpgradeData upgradeData,
        final HeightLimitView heightLimitView,
        final Registry<Biome> biome,
        final long inhabitedTime,
        final @Nullable ChunkSection[] sectionArrayInitializer,
        final @Nullable BlendingData blendingData
    ) {
        super(
            pos,
            upgradeData,
            heightLimitView,
            biome,
            inhabitedTime,
            sectionArrayInitializer,
            blendingData
        );
        //  LOGGER.info("I like 'em chunky");  // TODO remove
        //  if (sectionArrayInitializer != null) { // TODO ^
        //      this.initStorage();
        //  }
    }

    // }
    // upon receiving a packet, initialize storage {
    @Inject(
        method = """
            loadFromPacket(
                Lnet/minecraft/network/PacketByteBuf;
                Lnet/minecraft/nbt/NbtCompound;
                Ljava/util/function/Consumer;
            )V
        """,
        at = @At("RETURN")
    )
    private void load(
        final PacketByteBuf buf,
        final NbtCompound nbt,
        final Consumer<ChunkData.BlockEntityVisitor> consumer,
        final CallbackInfo callback
    ) {
        initStorage();
    }
    // }

    // upon creation of world, initialize storage {
    @Inject(
        method = """
            <init>(
                Lnet/minecraft/world/World;
                Lnet/minecraft/util/math/ChunkPos;
                Lnet/minecraft/world/chunk/UpgradeData;
                Lnet/minecraft/world/tick/ChunkTickScheduler;
                Lnet/minecraft/world/tick/ChunkTickScheduler;
                J
                [Lnet/minecraft/world/chunk/ChunkSection;
                Lnet/minecraft/world/chunk/WorldChunk$EntityLoader;
                Lnet/minecraft/world/gen/chunk/BlendingData;
            )V
        """,
        at = @At("RETURN")
    )
    private void create(
        final World world,
        final ChunkPos pos,
        final UpgradeData upgradeData,
        final ChunkTickScheduler<?> blockTickScheduler,
        final ChunkTickScheduler<?> fluidTickScheduler,
        final long inhabitedTime,
        final ChunkSection[] sectionArrayInitializer,
        final WorldChunk.EntityLoader entityLoader,
        final BlendingData blendingData,
        final CallbackInfo callback
    ) {
        if (sectionArrayInitializer != null) {
            initStorage();
        }
    }
    // }

    // for readability
    private ChunkChain take(final int x, final int z) {
        return (ChunkChain) this.world.getChunk(
            super.pos.x + x,
            super.pos.z + z,
            ChunkStatus.FULL,
            false
        );
    }

    public void initStorage() {
        if (this.world == null || !this.world.isClient) { return; }
        //* TODO remove
        if (!Engine.hasLoaded) {
            Engine.hasLoaded = Cache.generate();
            // return;
        }
        // */

        // 16 x 16 x 16 blocks
        final var chunkSections = getSectionArray();
        // LOGGER.info(String.valueOf(chunkSections.length)
        //     /* + "\t"
        //     + Arrays.toString(chunkSections)
        // */); // TODO remove
        this.yOffset  = -chunkSections[0].getYOffset() >> 4;
        final var pos = this.getPos();
        final var x   = pos.x << 4; // * 16
        final var z   = pos.z << 4; // * 16
        this.branches = new Branch[chunkSections.length];
        final var branches = new Branch[chunkSections.length];

        // chunk up a section into an octree
        Stream.of(chunkSections)
            .parallel()
            .forEach((chunkSection) -> {
                final var y = chunkSection.getYOffset();
                final var index = this.yOffset + (y >> 4);
                final var empty = chunkSection.isEmpty();
    
                final var air = new Branch(
                    new BlockPos(x, y, z),
                    16,
                    Cache.material(Blocks.AIR.getDefaultState())
                );
                final var blank = new Branch(new BlockPos(x, y, z),16);
    
                // provide fallback or all-air branch when necessary
                synchronized (branches) {
                    branches[index] = empty ? air : blank;
                }
                // only calculate if necessary
                if (empty) {
                    Cache.counter++;
                    Cache.octreePool.execute(() -> Cache.plantOctree(this, index, blank));
                }
            });

        this.branches = branches;

        final var adj = new ChunkChain[4];
        // retrieve & save locally
        this.set(0, -1, adj[0] = take(-1, +0));
        this.set(0, +1, adj[1] = take(+1, +0));
        this.set(1, -1, adj[2] = take(+0, -1));
        this.set(1, +1, adj[3] = take(+0, +1));

        // update self-references using reversed direction
        if (adj[0] != null) adj[0].set(0, +1, this);
        if (adj[1] != null) adj[1].set(0, -1, this);
        if (adj[2] != null) adj[2].set(1, +1, this);
        if (adj[3] != null) adj[3].set(1, -1, this);
    }

    public void set(final int index, final Branch branch) {
        this.branches[index] = branch;
    }

    // upon setting block in client, update our copy {
    @Inject(
        method = """
            setBlockState(
                Lnet/minecraft/util/math/BlockPos;
                Lnet/minecraft/block/BlockState;
                Z
            )Lnet/minecraft/block/BlockState;
        """,
        at = @At("HEAD")
    )
    private void setBlock(
        final BlockPos pos,
        final BlockState state,
        final boolean moved,
        final CallbackInfoReturnable<BlockState> cir
    ) {
        if (this.world.isClient && this.loaded) {
            updateBlock(pos, state, moved);
        }
    }
    // }

    @Mixin(ClientChunkManager.class)
    public abstract static class Unloaded {
        @Shadow
        @SuppressWarnings("SameReturnValue")
        public WorldChunk getChunk(
            final int x,
            final int z,
            final ChunkStatus chunkStatus,
            final boolean bl
        ) {
            return null;
        }

        // for readability
        private ChunkChain take(final int x, final int z) {
            return (ChunkChain) getChunk(x, z, ChunkStatus.FULL, false);
        }

        @Inject(method = "unload(II)V", at = @At("HEAD"))
        public void unload(final int x, final int z, final CallbackInfo callback) {
            final var adj = new ChunkChain[4];
            adj[0] = take(x - 1, z + 0);
            adj[1] = take(x + 1, z + 0);
            adj[2] = take(x + 0, z - 1);
            adj[3] = take(x + 0, z + 1);

            // delete self-references & using reversed direction
            if (adj[0] != null) adj[0].set(0,+1,null);
            if (adj[1] != null) adj[1].set(0,-1,null);
            if (adj[2] != null) adj[2].set(1,+1,null);
            if (adj[3] != null) adj[3].set(1,-1,null);
        }
    }

    private void updateBlock(final BlockPos pos, final BlockState state, final boolean moved) {
        // get smallest branch at position
        final var branch = this.getBranch(pos.getY() >> 4).get(pos);

        final var material = Cache.material(state);
        // if block is homogenous with branch
        //* TODO remove
        if (material.equals(branch.material)) { return; }
        // */

        // will get optimized on reload, must keep this function quick
        branch.material = null;
        this.shapes.remove(pos.asLong());
    }
}
