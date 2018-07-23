package sponge_clj;

import clojure.lang.Keyword;
import com.google.common.reflect.TypeToken;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.manipulator.DataManipulatorBuilder;
import org.spongepowered.api.data.manipulator.immutable.common.AbstractImmutableSingleData;
import org.spongepowered.api.data.manipulator.mutable.common.AbstractSingleData;
import org.spongepowered.api.data.merge.MergeFunction;
import org.spongepowered.api.data.persistence.AbstractDataBuilder;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.Value;

import java.util.Optional;

public class LambdaMobData extends AbstractSingleData<Keyword, LambdaMobData, LambdaMobData.Immutable> {
    public static Key<Value<Keyword>> KEY;

    public static void initKey() {
        KEY = Key.builder()
                .type(new TypeToken<Value<Keyword>>(){})
                .id("lambdamob.id")
                .name("Lambda Mob Id")
                .query(DataQuery.of('.', "lambdamob.id"))
                .build();
    }

    LambdaMobData(Keyword keyword) {
        super(keyword, KEY);
    }

    @Override
    protected Value<Keyword> getValueGetter() {
        return Sponge.getRegistry().getValueFactory().createValue(KEY, getValue());
    }

    public Value<Keyword> color() {
        return getValueGetter();
    }

    @Override
    public Optional<LambdaMobData> fill(DataHolder dataHolder, MergeFunction overlap) {
        Optional<LambdaMobData> data_ = dataHolder.get(LambdaMobData.class);
        if (data_.isPresent()) {
            LambdaMobData data = data_.get();
            LambdaMobData finalData = overlap.merge(this, data);
            setValue(finalData.getValue());
        }
        return Optional.of(this);
    }

    @Override
    public Optional<LambdaMobData> from(DataContainer container) {
        return Optional.of(this);
    }

    @Override
    public LambdaMobData copy() {
        return new LambdaMobData(getValue());
    }

    @Override
    public Immutable asImmutable() {
        return new Immutable(getValue());
    }

    @Override
    public int getContentVersion() {
        return 1;
    }

    public static class Immutable extends AbstractImmutableSingleData<Keyword, Immutable, LambdaMobData> {
        Immutable(Keyword keyword) {
            super(keyword, LambdaMobData.KEY);
        }

        @Override
        protected ImmutableValue<?> getValueGetter() {
            return Sponge.getRegistry().getValueFactory().createValue(LambdaMobData.KEY, getValue()).asImmutable();
        }

        @Override
        public LambdaMobData asMutable() {
            return new LambdaMobData(getValue());
        }

        @Override
        public int getContentVersion() {
            return 1;
        }
    }

    public static class Builder extends AbstractDataBuilder<LambdaMobData> implements DataManipulatorBuilder<LambdaMobData, Immutable> {
        public Builder() {
            super(LambdaMobData.class, 1);
        }

        @Override
        public LambdaMobData create() {
            return new LambdaMobData(Keyword.intern("sponge-clj", "null"));
        }

        @Override
        public Optional<LambdaMobData> createFrom(DataHolder dataHolder) {
            return create().fill(dataHolder);
        }

        @Override
        protected Optional<LambdaMobData> buildContent(DataView container) throws InvalidDataException {
            return Optional.of(create());
        }
    }
}

