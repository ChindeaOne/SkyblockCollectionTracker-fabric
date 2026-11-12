plugins {
    id("net.fabricmc.fabric-loom") version "1.19-SNAPSHOT" apply false
}

stonecutter active "26.2"

stonecutter {
    parameters {
        replacements.string(current.parsed >= "26.3") {
            replace("blaze3d.buffers.GpuBufferSlice", "renderpearl.api.buffers.GpuBufferSlice")
            replace("blaze3d.buffers.GpuBuffer", "renderpearl.api.buffers.GpuBuffer")
            replace("blaze3d.pipeline.RenderPipeline", "renderpearl.api.pipeline.RenderPipeline")
            replace("blaze3d.textures.FilterMode", "renderpearl.api.textures.FilterMode")
            replace("blaze3d.pipeline.BlendFunction", "renderpearl.api.pipeline.BlendFunction")
            replace("blaze3d.pipeline.ColorTargetState", "renderpearl.api.pipeline.ColorTargetState")
            replace("blaze3d.PrimitiveTopology", "renderpearl.api.pipeline.PrimitiveTopology")
            replace("blaze3d.pipeline.DepthStencilState", "renderpearl.api.pipeline.DepthStencilState")
            replace("blaze3d.platform.CompareOp", "renderpearl.api.pipeline.CompareOp")
            replace("blaze3d.pipeline.BindGroupLayout", "renderpearl.api.pipeline.BindGroupLayout")
            replace("blaze3d.shaders.UniformType", "renderpearl.api.pipeline.UniformType")
            replace("blaze3d.systems.RenderPass", "renderpearl.api.commands.RenderPass")
            replace("DynamicUniformStorage", "DynamicGpuDataStorageMapped")
            replace("setPipeline(Lcom/mojang/blaze3d/pipeline/RenderPipeline", "setPipeline(Lcom/mojang/renderpearl/api/pipeline/CompiledRenderPipeline")
            replace("blaze3d/pipeline/RenderPipeline", "renderpearl/api/pipeline/RenderPipeline")
            replace("blaze3d/systems/RenderPass", "renderpearl/api/commands/RenderPass")
            replace("blaze3d/buffers/GpuBufferSlice", "renderpearl/api/buffers/GpuBufferSlice")
            replace("blaze3d/textures/GpuSampler", "renderpearl/api/textures/GpuSampler")
            replace("blaze3d/textures/GpuTextureView", "renderpearl/api/textures/GpuTextureView")

            replace("#moj_import", "#include")
        }
    }
}