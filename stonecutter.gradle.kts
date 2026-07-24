plugins {
    id("dev.kikugie.stonecutter")
    id("net.fabricmc.fabric-loom") version "1.17-SNAPSHOT" apply false
}

stonecutter active "26.1"

stonecutter {
    parameters {
        replacements {
            string(current.parsed < "26.2") {
                replace(
                    "GpuBufferSlice;II)V",
                    "GpuBufferSlice;Lcom/mojang/blaze3d/buffers/GpuBufferSlice;Lcom/mojang/blaze3d/buffers/GpuBuffer;Lcom/mojang/blaze3d/vertex/VertexFormat\$IndexType;II)V"
                )
                replace(
                    "\"createForGrayscaleTexture\"",
                    "\"createForIntensityTexture\""
                )
            }
        }
    }
}