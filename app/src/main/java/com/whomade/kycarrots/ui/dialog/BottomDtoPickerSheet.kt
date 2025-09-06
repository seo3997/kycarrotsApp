package com.whomade.kycarrots.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.whomade.kycarrots.R

class BottomDtoPickerSheet(
    private val onPicked: (BottomDto) -> Unit
) : BottomSheetDialogFragment() {

    private lateinit var adapter: BottomAdapter
    private var origin: List<BottomDto> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val v = inflater.inflate(R.layout.dialog_bottom, container, false)
        val rv = v.findViewById<RecyclerView>(R.id.rvCenters)
        val et = v.findViewById<EditText>(R.id.etSearch)
        val tvTitle = v.findViewById<TextView>(R.id.tvTitle)

        // ✅ arguments 에서 타이틀 가져오기 (없으면 기본값)
        val title = arguments?.getString(ARG_TITLE) ?: "센터/도매상 선택"
        tvTitle.text = title

        adapter = BottomAdapter { item ->
            onPicked(item)   // ✅ 선택 이벤트 콜백
            dismiss()
        }
        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = adapter

        // 전달받은 센터 데이터 세팅
        origin = requireArguments()
            .getParcelableArrayList<BottomDtoParcel>("centers")!!
            .map { it.toDto() }
        adapter.submitList(origin)

        et.addTextChangedListener {
            val q = it?.toString()?.trim().orEmpty()
            val filtered = if (q.isEmpty()) origin else origin.filter { c ->
                c.name.contains(q, true)
            }
            adapter.submitList(filtered)
        }
        return v
    }

    companion object {
        private const val ARG_CENTERS = "centers"
        private const val ARG_TITLE = "title"

        fun new(
            centers: List<BottomDto>,
            title: String? = null,
            onPicked: (BottomDto) -> Unit
        ): BottomDtoPickerSheet {
            return BottomDtoPickerSheet(onPicked).apply {
                arguments = bundleOf(
                    ARG_CENTERS to ArrayList(centers.map { BottomDtoParcel.from(it) }),
                    ARG_TITLE to title
                )
            }
        }
    }
}
