package xaero.pac.common.server.claims.protection;

import com.mojang.datafixers.util.Either;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tags.TagKey;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

public final class ChunkProtectionExceptionSet<T> {

	private final Set<T> exceptions;
	private final Set<T> tagBasedExceptions;
	private final Set<TagKey<T>> exceptionTags;
	private final ExceptionElementType<T> elementType;

	private ChunkProtectionExceptionSet(Set<T> exceptions, Set<T> tagBasedExceptions, Set<TagKey<T>> exceptionTags, ExceptionElementType<T> elementType) {
		this.exceptions = exceptions;
		this.tagBasedExceptions = tagBasedExceptions;
		this.exceptionTags = exceptionTags;
		this.elementType = elementType;
	}

	public boolean contains(T object){
		if(object == null)
			return false;
		return exceptions.contains(object) || tagBasedExceptions.contains(object);
	}

	public void updateTagExceptions(MinecraftServer server){
		tagBasedExceptions.clear();
		Registry<T> elementRegistry = elementType.getRegistry(server);
		exceptionTags.stream().flatMap(tag -> elementRegistry.get(tag).stream().flatMap(HolderSet.Named::stream).map(Holder::value)).forEach(tagBasedExceptions::add);
	}

	public Stream<Either<T, TagKey<T>>> stream(){
		return Stream.concat(exceptions.stream().map(Either::left), exceptionTags.stream().map(Either::right));
	}

	public ExceptionElementType<T> getElementType() {
		return elementType;
	}

	public static class Builder<T> {

		private final Set<T> exceptions;
		private final Set<TagKey<T>> exceptionTags;
		private final ExceptionElementType<T> elementType;

		private Builder(ExceptionElementType<T> elementType){
			exceptions = new HashSet<>();
			exceptionTags = new HashSet<>();
			this.elementType = elementType;
		}

		public Builder<T> setDefault(){
			exceptions.clear();
			exceptionTags.clear();
			return this;
		}

		public Builder<T> add(T exception){
			exceptions.add(exception);
			return this;
		}

		public Builder<T> addTag(TagKey<T> exceptionTag){
			exceptionTags.add(exceptionTag);
			return this;
		}

		public Builder<T> addEither(Either<T, TagKey<T>> eitherException){
			eitherException.left().ifPresent(exceptions::add);
			eitherException.right().ifPresent(exceptionTags::add);
			return this;
		}

		public ChunkProtectionExceptionSet<T> build(){
			return new ChunkProtectionExceptionSet<>(exceptions, new HashSet<>(), exceptionTags, elementType);
		}

		public static <T> Builder<T> begin(ExceptionElementType<T> elementType){
			return new Builder<T>(elementType).setDefault();
		}

	}

}
