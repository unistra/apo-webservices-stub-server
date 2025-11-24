package fr.unistra.dnum.apogee.ws.server.stub.presentation.ws;

import fr.unistra.dnum.apogee.ws.export.cli.EtudiantMetierClientConfig;
import fr.unistra.dnum.apogee.ws.server.stub.test.DataSetTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.AliasFor;
import org.springframework.test.context.ActiveProfiles;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
@Documented
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DataSetTest
@Import(EtudiantMetierClientConfig.class)
@ActiveProfiles("test")
public @interface SpringBootPresentationTest {
    @AliasFor(annotation = SpringBootTest.class, attribute = "value")
    String[] value() default {};
    @AliasFor(annotation = SpringBootTest.class, attribute = "properties")
    String[] properties() default {};
    @AliasFor(annotation = SpringBootTest.class, attribute = "classes")
	Class<?>[] classes() default {};
}
