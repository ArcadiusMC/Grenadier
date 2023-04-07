package net.forthecrown.grenadier.annotations.compiler;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.forthecrown.grenadier.annotations.compiler.MemberChain.FieldMember;
import net.forthecrown.grenadier.annotations.compiler.MemberChain.MethodMember;
import net.forthecrown.grenadier.annotations.tree.MemberChainTree;
import net.forthecrown.grenadier.annotations.tree.MemberChainTree.Kind;
import net.forthecrown.grenadier.annotations.util.Result;

final class MemberChainCompiler {
  private MemberChainCompiler() {}

  public static Result<MemberChain> compile(
      Class<?> declaring,
      MemberChainTree ref,
      ChainCompileConfig config
  ) {
    if (ref.kind() == Kind.METHOD) {
      return compileMethod(declaring, ref, config);
    }

    return compileField(declaring, ref, config);
  }

  private static Result<MemberChain> compileField(
      Class<?> declaring,
      MemberChainTree ref,
      ChainCompileConfig config
  ) {
    int position = config.getPosition();
    Result<Field> f = ref.getField(declaring).withPosition(position);

    if (f.isError()) {
      return f.map(null);
    }

    Field field = f.getValue();

    if (ref.next() == null) {
      return typeContains(field, config.getFinalFieldTypes()).map(field1 -> {
        return new FieldMember(field1, field1.isAccessible(), null);
      });
    }

    return compile(field.getType(), ref.next(), config).map(chain -> {
      return new FieldMember(field, field.isAccessible(), chain);
    });
  }

  private static Result<Field> typeContains(Field field, Class[] types) {
    if (types == null || types.length < 1) {
      return Result.success(field);
    }

    for (var t: types) {
      if (t.isAssignableFrom(field.getType())) {
        return Result.success(field);
      }
    }

    return Result.fail(
        "Field '%s' in %s must return %s",
        field.getName(), field.getDeclaringClass(),

        types.length == 1 ? types[0] : ("one of " + Arrays.toString(types))
    );
  }

  private static Result<MemberChain> compileMethod(
      Class<?> declaring,
      MemberChainTree ref,
      ChainCompileConfig config
  ) {
    int position = config.getPosition();

    if (ref.next() == null) {
      Result<Method> methodResult;

      if (config.getParamTypes() != null) {
        methodResult = ref.getMethod(declaring, config.getParamTypes());
      } else {
        methodResult = ref.findUniqueMethod(declaring);
      }

      return methodResult.withPosition(position)
          .flatMap(method -> validateFinalMethod(method, config, position));
    }

    Result<Method> m = ref.getMethod(declaring).withPosition(position);

    if (m.isError()) {
      return m.map(null);
    }

    Method method = m.getValue();

    if (method.getReturnType() == Void.TYPE) {
      return Result.fail(
          position,
          "Method %s in %s returns void when a return type is required",
          ref.name(), declaring
      );
    }

    return compile(method.getReturnType(), ref.next(), config).map(chain -> {
      return new MethodMember(method, method.isAccessible(), chain);
    });
  }

  private static Result<MemberChain> validateFinalMethod(
      Method method,
      ChainCompileConfig config,
      int position
  ) {
    var validReturn = config.getValidReturnType();

    if (validReturn != null
        && !validReturn.isAssignableFrom(method.getReturnType())
    ) {
      return Result.fail(position,
          "Invalid return type %s for method '%s' in %s, must be %s",
          method.getReturnType(),
          method.getName(),
          method.getDeclaringClass(),
          validReturn
      );
    }

    return Result.success(
        new MethodMember(method, method.isAccessible(), null)
    );
  }

  @Getter @Setter
  @Accessors(chain = true)
  @RequiredArgsConstructor
  static class ChainCompileConfig {
    private Class<?>[] finalFieldTypes;
    private Class<?> validReturnType;
    private Class<?>[] paramTypes;

    private final int position;

    public static ChainCompileConfig create(int position) {
      return new ChainCompileConfig(position);
    }

    public ChainCompileConfig setFinalFieldTypes(Class<?>... finalFieldTypes) {
      this.finalFieldTypes = finalFieldTypes;
      return this;
    }

    public ChainCompileConfig setParamTypes(Class<?>... paramTypes) {
      this.paramTypes = paramTypes;
      return this;
    }
  }
}