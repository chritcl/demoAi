package com.oa.platform;

import com.oa.platform.arch.ArchitectureBoundaries;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;

import java.util.List;

/**
 * 生产代码架构边界校验。
 * 规则本身能否识别违规由 ArchitectureViolationFixtureTest 通过夹具证明，
 * 本测试只保证生产代码不违反边界。
 */
class ArchitectureRulesTest {

    @Test
    void architectureBoundariesShouldBeKept() {
        JavaClasses classes = new ClassFileImporter()
                .withImportOption(new ImportOption.DoNotIncludeTests())
                .importPackages("com.oa.platform");

        ArchitectureBoundaries.controllerMustNotDependOnMapperOrEntity().check(classes);
        ArchitectureBoundaries.mapperOnlyCalledByServiceImpl().check(classes);
        ArchitectureBoundaries.serviceImplOnlyUsedInsideImplLayer().check(classes);
        for (ArchRule rule : ArchitectureBoundaries.crossDomainRules(
                ArchitectureBoundaries.BASE_PACKAGE, ArchitectureBoundaries.BUSINESS_DOMAINS)) {
            rule.check(classes);
        }
        ArchitectureBoundaries.commonMustNotDependOnDomains(
                ArchitectureBoundaries.BASE_PACKAGE, ArchitectureBoundaries.BUSINESS_DOMAINS).check(classes);
        ArchitectureBoundaries.domainsMustBeFreeOfCycles(ArchitectureBoundaries.BASE_PACKAGE).check(classes);
    }
}
