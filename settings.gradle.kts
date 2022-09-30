import matt.kbuild.settings.applySettings

buildscript {
  val props = java.util.Properties().apply {
	load(
	  sourceFile!!.parentFile.resolve("gradle.properties").reader()
	)
  }

  fun prop(key: String) = (gradle.startParameter.projectProperties[key] ?: props[key].toString())
  val VERBOSE = prop("verboseLogging").toBoolean()
  if (VERBOSE) println("top of settings.gradle.kts buildscript block")
  repositories {
	mavenLocal()
	mavenCentral()
	google()
	gradlePluginPortal()
	maven(
	  url = "https://s01.oss.sonatype.org/content/repositories/releases/"
	)
  }
  /*this is necessary for libs.xmlutil.core and libs.xmlutil.serialization*/
  val androidAttribute = Attribute.of("net.devrieze.android", Boolean::class.javaObjectType)
  configurations.all {
	attributes {
	  attribute(androidAttribute, false)
	}
  }
  dependencies {
	val osName = System.getProperty("os.name")
	val userHomeFolder = File(System.getProperty("user.home"))
	val registeredDir = userHomeFolder.resolve("registered")
	val libsVersionToml = registeredDir.resolve("common").resolve("libs.versions.toml").takeIf { it.exists() }
	val libsText = libsVersionToml?.readText()
	  ?: java.net.URI(
		"https://raw.githubusercontent.com/mgroth0/common/master/libs.versions.toml"
	  ).toURL().readText()


	val kbuildDir = registeredDir.resolve("kbuild")
	val numBack = prop("NUM_BACK").toInt()
	if (osName == "Windows 11") {
	  classpath(files("Y:\\kbuild.jar")) /*PROBABLY WONT WORK AFTER KBUILD DEPS LIST FILE UPDATE*/
	} else if (prop("PARTIAL_BOOTSTRAP").toBoolean()) {
	  classpath(files(registeredDir.resolve("kbuild.jar"))) /*PROBABLY WONT WORK AFTER KBUILD DEPS LIST FILE UPDATE*/
	} else if (numBack == 0) classpath(fileTree(registeredDir.resolve("bin/dist/kbuild/lib")))
	else {
	  val recentVersion = kbuildDir.list()!!.mapNotNull {
		it.toLongOrNull()
	  }.sorted().reversed()[numBack]
	  classpath(fileTree(kbuildDir.resolve("$recentVersion")))
	}



	fun stupidTomlVersion(key: String) = run {
	  libsText
		.lines()
		.first {
		  key in it
		}
		.substringAfter(key)
		.substringAfter('"')
		.substringBefore('"')
	}

	val stupidKtVersion = stupidTomlVersion("kotlin")
	classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$stupidKtVersion")
	classpath("org.jetbrains.kotlin:kotlin-serialization:$stupidKtVersion")
	classpath(
	  "com.gradle.enterprise:com.gradle.enterprise.gradle.plugin:${stupidTomlVersion("gradleEnterprisePluginVersion")}"
	)
	classpath("gradle.plugin.com.github.johnrengelman:shadow:${stupidTomlVersion("shadowPluginVersion")}")
	classpath("com.dorongold.plugins:task-tree:${stupidTomlVersion("taskTreeVersion")}")
	classpath("org.panteleyev:jpackage-gradle-plugin:${stupidTomlVersion("jPackage")}")
	classpath("org.jetbrains.intellij.plugins:gradle-intellij-plugin:${stupidTomlVersion("gradleIntelliJPlugin")}")
	/*/*https://discuss.kotlinlang.org/t/is-there-an-up-to-date-tutorial-for-writing-a-kotlin-compiler-plugin-for-kotlin-1-7-10/25535*/*/
	/*classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$stupidKtVersion")*/





  }
  if (VERBOSE) println("bottom of settings.gradle.kts buildscript block")
}
applySettings()


