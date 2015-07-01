package com.googlecode.yadic.generics;

import com.googlecode.totallylazy.Option;
import com.googlecode.yadic.Container;
import com.googlecode.yadic.SimpleContainer;
import com.googlecode.yadic.examples.DecoratedGenericType;
import com.googlecode.yadic.examples.FlexibleNode;
import com.googlecode.yadic.examples.GenericType;
import com.googlecode.yadic.examples.Instance;
import com.googlecode.yadic.examples.Node;
import com.googlecode.yadic.examples.RootNode;
import com.googlecode.yadic.examples.UsesGenericType;
import com.googlecode.yadic.resolvers.OptionResolver;
import org.junit.Test;

import static com.googlecode.totallylazy.predicates.Predicates.always;
import static com.googlecode.totallylazy.reflection.Types.parameterizedType;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

@SuppressWarnings("unchecked")
public class GenericsTest {
    @Test
    public void containerShouldSupportGenericsDecoration() throws Exception {
        Container container = new SimpleContainer();
        container.addInstance(Integer.class, 1);
        container.addType(new TypeFor<Instance<Integer>>() {
        }.get(), new TypeFor<GenericType<Integer>>() {
        }.get());
        container.decorateType(new TypeFor<Instance<Integer>>() {
        }.get(), new TypeFor<DecoratedGenericType<Integer>>() {
        }.get());
        Instance<Integer> instance = (Instance<Integer>) container.resolve(new TypeFor<Instance<Integer>>(){{}}.get());
        assertThat(instance, is(instanceOf(DecoratedGenericType.class)));
    }

    @Test
    public void containerShouldSupportGenericWithWildCardOnConcrete() throws Exception {
        Container container = new SimpleContainer();
        container.addInstance(Integer.class, 1);
        container.addType(new TypeFor<GenericType<?>>() {{
        }}.get(), new TypeFor<GenericType<?>>() {{
        }}.get());
        GenericType<Integer> genericType = (GenericType<Integer>) container.resolve(new TypeFor<GenericType<Integer>>(){{}}.get());
        assertThat(genericType.instance(), is(1));
    }

    @Test
    public void containerShouldSupportGenericClassUsingTypeFor() throws Exception {
        Container container = new SimpleContainer();
        container.addInstance(String.class, "bob");
        container.addInstance(Integer.class, 1);
        container.addType(new TypeFor<GenericType<Integer>>() {{
        }}.get(), new TypeFor<GenericType<Integer>>() {{
        }}.get());
        container.add(UsesGenericType.class);
        UsesGenericType genericType = container.get(UsesGenericType.class);
        assertThat(genericType.instance().instance(), is(1));
    }

    @Test
    public void containerShouldSupportGenericClass() throws Exception {
        Container container = new SimpleContainer();
        container.addInstance(String.class, "bob");
        container.addInstance(Integer.class, 1);
        container.addType(parameterizedType(GenericType.class, Integer.class), parameterizedType(GenericType.class, Integer.class));
        container.add(UsesGenericType.class);
        UsesGenericType genericType = container.get(UsesGenericType.class);
        assertThat(genericType.instance().instance(), is(1));
    }

    @Test
    public void containerShouldSupportGenericInterfaceAndClass() throws Exception {
        Container container = new SimpleContainer();
        container.addInstance(String.class, "bob");
        container.addInstance(Integer.class, 1);
        container.addType(parameterizedType(Instance.class, Integer.class), parameterizedType(GenericType.class, Integer.class));
        container.add(UsesGenericType.class);
        UsesGenericType genericType = container.get(UsesGenericType.class);
        assertThat(genericType.instance().instance(), is(1));
    }

    @Test
    public void containerShouldSupportWildcards() throws Exception {
        Container container = new SimpleContainer();
        container.addType(new TypeFor<Option<?>>() {{
        }}.get(), new OptionResolver(container, always()));
        container.add(Node.class, RootNode.class);
        container.add(FlexibleNode.class);
        assertThat(container.get(FlexibleNode.class).parent(), is(instanceOf(RootNode.class)));
    }

    @Test
    public void containerShouldSupportSomeOption() throws Exception {
        Container container = new SimpleContainer();
        container.addType(new TypeFor<Option<Node>>() {{
        }}.get(), new OptionResolver(container, always()));
        container.add(Node.class, RootNode.class);
        container.add(FlexibleNode.class);
        assertThat(container.get(FlexibleNode.class).parent(), is(instanceOf(RootNode.class)));
    }

    @Test
    public void containerShouldSupportNoneOption() throws Exception {
        Container container = new SimpleContainer();
        container.addType(new TypeFor<Option<Node>>() {{
        }}.get(), new OptionResolver(container, always()));
        container.add(FlexibleNode.class);
        Option<Node> none = Option.none(Node.class);
        assertThat(container.get(FlexibleNode.class).optionalParent(), is(none));
    }
}