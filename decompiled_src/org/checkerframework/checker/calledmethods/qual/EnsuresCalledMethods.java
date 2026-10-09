package org.checkerframework.checker.calledmethods.qual;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.checkerframework.framework.qual.InheritedAnnotation;
import org.checkerframework.framework.qual.PostconditionAnnotation;
import org.checkerframework.framework.qual.QualifierArgument;

@PostconditionAnnotation(
   qualifier = CalledMethods.class
)
@Target({ElementType.METHOD, ElementType.CONSTRUCTOR})
@Repeatable(EnsuresCalledMethods.List.class)
public @interface EnsuresCalledMethods {
   String[] value();

   @QualifierArgument("value")
   String[] methods();

   @Documented
   @Retention(RetentionPolicy.RUNTIME)
   @Target({ElementType.METHOD, ElementType.CONSTRUCTOR})
   @InheritedAnnotation
   public @interface List {
      EnsuresCalledMethods[] value();
   }
}
