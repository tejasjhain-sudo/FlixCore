package org.lime.swiftCore.libs.lightcore.api.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class StreamUtil<T> {
   private Stream<T> stream;

   private StreamUtil(@Nullable Collection<T> var1) {
      this.stream = var1 == null ? Stream.empty() : var1.stream();
   }

   @NotNull
   public static <T> StreamUtil<T> from(@Nullable Collection<T> var0) {
      return new StreamUtil<>(var0);
   }

   @NotNull
   public StreamUtil<T> filter(@NotNull Predicate<? super T> var1) {
      this.stream = this.stream.filter(var1);
      return this;
   }

   @NotNull
   public StreamUtil<T> filterNotNull() {
      this.stream = this.stream.filter(Objects::nonNull);
      return this;
   }

   @NotNull
   public StreamUtil<T> distinct() {
      this.stream = this.stream.distinct();
      return this;
   }

   @NotNull
   public StreamUtil<T> distinctBy(@NotNull Function<? super T, ?> var1) {
      HashSet var2 = new HashSet();
      this.stream = this.stream.filter(var2x -> var2.add(var1.apply((T)var2x)));
      return this;
   }

   @NotNull
   public <R> StreamUtil<R> map(@NotNull Function<? super T, ? extends R> var1) {
      List var2 = this.stream.map(var1).toList();
      return from(var2);
   }

   @NotNull
   public <R> StreamUtil<R> flatMap(@NotNull Function<? super T, ? extends Collection<R>> var1) {
      List var2 = this.stream.flatMap(var1x -> {
         Collection var2x = (Collection)var1.apply((T)var1x);
         return var2x == null ? Stream.empty() : var2x.stream();
      }).toList();
      return from(var2);
   }

   @NotNull
   public StreamUtil<T> sorted(@NotNull Comparator<? super T> var1) {
      this.stream = this.stream.sorted(var1);
      return this;
   }

   @NotNull
   public StreamUtil<T> reverse() {
      ArrayList var1 = new ArrayList<>(this.stream.toList());
      Collections.reverse(var1);
      this.stream = var1.stream();
      return this;
   }

   @NotNull
   public StreamUtil<T> peek(@NotNull Consumer<? super T> var1) {
      this.stream = this.stream.peek(var1);
      return this;
   }

   @NotNull
   public StreamUtil<T> limit(long var1) {
      this.stream = this.stream.limit(var1);
      return this;
   }

   @NotNull
   public StreamUtil<T> skip(long var1) {
      this.stream = this.stream.skip(var1);
      return this;
   }

   @NotNull
   public StreamUtil<T> parallel() {
      this.stream = this.stream.parallel();
      return this;
   }

   @NotNull
   public StreamUtil<T> sequential() {
      this.stream = this.stream.sequential();
      return this;
   }

   @NotNull
   public <R, A> R collect(@NotNull Collector<? super T, A, R> var1) {
      return this.stream.collect(var1);
   }

   @NotNull
   public List<T> toList() {
      return this.stream.toList();
   }

   @NotNull
   public Set<T> toSet() {
      return this.stream.collect(Collectors.toSet());
   }

   @NotNull
   public <K> Map<K, List<T>> groupBy(@NotNull Function<? super T, ? extends K> var1) {
      return this.stream.collect(Collectors.groupingBy(var1));
   }

   @NotNull
   public <K, V> Map<K, V> toMap(@NotNull Function<? super T, ? extends K> var1, @NotNull Function<? super T, ? extends V> var2) {
      return this.stream.collect(Collectors.toMap(var1, var2, (var0, var1x) -> var1x));
   }

   @NotNull
   public Optional<T> findFirst() {
      return this.stream.findFirst();
   }

   @NotNull
   public Optional<T> findAny() {
      return this.stream.findAny();
   }

   @NotNull
   public Optional<T> find(@NotNull Predicate<? super T> var1) {
      return this.stream.filter(var1).findFirst();
   }

   @NotNull
   public Optional<T> min(@NotNull Comparator<? super T> var1) {
      return this.stream.min(var1);
   }

   @NotNull
   public Optional<T> max(@NotNull Comparator<? super T> var1) {
      return this.stream.max(var1);
   }

   @NotNull
   public Optional<T> reduce(@NotNull BinaryOperator<T> var1) {
      return this.stream.reduce(var1);
   }

   public long count() {
      return this.stream.count();
   }

   public boolean anyMatch(@NotNull Predicate<? super T> var1) {
      return this.stream.anyMatch(var1);
   }

   public boolean allMatch(@NotNull Predicate<? super T> var1) {
      return this.stream.allMatch(var1);
   }

   public boolean noneMatch(@NotNull Predicate<? super T> var1) {
      return this.stream.noneMatch(var1);
   }

   public void forEach(@NotNull Consumer<? super T> var1) {
      this.stream.forEach(var1);
   }

   public void forEachParallel(@NotNull Consumer<? super T> var1) {
      this.stream.parallel().forEach(var1);
   }
}
