package com.dingyi.unluactool.engine.lasm.indexer

import com.dingyi.unluactool.MainApplication
import com.dingyi.unluactool.R
import com.dingyi.unluactool.common.ktx.getString
import com.dingyi.unluactool.core.progress.ProgressState
import com.dingyi.unluactool.core.project.Project
import com.dingyi.unluactool.core.project.ProjectIndexer
import com.dingyi.unluactool.engine.decompiler.BHeaderDecompiler
import com.dingyi.unluactool.engine.lasm.data.LASMChunk
import com.dingyi.unluactool.engine.lasm.disassemble.LasmDisassembler
import com.dingyi.unluactool.engine.lasm.dump.LasmDumper
import com.dingyi.unluactool.engine.util.ByteArrayOutputProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.apache.commons.vfs2.Selectors
import unluac.Configuration
import unluac.decompile.Output
import unluac.parse.BHeader
import java.nio.ByteBuffer
import java.util.Collections
import kotlin.io.path.toPath

class LasmIndexer : ProjectIndexer<List<LASMChunk>> {

    override suspend fun index(project: Project, progressState: ProgressState?): List<LASMChunk> =
        withContext(Dispatchers.IO) {
            val allProjectFileList = project.getProjectFileList()

            val projectSrcDir = project.getProjectPath(Project.PROJECT_SRC_NAME)

            val projectIndexedDir = project.getProjectPath(Project.PROJECT_INDEXED_NAME)


            val size = allProjectFileList.size

            progressState?.text = getString(
                R.string.editor_project_indexer_tips,
                project.name,
                0,
                size,
            )

            if (projectIndexedDir.isFolder && projectIndexedDir.findFiles(Selectors.SELECT_FILES)
                    .isNotEmpty()
            ) {
                //indexed, use file system to open
                return@withContext Collections.emptyList()
            }



            delay(2000)

            projectIndexedDir.createFolder()



            for (index in 0 until size) {
                val originFileObject = allProjectFileList[index]
                val nowProgress = (index / size) * 100
                val nextProgress = (index + 1 / size) * 100
                val rangeProgress = nextProgress - nowProgress


                val originFile = originFileObject.uri.toPath().toFile()
                val srcDirFile = projectSrcDir.uri.toPath().toFile()
                //val indexedDirFile = projectIndexedDir.uri.toPath().toFile()

                val fileName = originFile.absolutePath.substring(
                    srcDirFile.absolutePath.lastIndex + 1
                )


                progressState?.progress = progressState?.progress?.plus(rangeProgress / 3) ?: 0



                progressState?.text = getString(
                    R.string.editor_project_indexer_toast,
                    fileName,
                    index,
                    size
                )

                delay(1000)

                val targetFile = MainApplication
                    .instance
                    .fileSystemManager
                    .resolveFile(
                        projectIndexedDir.publicURIString.plus("/").plus(
                            originFile
                                .nameWithoutExtension
                        ).plus(".lasm")
                    )


                val header: BHeader
                try {
                    header = BHeaderDecompiler.decompile(Configuration().apply {
                        this.rawstring = true
                        this.mode = Configuration.Mode.DECOMPILE
                        this.variable = Configuration.VariableMode.FINDER
                    } to originFileObject.content.inputStream.use {
                        ByteBuffer.wrap(it.readBytes())
                    })
                } catch (e: Exception) {
                    e.printStackTrace()
                    continue
                }


                progressState?.progress = progressState?.progress?.plus(rangeProgress / 3) ?: 0

                delay(1000)

                val chunk = LasmDisassembler(header.main).decompile()

                val provider = ByteArrayOutputProvider()

                val output = Output(provider)

                val dumper = LasmDumper(output, chunk)

                dumper.dump()

                val bytes = provider.getBytes()

                targetFile.content.outputStream.use {
                    it.write(bytes)
                }

                progressState?.progress = nextProgress


            }


            return@withContext Collections.emptyList()


        }


}