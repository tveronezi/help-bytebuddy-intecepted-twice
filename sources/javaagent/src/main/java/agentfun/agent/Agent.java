package agentfun.agent;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.ClassFileLocator;
import net.bytebuddy.dynamic.loading.ClassInjector;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.bind.annotation.SuperCall;
import net.bytebuddy.matcher.ElementMatchers;

import java.io.File;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.nio.file.Files;
import java.util.Collections;
import java.util.concurrent.Callable;

public class Agent {

    public static class MyInterceptor {

        @SuppressWarnings("unused")
        public static String intercept(@SuperCall Callable<String> zuper) throws Exception {
            System.out.println("Intercepted!");
            return zuper.call();
        }
    }

    public static void premain(String agentArgs, Instrumentation instrumentation) throws IOException {
        File temp = Files.createTempDirectory("tmp").toFile();
        ClassInjector.UsingInstrumentation.of(
                temp, ClassInjector.UsingInstrumentation.Target.BOOTSTRAP, instrumentation
        ).inject(Collections.singletonMap(
                new TypeDescription.ForLoadedType(MyInterceptor.class),
                ClassFileLocator.ForClassLoader.read(MyInterceptor.class))
        );
        new AgentBuilder.Default()
                .ignore(ElementMatchers.nameStartsWith("net.bytebuddy."))
                .with(new AgentBuilder.InjectionStrategy.UsingUnsafe.OfFactory(
                        ClassInjector.UsingUnsafe.Factory.resolve(instrumentation)
                ))
                .type(ElementMatchers.nameEndsWith(".HttpURLConnection"))
                .transform((builder, typeDescription, classLoader, javaModule) -> builder.method(
                        ElementMatchers.named("getRequestMethod")
                ).intercept(MethodDelegation.to(MyInterceptor.class))).installOn(instrumentation);
    }
}
