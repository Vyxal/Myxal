plugins {
    id("io.github.seggan.myxal.kotlin-library-conventions")
}

dependencies {
    api(project(":runtime"))

    api("org.ow2.asm:asm:9.3")
    api("org.ow2.asm:asm-commons:9.3")
    api("org.ow2.asm:asm-util:9.3")

    api("commons-cli:commons-cli:1.5.0")
}