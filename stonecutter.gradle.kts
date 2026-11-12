plugins {
    id("dev.kikugie.stonecutter")
    id("net.fabricmc.fabric-loom") version "1.19-SNAPSHOT" apply false
}

stonecutter active "26.2"

stonecutter handlers {
    configure("fsh", "fsh") {
        commenter = line("//")
    }
}

stonecutter {
    parameters {
        replacements {
            string(current.parsed < "26.3") {
                replace("renderpearl.api.buffers.GpuBufferSlice", "blaze3d.buffers.GpuBufferSlice")
                replace("renderpearl.api.buffers.GpuBuffer", "blaze3d.buffers.GpuBuffer")
                replace("renderpearl.api.pipeline.RenderPipeline", "blaze3d.pipeline.RenderPipeline")
                replace("renderpearl.api.textures.FilterMode", "blaze3d.textures.FilterMode")
                replace("renderpearl.api.pipeline.BlendFunction", "blaze3d.pipeline.BlendFunction")
                replace("renderpearl.api.pipeline.ColorTargetState", "blaze3d.pipeline.ColorTargetState")
                replace("renderpearl.api.pipeline.PrimitiveTopology", "blaze3d.PrimitiveTopology")
                replace("renderpearl.api.pipeline.DepthStencilState", "blaze3d.pipeline.DepthStencilState")
                replace("renderpearl.api.pipeline.CompareOp", "blaze3d.platform.CompareOp")
                replace("renderpearl.api.pipeline.BindGroupLayout", "blaze3d.pipeline.BindGroupLayout")
                replace("renderpearl.api.pipeline.UniformType", "blaze3d.shaders.UniformType")
                replace("renderpearl.api.commands.RenderPass", "blaze3d.systems.RenderPass")
                replace("DynamicGpuDataStorageMapped", "DynamicUniformStorage")
                replace("setPipeline(Lcom/mojang/renderpearl/api/pipeline/CompiledRenderPipeline", "setPipeline(Lcom/mojang/blaze3d/pipeline/RenderPipeline")
                replace("renderpearl/api/pipeline/RenderPipeline", "blaze3d/pipeline/RenderPipeline")
                replace("renderpearl/api/commands/RenderPass", "blaze3d/systems/RenderPass")
                replace("renderpearl/api/buffers/GpuBufferSlice", "blaze3d/buffers/GpuBufferSlice")
                replace("renderpearl/api/textures/GpuSampler", "blaze3d/textures/GpuSampler")
                replace("renderpearl/api/textures/GpuTextureView", "blaze3d/textures/GpuTextureView")

                replace("#include", "#moj_import")
            }
        }
        filters.include("**/*.fsh", "**/*.vsh")
    }
}