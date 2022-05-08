package org.moon.figura.lua.api;

import com.mojang.brigadier.StringReader;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.arguments.ParticleArgument;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.level.Level;
import org.moon.figura.lua.LuaWhitelist;
import org.moon.figura.lua.api.world.WorldAPI;
import org.moon.figura.lua.docs.LuaFunctionOverload;
import org.moon.figura.lua.docs.LuaMethodDoc;
import org.moon.figura.lua.docs.LuaTypeDoc;
import org.moon.figura.math.vector.FiguraVec3;
import org.moon.figura.math.vector.FiguraVec6;
import org.moon.figura.utils.LuaUtils;
import org.terasology.jnlua.LuaRuntimeException;

@LuaWhitelist
@LuaTypeDoc(
        name = "ParticleAPI",
        description = "A global API which is used for dealing with Minecraft's particles. " +
                "Can currently only be used to summon a particle."
)
public class ParticleAPI {

    public static final ParticleAPI INSTANCE = new ParticleAPI();

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaFunctionOverload(
                            argumentTypes = {String.class, FiguraVec6.class},
                            argumentNames = {"name", "posVel"},
                            returnType = void.class
                    ),
                    @LuaFunctionOverload(
                            argumentTypes = {String.class, FiguraVec3.class},
                            argumentNames = {"name", "pos"},
                            returnType = void.class
                    ),
                    @LuaFunctionOverload(
                            argumentTypes = {String.class, FiguraVec3.class, FiguraVec3.class},
                            argumentNames = {"name", "pos", "vel"},
                            returnType = void.class
                    ),
                    @LuaFunctionOverload(
                            argumentTypes = {String.class, Double.class, Double.class, Double.class},
                            argumentNames = {"name", "posX", "posY", "posZ"},
                            returnType = void.class
                    ),
                    @LuaFunctionOverload(
                            argumentTypes = {String.class, FiguraVec3.class, Double.class, Double.class, Double.class},
                            argumentNames = {"name", "pos", "velX", "velY", "velZ"},
                            returnType = void.class
                    ),
                    @LuaFunctionOverload(
                            argumentTypes = {String.class, Double.class, Double.class, Double.class, FiguraVec3.class},
                            argumentNames = {"name", "posX", "posY", "posZ", "vel"},
                            returnType = void.class
                    ),
                    @LuaFunctionOverload(
                            argumentTypes = {String.class, Double.class, Double.class, Double.class, Double.class, Double.class, Double.class},
                            argumentNames = {"name", "posX", "posY", "posZ", "velX", "velY", "velZ"},
                            returnType = void.class
                    )
            },
            description = "Creates a particle with the given name at the specified position, with the given velocity. " +
                    "Some particles have special properties, like the \"dust\" particle. For these particles, the special " +
                    "properties can be put into the \"name\" parameter, the same way as it works for commands."
    )
    public static void addParticle(String id, Object x, Object y, Double z, Object w, Double t, Double h) {
        FiguraVec3 pos, vel;

        //Parse pos and vel
        if (x instanceof FiguraVec3) {
            pos = ((FiguraVec3) x).copy();
            if (y instanceof FiguraVec3) {
                vel = ((FiguraVec3) y).copy();
            } else if (y == null || y instanceof Double) {
                vel = LuaUtils.parseVec3("addParticle", y, z, (Double) w);
            } else {
                throw new LuaRuntimeException("Illegal argument to addParticle(): " + y);
            }
        } else if (x == null || x instanceof Double) {
            pos = LuaUtils.parseVec3("addParticle", x, (Double) y, z);
            if (w instanceof FiguraVec3) {
                vel = ((FiguraVec3) w).copy();
            } else if (w == null || w instanceof Double) {
                vel = LuaUtils.parseVec3("addParticle", w, t, h);
            } else {
                throw new LuaRuntimeException("Illegal argument to addParticle(): " + w);
            }
        } else if (x instanceof FiguraVec6 posVel) {
            pos = FiguraVec3.of(posVel.x, posVel.y, posVel.z);
            vel = FiguraVec3.of(posVel.w, posVel.t, posVel.h);
        } else {
            throw new LuaRuntimeException("Illegal argument to addParticle(): " + x);
        }

        try {
            ParticleOptions particle = ParticleArgument.readParticle(new StringReader(id));
            Level level = WorldAPI.getCurrentWorld();

            if (!Minecraft.getInstance().isPaused() && level != null)
                level.addParticle(particle, pos.x, pos.y, pos.z, vel.x, vel.y, vel.z);
        } catch (Exception e) {
            throw new LuaRuntimeException(e.getMessage());
        }
        pos.free();
        vel.free();
    }
}