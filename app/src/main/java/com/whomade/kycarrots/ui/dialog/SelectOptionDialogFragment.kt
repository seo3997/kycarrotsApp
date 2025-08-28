package com.whomade.kycarrots.ui.dialog

import android.app.Dialog
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Parcelable
import android.view.*
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.whomade.kycarrots.R

/**
 * 공통 선택 다이얼로그
 * - payload: List<SelectOption(code, name)>
 * - 결과: RESULT_ITEM(SelectOption), RESULT_INDEX(Int), RESULT_CANCELED(Boolean)
 * - 레이아웃: dlg_txt_list_rv / item_dlg_text 사용
 */
class SelectOptionDialogFragment : DialogFragment() {

    companion object {
        private const val ARG_TITLE = "arg_title"
        private const val ARG_ITEMS = "arg_items"
        private const val ARG_NUMBERED = "arg_numbered"
        private const val ARG_SHOW_NONE = "arg_show_none"

        const val RESULT_KEY      = "select_option_result"
        const val RESULT_ITEM     = "result_item"
        const val RESULT_INDEX    = "result_index"
        const val RESULT_CANCELED = "result_canceled"
        const val RESULT_NONE     = "result_none"
        /**
         * @param title 다이얼로그 제목
         * @param options 표시/반환할 SelectOption 목록
         * @param numbered 리스트에 1., 2. 번호 접두어 표시 여부 (기본 true)
         */
        fun newInstance(
            title: String,
            options: ArrayList<SelectOption>,
            numbered: Boolean = true,
            showNone: Boolean = false
        ) = SelectOptionDialogFragment().apply {
            arguments = bundleOf(
                ARG_TITLE to title,
                ARG_ITEMS to options,
                ARG_NUMBERED to numbered,
                ARG_SHOW_NONE to showNone
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isCancelable = true // 뒤로가기/밖 터치 허용
    }
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dlg = Dialog(requireContext())
        dlg.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dlg.setContentView(R.layout.dlg_txt_list_rv)

        // 배경(스크림) 클릭 → cancel()만 호출 (전송은 onCancel에서)
        dlg.findViewById<View>(R.id.root_scrim).setOnClickListener {
            dialog?.cancel()
        }

        // 2) 시스템 바깥터치 취소까지 확실히: (가능하면 켜두는 게 편함)
        dlg.setCanceledOnTouchOutside(true)

        val title = requireArguments().getString(ARG_TITLE).orEmpty()
        val items = requireArguments().getParcelableArrayList<Parcelable>(ARG_ITEMS)
            ?.map { it as SelectOption }
            ?: emptyList()
        val numbered = requireArguments().getBoolean(ARG_NUMBERED, true)
        val showNone = requireArguments().getBoolean(ARG_SHOW_NONE, false)
        // 제목/구분선
        dlg.findViewById<TextView>(R.id.txt_dlg_title).text = title
        dlg.findViewById<View>(R.id.ll_dlg_title_devider).visibility = View.VISIBLE

        // RecyclerView
        val rv = dlg.findViewById<RecyclerView>(R.id.rv_txt)
        rv.layoutManager = LinearLayoutManager(requireContext())

        rv.adapter = SelectOptionAdapter(
            items = items,
            onClick = { selected ->
                val index = items.indexOf(selected)
                parentFragmentManager.setFragmentResult(
                    RESULT_KEY,
                    bundleOf(
                        RESULT_ITEM to selected,
                        RESULT_INDEX to index
                    )
                )
                dismissAllowingStateLoss()
            }
        )

        val btnNone = dlg.findViewById<TextView>(R.id.btn_dlg_cancel)
        btnNone.visibility = if (showNone) View.VISIBLE else View.GONE
        btnNone.setOnClickListener {
            parentFragmentManager.setFragmentResult(
                RESULT_KEY,
                bundleOf(RESULT_NONE to true)
            )
            dismissAllowingStateLoss()
        }
        // 취소 처리
        dlg.setCanceledOnTouchOutside(true)
        dlg.setOnCancelListener {
            parentFragmentManager.setFragmentResult(
                RESULT_KEY,
                bundleOf(RESULT_CANCELED to true)
            )
        }

        // 창 스타일(배경 투명 + 전체 사이즈)
        dlg.window?.apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            attributes = attributes.apply {
                width = WindowManager.LayoutParams.MATCH_PARENT
                height = WindowManager.LayoutParams.MATCH_PARENT
                gravity = Gravity.CENTER
            }
        }

        // 배경(스크림) 클릭 → 취소 결과 반환 후 닫기
        dlg.findViewById<View>(R.id.root_scrim).setOnClickListener {
            parentFragmentManager.setFragmentResult(
                RESULT_KEY,
                bundleOf(RESULT_CANCELED to true)
            )
            dismissAllowingStateLoss()
        }


        return dlg
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setGravity(Gravity.CENTER)
            val w = (resources.displayMetrics.widthPixels * 0.9f).toInt()
            setLayout(w, ViewGroup.LayoutParams.WRAP_CONTENT)
            attributes = attributes.apply { dimAmount = 0.6f }
        }
    }
    override fun onCancel(dialog: DialogInterface) {
        // 배경 클릭 / 뒤로가기 → 여기서만 취소 결과 전송
        parentFragmentManager.setFragmentResult(
            RESULT_KEY,
            bundleOf(RESULT_CANCELED to true)
        )
        super.onCancel(dialog)
    }
}