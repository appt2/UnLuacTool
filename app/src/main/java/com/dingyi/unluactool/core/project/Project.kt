package com.dingyi.unluactool.core.project

import org.apache.commons.vfs2.FileObject
import java.io.File

abstract interface Project {

    val fileCount:Int

    val projectPath:FileObject

    var projectIconPath:String?

    var name:String


    suspend fun resolveProjectFileCount(): Int


    suspend fun remove():Boolean
}