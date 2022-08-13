package de.leximon.fluidlogged.core;

import net.minecraft.world.level.block.state.properties.IntegerProperty;

public class FluidProperty extends IntegerProperty {

    protected FluidProperty(String name) {
        super(name, 0, FluidloggedConfig.fluids.size());
    }

    public static FluidProperty of(String name) {
        return new FluidProperty(name);
    }
}
