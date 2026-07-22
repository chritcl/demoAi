package com.oa.platform.arch;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.EvaluationResult;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 违规夹具验证。
 * archfixture 包下的夹具类带有真实的违规依赖，
 * 本测试证明每条架构规则确实会因为违规而失败，而不是只在空目录上空转。
 */
class ArchitectureViolationFixtureTest {

    private static final String FIXTURE_PACKAGE = "com.oa.platform.archfixture";
    private static final List<String> FIXTURE_DOMAINS = List.of("alpha", "beta");

    private static JavaClasses importFixtures() {
        return new ClassFileImporter().importPackages(FIXTURE_PACKAGE);
    }

    @Test
    void controllerRuleRejectsMapperAndEntityDependency() {
        // 夹具 AlphaController 直接依赖 AlphaMapper 与 AlphaEntity，必须被拒绝
        assertThrows(AssertionError.class, () -> ArchitectureBoundaries
                .controllerMustNotDependOnMapperOrEntity().check(importFixtures()));
    }

    @Test
    void mapperRuleRejectsCallerOutsideServiceImpl() {
        // 夹具 AlphaController 在 service.impl 之外调用 Mapper，必须被拒绝
        assertThrows(AssertionError.class, () -> ArchitectureBoundaries
                .mapperOnlyCalledByServiceImpl().check(importFixtures()));
    }

    @Test
    void crossDomainRulesRejectMapperEntityAndImplDependency() {
        JavaClasses classes = importFixtures();
        List<ArchRule> rules = ArchitectureBoundaries.crossDomainRules(FIXTURE_PACKAGE, FIXTURE_DOMAINS);
        assertFalse(rules.isEmpty(), "跨业务域规则不应为空");
        boolean violationFound = false;
        for (ArchRule rule : rules) {
            EvaluationResult result = rule.evaluate(classes);
            if (result.hasViolation()) {
                violationFound = true;
                // 违规来源必须是夹具中故意违规的 BetaServiceImpl
                assertTrue(result.getFailureReport().toString().contains("BetaServiceImpl"),
                        result.getFailureReport().toString());
            }
        }
        assertTrue(violationFound, "跨业务域违规依赖未被规则识别");
    }

    @Test
    void crossDomainRulesAcceptServiceInterfaceCalls() {
        // 只导入合法调用方：AlphaServiceImpl 仅通过 BetaService 接口跨域调用，规则必须放行
        JavaClasses classes = new ClassFileImporter()
                .importPackages(FIXTURE_PACKAGE + ".alpha.service.impl");
        for (ArchRule rule : ArchitectureBoundaries.crossDomainRules(FIXTURE_PACKAGE, FIXTURE_DOMAINS)) {
            rule.check(classes);
        }
    }

    @Test
    void commonRuleRejectsDomainDependency() {
        // 夹具 FixtureCommonUtil 依赖 alpha 业务域 Entity，必须被拒绝
        assertThrows(AssertionError.class, () -> ArchitectureBoundaries
                .commonMustNotDependOnDomains(FIXTURE_PACKAGE, FIXTURE_DOMAINS).check(importFixtures()));
    }

    @Test
    void cycleRuleRejectsDomainCycles() {
        // 夹具 alpha 与 beta 互相依赖，必须被拒绝
        assertThrows(AssertionError.class, () -> ArchitectureBoundaries
                .domainsMustBeFreeOfCycles(FIXTURE_PACKAGE).check(importFixtures()));
    }
}
