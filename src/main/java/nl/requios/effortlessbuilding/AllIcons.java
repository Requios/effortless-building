package nl.requios.effortlessbuilding;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import nl.requios.effortlessbuilding.create.Create;
import nl.requios.effortlessbuilding.create.foundation.gui.element.DelegatedStencilElement;
import nl.requios.effortlessbuilding.create.foundation.gui.element.ScreenElement;
import nl.requios.effortlessbuilding.create.foundation.utility.Color;
import org.joml.Matrix4f;

public class AllIcons implements ScreenElement {
    
    public static final ResourceLocation ICON_ATLAS = Create.asResource("textures/gui/icons.png");
    public static final int ICON_ATLAS_SIZE = 256;
    private static int x = 0, y = -1;
    private int iconX;
    private int iconY;
    
    public static final AllIcons
    I_SETTINGS = newRow(),
    I_UNDO = next(),
    I_REDO = next(),
    I_REPLACE = next(),
    I_REPLACE_AIR = next(),
    I_REPLACE_BLOCKS_AND_AIR = next(),
    I_REPLACE_BLOCKS = next(),
    I_REPLACE_OFFHAND_FILTERED = next(),
    I_PROTECT_TILE_ENTITIES = next();

    
    public static final AllIcons
    I_DISABLE = newRow(),
    I_SINGLE = next(),
    I_LINE = next(),
    I_WALL = next(),
    I_FLOOR = next(),
    I_CUBE = next(),
    I_DIAGONAL_LINE = next(),
    I_DIAGONAL_WALL = next(),
    I_SLOPED_FLOOR = next(),
    I_CIRCLE = next(),
    I_CYLINDER = next(),
    I_SPHERE = next(),
    I_PYRAMID = next(),
    I_CONE = next(),
    I_DOME = next();
    
    public static final AllIcons
    I_NORMAL_SPEED = newRow(),
    I_FAST_SPEED = next(),
    I_FILLED = next(),
    I_HOLLOW = next(),
    I_CUBE_FILLED = next(),
    I_CUBE_HOLLOW = next(),
    I_CUBE_SKELETON = next(),
    I_SHORT_EDGE = next(),
    I_LONG_EDGE = next(),
    I_CIRCLE_START_CORNER = next(),
    I_CIRCLE_START_CENTER = next(),
    I_THICKNESS_1 = next(),
    I_THICKNESS_3 = next(),
    I_THICKNESS_5 = next();
    
    public static final AllIcons
    I_PLAYER = newRow(),
    I_BLOCK_CENTER = next(),
    I_BLOCK_CORNER = next(),
    I_HIDE_LINES = next(),
    I_SHOW_LINES = next(),
    I_HIDE_AREAS = next(),
    I_SHOW_AREAS = next(),
    I_X_OFF = next(),
    I_X_ON = next(),
    I_Y_OFF = next(),
    I_Y_ON = next(),
    I_Z_OFF = next(),
    I_Z_ON = next(),
    I_ALTERNATE_OFF = next(),
    I_ALTERNATE_ON = next();
    
    
    public AllIcons(int x, int y) {
        iconX = x * 16;
        iconY = y * 16;
    }
    
    private static AllIcons next() {
        return new AllIcons(++x, y);
    }
    
    private static AllIcons newRow() {
        return new AllIcons(x = 0, ++y);
    }

    public void bind() {
        RenderSystem.setShaderTexture(0, ICON_ATLAS);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void render(PoseStack ms, int x, int y) {
        bind();
        GuiComponent.blit(ms, x, y, 0, iconX, iconY, 16, 16, 256, 256);
    }

    @OnlyIn(Dist.CLIENT)
    public void render(PoseStack ms, MultiBufferSource buffer, int color) {
        VertexConsumer builder = buffer.getBuffer(RenderType.textSeeThrough(ICON_ATLAS));
        Matrix4f matrix = ms.last().pose();
        Color rgb = new Color(color);
        int light = LightTexture.FULL_BRIGHT;
        
        Vec3 vec1 = new Vec3(0, 0, 0);
        Vec3 vec2 = new Vec3(0, 1, 0);
        Vec3 vec3 = new Vec3(1, 1, 0);
        Vec3 vec4 = new Vec3(1, 0, 0);
        
        float u1 = iconX * 1f / ICON_ATLAS_SIZE;
        float u2 = (iconX + 16) * 1f / ICON_ATLAS_SIZE;
        float v1 = iconY * 1f / ICON_ATLAS_SIZE;
        float v2 = (iconY + 16) * 1f / ICON_ATLAS_SIZE;
        
        vertex(builder, matrix, vec1, rgb, u1, v1, light);
        vertex(builder, matrix, vec2, rgb, u1, v2, light);
        vertex(builder, matrix, vec3, rgb, u2, v2, light);
        vertex(builder, matrix, vec4, rgb, u2, v1, light);
    }
    
    @OnlyIn(Dist.CLIENT)
    private void vertex(VertexConsumer builder, Matrix4f matrix, Vec3 vec, Color rgb, float u, float v, int light) {
        builder.vertex(matrix, (float) vec.x, (float) vec.y, (float) vec.z)
            .color(rgb.getRed(), rgb.getGreen(), rgb.getBlue(), 255)
            .uv(u, v)
            .uv2(light)
            .endVertex();
    }
    
    @OnlyIn(Dist.CLIENT)
    public DelegatedStencilElement asStencil() {
        return new DelegatedStencilElement().withStencilRenderer((ms, w, h, alpha) -> this.render(ms, 0, 0)).withBounds(16, 16);
    }
}
