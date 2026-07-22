package com.oa.platform.arch;

import com.tngtech.archunit.lang.ArchRule;

import java.util.ArrayList;
import java.util.List;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

/**
 * 架构边界规则集中定义。
 * 生产代码检查与违规夹具验证共用同一套规则构造，
 * 保证规则定义只有一份，夹具验证的结论对生产检查同样有效。
 */
public final class ArchitectureBoundaries {

    /** 生产代码根包 */
    public static final String BASE_PACKAGE = "com.oa.platform";

    /** 稳定业务域编码，与 docs/ARCHITECTURE.md 保持一致 */
    public static final List<String> BUSINESS_DOMAINS = List.of(
            "s0_xtsz", "s1_portal", "s2_document", "s3_office", "s4_workflow",
            "s5_contacts", "s6_file", "s7_message", "s8_audit");

    private ArchitectureBoundaries() {
    }

    /** Controller 不得依赖 Mapper、Entity */
    public static ArchRule controllerMustNotDependOnMapperOrEntity() {
        return noClasses()
                .that().resideInAnyPackage("..controller..")
                .should().dependOnClassesThat().resideInAnyPackage("..mapper..", "..entity..")
                .allowEmptyShould(true)
                .as("Controller 不得依赖 Mapper、Entity");
    }

    /** Mapper 只能由本业务包的 service.impl 调用 */
    public static ArchRule mapperOnlyCalledByServiceImpl() {
        return noClasses()
                .that().resideOutsideOfPackage("..service.impl..")
                .should().dependOnClassesThat().resideInAnyPackage("..mapper..")
                .allowEmptyShould(true)
                .as("Mapper 只能由 service.impl 调用");
    }

    /** service.impl 不得被本层之外的代码调用 */
    public static ArchRule serviceImplOnlyUsedInsideImplLayer() {
        return noClasses()
                .that().resideOutsideOfPackage("..service.impl..")
                .should().dependOnClassesThat().resideInAnyPackage("..service.impl..")
                .allowEmptyShould(true)
                .as("service.impl 不得被本层之外的代码调用");
    }

    /**
     * 不同业务域之间不得直接依赖 Mapper、Entity、service.impl。
     * 跨业务域调用只能面向对方 service 包中已声明的 Service 接口。
     */
    public static List<ArchRule> crossDomainRules(String basePackage, List<String> domains) {
        List<ArchRule> rules = new ArrayList<>();
        for (String source : domains) {
            for (String target : domains) {
                if (source.equals(target)) {
                    continue;
                }
                rules.add(noClasses()
                        .that().resideInAPackage(basePackage + "." + source + "..")
                        .should().dependOnClassesThat().resideInAnyPackage(
                                basePackage + "." + target + ".mapper..",
                                basePackage + "." + target + ".entity..",
                                basePackage + "." + target + ".service.impl..")
                        .allowEmptyShould(true)
                        .as("业务域 " + source + " 不得直接依赖业务域 " + target
                                + " 的 Mapper、Entity、service.impl，跨域调用只能面向 Service 接口"));
            }
        }
        return rules;
    }

    /** common 不得依赖任何具体业务域 */
    public static ArchRule commonMustNotDependOnDomains(String basePackage, List<String> domains) {
        String[] domainPackages = domains.stream()
                .map(domain -> basePackage + "." + domain + "..")
                .toArray(String[]::new);
        return noClasses()
                .that().resideInAPackage(basePackage + ".common..")
                .should().dependOnClassesThat().resideInAnyPackage(domainPackages)
                .allowEmptyShould(true)
                .as("common 不得依赖任何具体业务域");
    }

    /** 业务域之间不得形成循环依赖 */
    public static ArchRule domainsMustBeFreeOfCycles(String basePackage) {
        return slices().matching(basePackage + ".(*)..")
                .should().beFreeOfCycles()
                .as("业务域之间不得形成循环依赖");
    }
}
