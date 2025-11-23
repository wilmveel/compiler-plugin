package community.flock.compilerplugin

import org.jetbrains.kotlin.cli.common.ExitCode
import org.jetbrains.kotlin.cli.jvm.K2JVMCompiler
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintStream
import java.net.URLClassLoader
import java.util.jar.JarEntry
import java.util.jar.JarOutputStream
import kotlin.io.path.createTempDirectory
import kotlin.test.Test
import kotlin.test.assertEquals

class MapperIntegrationTest {
    @Test
    fun `compiler plugin generates mapper function and prints expected output`() {
        // Prepare temporary directories
        val tmpDir = createTempDirectory(prefix = "mapper-plugin-it-")
        val srcDir = tmpDir.resolve("src").toFile().apply { mkdirs() }
        val outDir = tmpDir.resolve("out").toFile().apply { mkdirs() }

        // Kotlin source using the plugin's @Mapper annotation
        val source = """
            |package demo
            |
            |import community.flock.compilerplugin.Mapper
            |
            |@Mapper
            |data class Person(
            |  val firstName: String,
            |  val lastName: String,
            |  val age: Int
            |)
            |
            |fun main() {
            |  val person = Person("John", "Doe", 35)
            |  println(person.mapper())
            |}
        """.trimMargin()

        val srcFile = File(srcDir, "Main.kt").apply { writeText(source) }

        // Package the plugin's compiled classes and resources into a temporary JAR
        val projectDir = File(System.getProperty("user.dir"))
        val mainClasses = File(projectDir, "build/classes/kotlin/main")
        val mainResources = File(projectDir, "build/resources/main")
        require(mainClasses.exists()) { "Main classes not found: ${mainClasses.absolutePath}. Run Gradle build before tests or let tests compile main." }
        val pluginJar = tmpDir.resolve("plugin.jar").toFile()
        JarOutputStream(pluginJar.outputStream()).use { jar ->
            fun addDir(dir: File, basePathLen: Int) {
                if (!dir.exists()) return
                dir.walkTopDown().filter { it.isFile }.forEach { file ->
                    val name =
                        file.absolutePath.substring(basePathLen).replace(File.separatorChar, '/')
                    val entry = JarEntry(if (name.startsWith("/")) name.drop(1) else name)
                    jar.putNextEntry(entry)
                    file.inputStream().use { it.copyTo(jar) }
                    jar.closeEntry()
                }
            }
            addDir(mainClasses, mainClasses.absolutePath.length)
            addDir(mainResources, mainResources.absolutePath.length)
        }

        // Compile with K2 JVM compiler; load plugin via -Xplugin JAR flag
        val compiler = K2JVMCompiler()
        val args = arrayOf(
            "-Xplugin=${pluginJar.absolutePath}",
            "-no-stdlib",
            "-no-reflect",
            "-classpath", System.getProperty("java.class.path"),
            "-d", outDir.absolutePath,
            srcFile.absolutePath
        )

        val exit = compiler.exec(System.err, *args)
        assertEquals(expected = ExitCode.OK, actual = exit, message = "Compilation failed: $exit")

        // Run compiled code and capture stdout
        val savedOut = System.out
        val baos = ByteArrayOutputStream()
        System.setOut(PrintStream(baos))
        try {
            URLClassLoader(arrayOf(outDir.toURI().toURL()), javaClass.classLoader).use { cl ->
                val mainClass = cl.loadClass("demo.MainKt")
                val main = mainClass.getMethod("main", Array<String>::class.java)
                main.invoke(null, arrayOf<String>())
            }
        } finally {
            System.setOut(savedOut)
        }

        val output = baos.toString().trim()

        assertEquals("Person(firstName=John, lastName=Doe, age=35)MAPPER", output)
    }
}
