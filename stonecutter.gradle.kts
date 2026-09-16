plugins {
    id("dev.kikugie.stonecutter")
    id("net.fabricmc.fabric-loom") version "1.17-SNAPSHOT" apply false
}

stonecutter active "26.1"

stonecutter {
    parameters {
        replacements {
            string(current.parsed < "26.2") {
                replace("\"createForGrayscaleTexture\"", "\"createForIntensityTexture\"")
            }
            string(current.parsed < "26.3") {
                replace("com.mojang.renderpearl.api.buffers.GpuBufferSlice", "com.mojang.blaze3d.buffers.GpuBufferSlice")
                replace("com.mojang.renderpearl.api.buffers.GpuBuffer", "com.mojang.blaze3d.buffers.GpuBuffer")
                replace("com.mojang.renderpearl.api.pipeline.RenderPipeline", "com.mojang.blaze3d.pipeline.RenderPipeline")
                replace("com.mojang.renderpearl.api.textures.FilterMode", "com.mojang.blaze3d.textures.FilterMode")
                replace("com.mojang.renderpearl.api.pipeline.BlendFunction", "com.mojang.blaze3d.pipeline.BlendFunction")
                replace("com.mojang.renderpearl.api.pipeline.ColorTargetState", "com.mojang.blaze3d.pipeline.ColorTargetState")
                replace("com.mojang.renderpearl.api.pipeline.PrimitiveTopology", "com.mojang.blaze3d.PrimitiveTopology")
                replace("com.mojang.renderpearl.api.pipeline.DepthStencilState", "com.mojang.blaze3d.pipeline.DepthStencilState")
                replace("com.mojang.renderpearl.api.pipeline.CompareOp", "com.mojang.blaze3d.platform.CompareOp")
                replace("com.mojang.renderpearl.api.pipeline.BindGroupLayout", "com.mojang.blaze3d.pipeline.BindGroupLayout")
                replace("com.mojang.renderpearl.api.pipeline.UniformType", "com.mojang.blaze3d.shaders.UniformType")
                replace("com.mojang.renderpearl.api.commands.RenderPass", "com.mojang.blaze3d.systems.RenderPass")
                replace("DynamicGpuDataStorageMapped", "DynamicUniformStorage")

                replace(
                    "Lnet/minecraft/client/renderer/state/gui/GuiElementRenderState;pipeline()Lcom/mojang/renderpearl/api/pipeline/RenderPipeline;",
                    "Lnet/minecraft/client/renderer/state/gui/GuiElementRenderState;pipeline()Lcom/mojang/blaze3d/pipeline/RenderPipeline;"
                )
                replace(
                    "Lcom/mojang/renderpearl/api/commands/RenderPass;setPipeline(Lcom/mojang/renderpearl/api/pipeline/CompiledRenderPipeline;)V",
                    "Lcom/mojang/blaze3d/systems/RenderPass;setPipeline(Lcom/mojang/blaze3d/pipeline/RenderPipeline;)V"
                )
            }
        }
    }
}