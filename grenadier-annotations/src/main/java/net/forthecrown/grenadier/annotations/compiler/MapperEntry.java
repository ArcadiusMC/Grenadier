package net.forthecrown.grenadier.annotations.compiler;

import net.forthecrown.grenadier.annotations.ArgumentModifier;

public record MapperEntry(String name, ArgumentModifier<Object, Object> modifier) {

}
