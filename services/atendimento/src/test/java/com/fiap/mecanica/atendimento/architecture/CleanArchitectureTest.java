package com.fiap.mecanica.atendimento.architecture;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.library.dependencies.SlicesRuleDefinition;

import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(packages = "com.fiap.mecanica.atendimento")
class CleanArchitectureTest {

    private static final String BASE_PACKAGE = "com.fiap.mecanica.atendimento";

    @ArchTest
    static final ArchRule domain_nao_deve_depender_de_spring_jpa_application_ou_adapter = noClasses()
            .that().resideInAPackage(BASE_PACKAGE + ".domain..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "org.springframework..",
                    "jakarta.persistence..",
                    BASE_PACKAGE + ".application..",
                    BASE_PACKAGE + ".adapter..",
                    BASE_PACKAGE + ".infrastructure.."
            );

    @ArchTest
    static final ArchRule application_nao_deve_depender_de_spring_jpa_adapter_ou_infrastructure = noClasses()
            .that().resideInAPackage(BASE_PACKAGE + ".application..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "org.springframework..",
                    "jakarta.persistence..",
                    BASE_PACKAGE + ".adapter..",
                    BASE_PACKAGE + ".infrastructure.."
            );

    @ArchTest
    static final ArchRule controllers_devem_depender_apenas_de_port_in = noClasses()
            .that().resideInAPackage(BASE_PACKAGE + ".adapter.in.web.controller..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    BASE_PACKAGE + ".application.usecase..",
                    BASE_PACKAGE + ".application.port.out..",
                    BASE_PACKAGE + ".adapter.out.."
            );

    @ArchTest
    static final ArchRule adapters_in_nao_devem_depender_de_adapters_out = noClasses()
            .that().resideInAPackage(BASE_PACKAGE + ".adapter.in..")
            .should().dependOnClassesThat().resideInAPackage(BASE_PACKAGE + ".adapter.out..");

    @ArchTest
    static final ArchRule adapters_out_nao_devem_depender_de_adapters_in = noClasses()
            .that().resideInAPackage(BASE_PACKAGE + ".adapter.out..")
            .should().dependOnClassesThat().resideInAPackage(BASE_PACKAGE + ".adapter.in..");

    @ArchTest
    static final ArchRule entidades_jpa_devem_residir_apenas_no_pacote_de_persistencia = classes()
            .that().areAnnotatedWith(jakarta.persistence.Entity.class)
            .should().resideInAPackage(BASE_PACKAGE + ".adapter.out.persistence.jpa.entity..");

    @ArchTest
    static final ArchRule rest_controllers_devem_residir_apenas_no_pacote_de_controllers = classes()
            .that().areAnnotatedWith(org.springframework.web.bind.annotation.RestController.class)
            .should().resideInAPackage(BASE_PACKAGE + ".adapter.in.web.controller..");

    @ArchTest
    static final ArchRule configuracoes_spring_devem_residir_apenas_em_infrastructure_configuration = classes()
            .that().areAnnotatedWith(org.springframework.context.annotation.Configuration.class)
            .should().resideInAPackage(BASE_PACKAGE + ".infrastructure.configuration..");

    // Ciclo real e intencional entre domain e exception: OrdemServico (domain) lança TransicaoInvalidaException
    // (exception), e TransicaoInvalidaException/TemplateNotFound (exception) recebem Status/CodigoTemplate
    // (domain) como parâmetro de construtor. Decisão documentada em
    // .junie/plans/migrar-atendimento-clean-architecture.md: TransicaoInvalidaException permanece em exception/,
    // não em domain/exception/, para manter consistência com o padrão de services/cliente — por isso esse ciclo
    // específico (nas duas direções) é tolerado, sem afrouxar a regra para os demais pacotes.
    @ArchTest
    static final ArchRule pacotes_principais_devem_estar_livres_de_ciclos = SlicesRuleDefinition.slices()
            .matching(BASE_PACKAGE + ".(*)..")
            .should().beFreeOfCycles()
            .ignoreDependency(resideInAPackage(BASE_PACKAGE + ".domain.."), resideInAPackage(BASE_PACKAGE + ".exception.."))
            .ignoreDependency(resideInAPackage(BASE_PACKAGE + ".exception.."), resideInAPackage(BASE_PACKAGE + ".domain.."));
}
