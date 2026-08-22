package com.fiap.mecanica.architecture;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import jakarta.persistence.Entity;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RestController;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

@AnalyzeClasses(
        packages = "com.fiap.mecanica",
        importOptions = ImportOption.DoNotIncludeTests.class)
class CleanArchitectureTest {

    @ArchTest
    static final ArchRule dominio_nao_depende_de_camadas_externas = noClasses()
            .that().resideInAPackage("..domain..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "org.springframework..",
                    "jakarta.persistence..",
                    "com.fasterxml.jackson..",
                    "..application..",
                    "..adapter..",
                    "..infrastructure..",
                    "..controller..",
                    "..dto..",
                    "..repository..",
                    "..service..");

    @ArchTest
    static final ArchRule aplicacao_nao_depende_de_frameworks_ou_adapters = noClasses()
            .that().resideInAPackage("..application..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "org.springframework..",
                    "jakarta.persistence..",
                    "com.fasterxml.jackson..",
                    "..adapter..",
                    "..infrastructure..",
                    "..controller..",
                    "..dto..",
                    "..repository..",
                    "..service..",
                    "..infra..");

    @ArchTest
    static final ArchRule controllers_dependem_de_ports_e_nao_de_interactors = noClasses()
            .that().resideInAPackage("..adapter.in.web.controller..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "..application.usecase..",
                    "..application.port.out..",
                    "..adapter.out..",
                    "..infrastructure..");

    @ArchTest
    static final ArchRule adapters_de_entrada_nao_dependem_de_adapters_de_saida = noClasses()
            .that().resideInAPackage("..adapter.in..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "..adapter.out..",
                    "..infrastructure..");

    @ArchTest
    static final ArchRule adapters_de_saida_nao_dependem_de_adapters_de_entrada = noClasses()
            .that().resideInAPackage("..adapter.out..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "..adapter.in..",
                    "..infrastructure..");

    @ArchTest
    static final ArchRule entidades_jpa_ficam_no_adapter_de_persistencia = classes()
            .that().areAnnotatedWith(Entity.class)
            .should().resideInAPackage("..adapter.out.persistence.jpa.entity..");

    @ArchTest
    static final ArchRule rest_controllers_ficam_no_adapter_web = classes()
            .that().areAnnotatedWith(RestController.class)
            .should().resideInAPackage("..adapter.in.web.controller..");

    @ArchTest
    static final ArchRule configuracoes_spring_ficam_na_infraestrutura = classes()
            .that().areAnnotatedWith(Configuration.class)
            .and().doNotHaveSimpleName("MecanicaApplication")
            .should().resideInAPackage("..infrastructure.configuration..");

    @ArchTest
    static final ArchRule pacotes_principais_nao_possuem_ciclos = slices()
            .matching("com.fiap.mecanica.(*)..")
            .should().beFreeOfCycles();
}
