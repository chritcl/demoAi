package com.oa.platform;

import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

class ArchitectureRulesTest {

    @Test
    void architectureBoundariesShouldBeKept() {
        JavaClasses classes = new ClassFileImporter()
                .withImportOption(new ImportOption.DoNotIncludeTests())
                .importPackages("com.oa.platform");
        ArchRule controllerShouldNotDependOnMapperOrEntity = noClasses()
                .that().resideInAnyPackage("..controller..")
                .should().dependOnClassesThat().resideInAnyPackage("..mapper..", "..entity..")
                .allowEmptyShould(true);
        ArchRule serviceImplementationShouldNotBeUsedOutsideItsLayer = noClasses()
                .that().resideOutsideOfPackage("..service.impl..")
                .should().dependOnClassesThat().resideInAnyPackage("..service.impl..")
                .allowEmptyShould(true);
        ArchRule mapperShouldOnlyBeUsedByServiceImplementation = noClasses()
                .that().resideOutsideOfPackage("..service.impl..")
                .should().dependOnClassesThat().resideInAnyPackage("..mapper..")
                .allowEmptyShould(true);

        controllerShouldNotDependOnMapperOrEntity.check(classes);
        serviceImplementationShouldNotBeUsedOutsideItsLayer.check(classes);
        mapperShouldOnlyBeUsedByServiceImplementation.check(classes);
    }
}
