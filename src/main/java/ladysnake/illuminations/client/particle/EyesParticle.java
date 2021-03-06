package ladysnake.illuminations.client.particle;

import ladysnake.illuminations.client.Config;
import ladysnake.illuminations.client.IlluminationsClient;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleFactory;
import net.minecraft.client.particle.ParticleTextureSheet;
import net.minecraft.client.particle.SpriteBillboardParticle;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Quaternion;
import net.minecraft.util.math.Vec3d;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class EyesParticle extends SpriteBillboardParticle {
    protected float alpha = 1f;

    private static final Random RANDOM = new Random();
    private final SpriteProvider spriteProvider;

    public EyesParticle(ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ, SpriteProvider spriteProvider) {
        super(world, x, y, z, velocityX, velocityY, velocityZ);
        this.spriteProvider = spriteProvider;
        this.setSprite(spriteProvider.getSprite(0, 3));

        this.scale *= 1f + new Random().nextFloat();
        this.maxAge = ThreadLocalRandom.current().nextInt(400, 1201); // live between   seconds and one minute
        this.collidesWithWorld = true;

        this.colorRed = 1f;
        this.colorGreen = 1f;
        this.colorBlue = 1f;
    }

    @Override
    public void buildGeometry(VertexConsumer vertexConsumer, Camera camera, float tickDelta) {
        // disable if night vision or config is set to disabled
        if (camera.getFocusedEntity() instanceof LivingEntity && ((LivingEntity) camera.getFocusedEntity()).hasStatusEffect(StatusEffects.NIGHT_VISION) || Config.getEyesInTheDark() == Config.EyesInTheDark.DISABLE) {
            this.markDead();
        }

        Vec3d vec3d = camera.getPos();
        float f = (float) (MathHelper.lerp(tickDelta, this.prevPosX, this.x) - vec3d.getX());
        float g = (float) (MathHelper.lerp(tickDelta, this.prevPosY, this.y) - vec3d.getY());
        float h = (float) (MathHelper.lerp(tickDelta, this.prevPosZ, this.z) - vec3d.getZ());
        Quaternion quaternion2;
        if (this.angle == 0.0F) {
            quaternion2 = camera.getRotation();
        } else {
            quaternion2 = new Quaternion(camera.getRotation());
            float i = MathHelper.lerp(tickDelta, this.prevAngle, this.angle);
            quaternion2.hamiltonProduct(Vector3f.POSITIVE_Z.getRadialQuaternion(i));
        }

        Vector3f vector3f = new Vector3f(-1.0F, -1.0F, 0.0F);
        vector3f.rotate(quaternion2);
        Vector3f[] vector3fs = new Vector3f[]{new Vector3f(-1.0F, -1.0F, 0.0F), new Vector3f(-1.0F, 1.0F, 0.0F), new Vector3f(1.0F, 1.0F, 0.0F), new Vector3f(1.0F, -1.0F, 0.0F)};
        float j = this.getSize(tickDelta);

        for (int k = 0; k < 4; ++k) {
            Vector3f vector3f2 = vector3fs[k];
            vector3f2.rotate(quaternion2);
            vector3f2.scale(j);
            vector3f2.add(f, g, h);
        }

        float minU = this.getMinU();
        float maxU = this.getMaxU();
        float minV = this.getMinV();
        float maxV = this.getMaxV();
        int l = 15728880;
        float a = Math.min(1f, Math.max(0f, this.colorAlpha));

        vertexConsumer.vertex(vector3fs[0].getX(), vector3fs[0].getY(), vector3fs[0].getZ()).texture(maxU, maxV).color(colorRed, colorGreen, colorBlue, colorAlpha).light(l).next();
        vertexConsumer.vertex(vector3fs[1].getX(), vector3fs[1].getY(), vector3fs[1].getZ()).texture(maxU, minV).color(colorRed, colorGreen, colorBlue, colorAlpha).light(l).next();
        vertexConsumer.vertex(vector3fs[2].getX(), vector3fs[2].getY(), vector3fs[2].getZ()).texture(minU, minV).color(colorRed, colorGreen, colorBlue, colorAlpha).light(l).next();
        vertexConsumer.vertex(vector3fs[3].getX(), vector3fs[3].getY(), vector3fs[3].getZ()).texture(minU, maxV).color(colorRed, colorGreen, colorBlue, colorAlpha).light(l).next();
    }

    public ParticleTextureSheet getType() {
        return ParticleTextureSheet.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Environment(EnvType.CLIENT)
    public static class DefaultFactory implements ParticleFactory<DefaultParticleType> {
        private final SpriteProvider spriteProvider;

        public DefaultFactory(SpriteProvider spriteProvider) {
            this.spriteProvider = spriteProvider;
        }

        public Particle createParticle(DefaultParticleType defaultParticleType, ClientWorld clientWorld, double d, double e, double f, double g, double h, double i) {
            return new EyesParticle(clientWorld, d, e, f, g, h, i, this.spriteProvider);
        }
    }

    public void tick() {
        if (this.age++ < this.maxAge) {
            if (this.age < 1) {
                this.setSprite(spriteProvider.getSprite(0, 3));
            } else if (this.age < 2) {
                this.setSprite(spriteProvider.getSprite(1, 3));
            } else if (this.age < 3) {
                this.setSprite(spriteProvider.getSprite(2, 3));
            } else {
                this.setSprite(spriteProvider.getSprite(3, 3));
            }
        } else {
            if (this.age < this.maxAge + 1) {
                this.setSprite(spriteProvider.getSprite(2, 3));
            } else if (this.age < this.maxAge + 2) {
                this.setSprite(spriteProvider.getSprite(1, 3));
            } else if (this.age < this.maxAge + 3) {
                this.setSprite(spriteProvider.getSprite(0, 3));
            } else {
                this.markDead();
            }
        }

        this.prevPosX = this.x;
        this.prevPosY = this.y;
        this.prevPosZ = this.z;

        // disappear if light or if player gets too close
        if (this.maxAge > this.age && (world.getLightLevel(new BlockPos(x, y, z)) > 0 || world.getClosestPlayer(x, y, z, IlluminationsClient.EYES_VANISHING_DISTANCE, false) != null)) {
            this.maxAge = this.age;
        }
    }

}
