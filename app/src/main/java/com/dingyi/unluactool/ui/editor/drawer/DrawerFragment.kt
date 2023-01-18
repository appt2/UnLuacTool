package com.dingyi.unluactool.ui.editor.drawer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dingyi.unluactool.common.base.BaseFragment
import com.dingyi.unluactool.common.ktx.dp
import com.dingyi.unluactool.common.ktx.getAttributeColor
import com.dingyi.unluactool.databinding.FragmentEditorDrawerShipBinding
import com.dingyi.unluactool.engine.filesystem.UnLuaCFileObject
import com.dingyi.unluactool.ui.editor.EditorFragmentData
import com.dingyi.unluactool.ui.editor.EditorViewModel
import com.dingyi.unluactool.ui.editor.adapter.EditorFileTabAdapter
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.RoundedCornerTreatment
import com.google.android.material.shape.ShapeAppearanceModel
import org.apache.commons.vfs2.VFS

class DrawerFragment : BaseFragment<FragmentEditorDrawerShipBinding>() {

    private val viewModel by activityViewModels<EditorViewModel>()

    private val fsManager by lazy(LazyThreadSafetyMode.NONE) {
        VFS.getManager()
    }

    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentEditorDrawerShipBinding {
        return FragmentEditorDrawerShipBinding.inflate(inflater, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val projectName = viewModel.project.value?.name

        binding.apply {
            editorDrawerToolbarTitle.text = projectName

            val shapeAppearanceModel = ShapeAppearanceModel.builder().apply {
                setAllCorners(RoundedCornerTreatment())
                setTopRightCornerSize(16.dp.toFloat())
                setBottomRightCornerSize(16.dp.toFloat())
            }.build()

            editorDrawerLinearRoot.background = MaterialShapeDrawable(shapeAppearanceModel).apply {
                setTint(getAttributeColor(com.google.android.material.R.attr.colorSurface))
            }


            var isSetHeight = false
            editorDrawerInsets.setOnInsetsCallback {
                editorDrawerStatusBar.updateLayoutParams<ViewGroup.LayoutParams> {
                    if (isSetHeight) {
                        return@updateLayoutParams
                    }
                    height = it.top
                    isSetHeight = true
                }
            }

            editorDrawerList.apply {
                val adapter = EditorFileTabAdapter()

                layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

                adapter.apply {
                    observableSource(viewModel.fragmentDataList)
                    observableCurrentSelectData(
                        this@DrawerFragment,
                        viewModel.currentSelectEditorFragmentData
                    )

                    clickListener = {
                        openFileObjectByFragmentData(it)
                    }
                }

                setAdapter(adapter)
            }

        }
    }

    private fun openFileObjectByFragmentData(fragmentData: EditorFragmentData) {
        val fileUri = fragmentData.fileUri
        if (fileUri.isEmpty()) {
            viewModel.setCurrentSelectEditorFragmentData(fragmentData)
            return
        }
        val fileObject = fsManager.resolveFile(fragmentData.fileUri) as UnLuaCFileObject
        viewModel.openFileObject(fileObject)
    }

    override fun onDestroyView() {
        super.onDestroyView()

        val editorDrawerListAdapter =
            binding.editorDrawerList.adapter as EditorFileTabAdapter

        editorDrawerListAdapter.removeObservable(viewModel.fragmentDataList)
    }
}