package com.dingyi.unluactool.engine.lasm.assemble.base

import com.dingyi.unluactool.engine.lasm.data.v1.LASMChunk
import java.io.OutputStream

interface AbstractLasmAssembler {

    fun assemble(mainChunk:LASMChunk,output:OutputStream)
}