package net.mehvahdjukaar.supplementaries.client.models;

import com.google.common.collect.ImmutableList;
import net.mehvahdjukaar.supplementaries.block.BlockProperties;
import net.mehvahdjukaar.supplementaries.block.blocks.FrameBlock;
import net.mehvahdjukaar.supplementaries.block.blocks.MimicBlock;
import net.mehvahdjukaar.supplementaries.block.tiles.FrameBlockTile;
import net.mehvahdjukaar.supplementaries.common.Textures;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockModelShapes;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.util.Direction;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.client.model.data.IDynamicBakedModel;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.pipeline.BakedQuadBuilder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class FrameBlockBakedModel implements IDynamicBakedModel {
    private final IBakedModel overlay;
    private final BlockModelShapes blockModelShaper;

    public FrameBlockBakedModel(IBakedModel overlay) {
        this.overlay = overlay;
        this.blockModelShaper = Minecraft.getInstance().getBlockRenderer().getBlockModelShaper();
    }

    @Nonnull
    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @Nonnull Random rand, @Nonnull IModelData extraData) {

        //always on cutout layer

        List<BakedQuad> quads = new ArrayList<>();

        if(state != null) {
            try {
                BlockState mimic = extraData.getData(BlockProperties.MIMIC);
                if (mimic == null || (mimic.isAir())) {
                    IBakedModel model = blockModelShaper.getBlockModel(state.setValue(FrameBlock.HAS_BLOCK, false));
                    quads.addAll(model.getQuads(mimic, side, rand, EmptyModelData.INSTANCE));
                    return quads;
                }

                if (!(mimic.getBlock() instanceof MimicBlock)) {
                    IBakedModel model = blockModelShaper.getBlockModel(mimic);

                    quads.addAll(model.getQuads(mimic, side, rand, EmptyModelData.INSTANCE));
                }
            } catch (Exception ignored) {
            }

            //IBakedModel overlay = Minecraft.getInstance().getBlockRenderer().getBlockModelShaper().getBlockModel(state.setValue(FrameBlock.TILE,2));
            try {
                quads.addAll(overlay.getQuads(state, side, rand, EmptyModelData.INSTANCE));
            } catch (Exception ignored) {
            }
        }

        return quads;
    }

    @Override
    public boolean useAmbientOcclusion() {
        return true;
    }

    @Override
    public boolean isGui3d() {
        return false;
    }

    @Override
    public boolean usesBlockLight() {
        return false;
    }

    @Override
    public boolean isCustomRenderer() {
        return false;
    }

    @Override
    public TextureAtlasSprite getParticleIcon() {
        return Minecraft.getInstance().getTextureAtlas(AtlasTexture.LOCATION_BLOCKS).apply(Textures.TIMBER_CROSS_BRACE_TEXTURE);
    }

    @Override
    public ItemOverrideList getOverrides() {
        return ItemOverrideList.EMPTY;
    }

    @Override
    public ItemCameraTransforms getTransforms() {
        return ItemCameraTransforms.NO_TRANSFORMS;
    }


}