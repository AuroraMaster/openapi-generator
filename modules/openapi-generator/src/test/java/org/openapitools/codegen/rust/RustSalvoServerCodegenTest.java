package org.openapitools.codegen.rust;

import org.openapitools.codegen.DefaultGenerator;
import org.openapitools.codegen.TestUtils;
import org.openapitools.codegen.config.CodegenConfigurator;
import org.openapitools.codegen.languages.RustSalvoServerCodegen;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.openapitools.codegen.TestUtils.linearize;

public class RustSalvoServerCodegenTest {

    @Test
    public void testInitialConfigValues() throws Exception {
        final RustSalvoServerCodegen codegen = new RustSalvoServerCodegen();
        codegen.processOpts();

        // Test default values
        Assert.assertEquals(codegen.getName(), "rust-salvo");
        Assert.assertEquals(codegen.getPackage(), "salvo_openapi");
        Assert.assertTrue(codegen.additionalProperties().containsKey("enableRequestValidation"));
        Assert.assertTrue(codegen.additionalProperties().containsKey("enableAuthMiddleware"));
        Assert.assertTrue(codegen.additionalProperties().containsKey("enableCorsMiddleware"));
    }

    @Test
    public void testBasicGeneration() throws IOException {
        Path target = Files.createTempDirectory("salvo-test");
        final CodegenConfigurator configurator = new CodegenConfigurator()
                .setGeneratorName("rust-salvo")
                .setInputSpec("src/test/resources/3_0/rust/rust-salvo-basic-test.yaml")
                .setSkipOverwrite(false)
                .setOutputDir(target.toAbsolutePath().toString().replace("\\", "/"));

        List<File> files = new DefaultGenerator().opts(configurator.toClientOptInput()).generate();
        files.forEach(File::deleteOnExit);

        // Verify core files are generated
        TestUtils.assertFileExists(Path.of(target.toString(), "Cargo.toml"));
        TestUtils.assertFileExists(Path.of(target.toString(), "src/lib.rs"));
        TestUtils.assertFileExists(Path.of(target.toString(), "src/main.rs"));
        TestUtils.assertFileExists(Path.of(target.toString(), "src/models.rs"));
        TestUtils.assertFileExists(Path.of(target.toString(), "src/routes.rs"));
        TestUtils.assertFileExists(Path.of(target.toString(), "src/handlers/mod.rs"));
    }

    @Test
    public void testHandlerGeneration() throws IOException {
        Path target = Files.createTempDirectory("salvo-handlers-test");
        final CodegenConfigurator configurator = new CodegenConfigurator()
                .setGeneratorName("rust-salvo")
                .setInputSpec("src/test/resources/3_0/rust/rust-salvo-basic-test.yaml")
                .setSkipOverwrite(false)
                .setOutputDir(target.toAbsolutePath().toString().replace("\\", "/"));

        List<File> files = new DefaultGenerator().opts(configurator.toClientOptInput()).generate();
        files.forEach(File::deleteOnExit);

        // Verify handler files are generated
        Path handlersModPath = Path.of(target.toString(), "src/handlers/mod.rs");
        TestUtils.assertFileExists(handlersModPath);

        // Check that the handlers module exports are present
        TestUtils.assertFileContains(handlersModPath, "pub mod default;");
        TestUtils.assertFileContains(handlersModPath, "pub use default::*;");
    }

    @Test
    public void testCargoTomlGeneration() throws IOException {
        Path target = Files.createTempDirectory("salvo-cargo-test");
        final CodegenConfigurator configurator = new CodegenConfigurator()
                .setGeneratorName("rust-salvo")
                .setInputSpec("src/test/resources/3_0/rust/rust-salvo-basic-test.yaml")
                .setSkipOverwrite(false)
                .setOutputDir(target.toAbsolutePath().toString().replace("\\", "/"));

        List<File> files = new DefaultGenerator().opts(configurator.toClientOptInput()).generate();
        files.forEach(File::deleteOnExit);

        // Verify Cargo.toml contains required dependencies
        Path cargoPath = Path.of(target.toString(), "Cargo.toml");
        TestUtils.assertFileExists(cargoPath);
        TestUtils.assertFileContains(cargoPath, "salvo = { version = \"0.70\"");
        TestUtils.assertFileContains(cargoPath, "serde = { version = \"1.0\"");
        TestUtils.assertFileContains(cargoPath, "tokio = { version = \"1.0\"");
        TestUtils.assertFileContains(cargoPath, "serde_json = \"1.0\"");
    }

    @Test
    public void testMiddlewareGeneration() throws IOException {
        Path target = Files.createTempDirectory("salvo-middleware-test");
        final CodegenConfigurator configurator = new CodegenConfigurator()
                .setGeneratorName("rust-salvo")
                .setInputSpec("src/test/resources/3_0/rust/rust-salvo-auth-test.yaml")
                .setSkipOverwrite(false)
                .setAdditionalProperty("enableAuthMiddleware", "true")
                .setOutputDir(target.toAbsolutePath().toString().replace("\\", "/"));

        List<File> files = new DefaultGenerator().opts(configurator.toClientOptInput()).generate();
        files.forEach(File::deleteOnExit);

        // Verify middleware file is generated when auth is enabled
        Path middlewarePath = Path.of(target.toString(), "src/middleware.rs");
        TestUtils.assertFileExists(middlewarePath);
        TestUtils.assertFileContains(middlewarePath, "pub struct AuthMiddleware");
    }

    @Test
    public void testValidationSupport() throws IOException {
        Path target = Files.createTempDirectory("salvo-validation-test");
        final CodegenConfigurator configurator = new CodegenConfigurator()
                .setGeneratorName("rust-salvo")
                .setInputSpec("src/test/resources/3_0/rust/rust-salvo-validation-test.yaml")
                .setSkipOverwrite(false)
                .setAdditionalProperty("enableRequestValidation", "true")
                .setOutputDir(target.toAbsolutePath().toString().replace("\\", "/"));

        List<File> files = new DefaultGenerator().opts(configurator.toClientOptInput()).generate();
        files.forEach(File::deleteOnExit);

        // Verify validation dependencies are included when enabled
        Path cargoPath = Path.of(target.toString(), "Cargo.toml");
        TestUtils.assertFileExists(cargoPath);
        TestUtils.assertFileContains(cargoPath, "validator = { version = \"0.18\"");
    }

    @Test
    public void testRouteGeneration() throws IOException {
        Path target = Files.createTempDirectory("salvo-routes-test");
        final CodegenConfigurator configurator = new CodegenConfigurator()
                .setGeneratorName("rust-salvo")
                .setInputSpec("src/test/resources/3_0/rust/rust-salvo-basic-test.yaml")
                .setSkipOverwrite(false)
                .setOutputDir(target.toAbsolutePath().toString().replace("\\", "/"));

        List<File> files = new DefaultGenerator().opts(configurator.toClientOptInput()).generate();
        files.forEach(File::deleteOnExit);

        // Verify routes file contains expected Salvo routing code
        Path routesPath = Path.of(target.toString(), "src/routes.rs");
        TestUtils.assertFileExists(routesPath);
        TestUtils.assertFileContains(routesPath, "use salvo::prelude::*;");
        TestUtils.assertFileContains(routesPath, "pub fn create_router()");
        TestUtils.assertFileContains(routesPath, "Router::new()");
    }
}
