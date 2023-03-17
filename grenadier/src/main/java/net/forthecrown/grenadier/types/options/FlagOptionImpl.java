package net.forthecrown.grenadier.types.options;

class FlagOptionImpl extends AbstractOption implements FlagOption {

  public FlagOptionImpl(AbstractBuilder<?> builder) {
    super(builder);
  }

  static class BuilderImpl
      extends AbstractBuilder<FlagOption.Builder>
      implements FlagOption.Builder
  {
    @Override
    public FlagOption build() {
      return new FlagOptionImpl(this);
    }
  }
}