#noinspection ShrinkerUnresolvedReference
-keep public class myxal.Main {
    public static void main(java.lang.String[]);
    private static java.lang.Object lambda$*(io.github.seggan.myxal.runtime.ProgramStack);
}

-dontnote

-assumenosideeffects class kotlin.jvm.internal.Intrinsics {
    public static void checkNotNullExpressionValue(java.lang.Object, java.lang.String);
    public static void checkNotNull(java.lang.Object, java.lang.String);
    public static void checkNotNull(java.lang.Object);
}

-assumenosideeffects class io.github.seggan.myxal.runtime.math.BigComplex {
    public io.github.seggan.myxal.runtime.math.BigComplex add(io.github.seggan.myxal.runtime.math.BigComplex);
    public io.github.seggan.myxal.runtime.math.BigComplex subtract(io.github.seggan.myxal.runtime.math.BigComplex);
    public io.github.seggan.myxal.runtime.math.BigComplex multiply(io.github.seggan.myxal.runtime.math.BigComplex);
    public io.github.seggan.myxal.runtime.math.BigComplex divide(io.github.seggan.myxal.runtime.math.BigComplex, java.math.MathContext);
}