package com.whomade.kycarrots.ui.ad.adqna

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.whomade.kycarrots.R

class AdQnaAdapter(
    private var qnas: List<Map<String, Any>>,
    private val currentUserId: String?,
    private val isAdminOrSeller: Boolean,
    private val onDeleteClick: (String) -> Unit,
    private val onAnswerClick: (String) -> Unit
) : RecyclerView.Adapter<AdQnaAdapter.ViewHolder>() {

    fun updateData(newQnas: List<Map<String, Any>>) {
        qnas = newQnas
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_product_qna, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    val qna = qnas[position]
    val qnaId = qna["QNA_ID"]?.toString() ?: qna["qnaNo"]?.toString() ?: ""
    val title = qna["TITLE"]?.toString() ?: qna["title"]?.toString() ?: ""
    val contents = qna["CONTENTS"]?.toString() ?: qna["contents"]?.toString() ?: ""
    val userNm = qna["USER_NM"]?.toString() ?: qna["userNm"]?.toString() ?: "사용자"
    val createDt = qna["REGIST_DT"]?.toString() ?: qna["createDt"]?.toString() ?: ""
    val status = qna["QNA_STATUS"]?.toString() ?: qna["status"]?.toString() ?: "10"
    val writerId = qna["USER_NO"]?.toString() ?: qna["userId"]?.toString()
    val secretYn = qna["SECRET_YN"]?.toString() ?: qna["secretYn"]?.toString() ?: "N"
    
    val answerContents = qna["ANSWER_CONTENTS"]?.toString() ?: qna["answerContents"]?.toString()
    val answerDt = qna["ANSWERED_AT"]?.toString() ?: qna["answerDt"]?.toString()

    holder.tvTitle.text = title
    holder.tvQnaMeta.text = "$userNm | $createDt"
    
    val canSee = !secretYn.equals("Y", true) || (currentUserId == writerId) || isAdminOrSeller
    
    if (canSee) {
        holder.tvContents.text = contents
        if (!answerContents.isNullOrBlank()) {
            holder.llAnswerContainer.visibility = View.VISIBLE
            holder.tvAnswerContents.text = answerContents
            holder.tvAnswerMeta.text = "공식 답변 | $answerDt"
        } else {
            holder.llAnswerContainer.visibility = View.GONE
        }
    } else {
        holder.tvContents.text = "비밀글입니다."
        holder.llAnswerContainer.visibility = View.GONE
    }

    if (!answerContents.isNullOrBlank() || status == "20" || status == "답변완료") {
        holder.tvStatusBadge.text = "답변완료"
        holder.tvStatusBadge.setTextColor(Color.parseColor("#059669"))
        holder.tvStatusBadge.setBackgroundResource(R.drawable.bg_badge_green)
    } else {
        holder.tvStatusBadge.text = "접수"
        holder.tvStatusBadge.setTextColor(Color.parseColor("#64748b"))
        holder.tvStatusBadge.setBackgroundResource(R.drawable.bg_badge_gray)
    }
    
    holder.ivSecretIcon.visibility = if (secretYn.equals("Y", true)) View.VISIBLE else View.GONE

    if (isAdminOrSeller || (currentUserId != null && currentUserId == writerId)) {
        holder.tvDelete.visibility = View.VISIBLE
        holder.tvDelete.setOnClickListener { onDeleteClick(qnaId) }
    } else {
        holder.tvDelete.visibility = View.GONE
    }
    
    // Seller view for answering if not yet answered
    if (isAdminOrSeller && answerContents.isNullOrBlank()) {
        holder.tvAnswerBtn.visibility = View.VISIBLE
        holder.tvAnswerBtn.setOnClickListener { onAnswerClick(qnaId) }
    } else {
        holder.tvAnswerBtn.visibility = View.GONE
    }
}

    override fun getItemCount(): Int = qnas.size

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvStatusBadge: TextView = view.findViewById(R.id.tv_status_badge)
        val tvTitle: TextView = view.findViewById(R.id.tv_title)
        val tvQnaMeta: TextView = view.findViewById(R.id.tv_qna_meta)
        val tvContents: TextView = view.findViewById(R.id.tv_contents)
        val tvDelete: TextView = view.findViewById(R.id.tv_delete)
        val tvAnswerBtn: TextView = view.findViewById(R.id.tv_answer_btn)
        val llAnswerContainer: LinearLayout = view.findViewById(R.id.ll_answer_container)
        val tvAnswerContents: TextView = view.findViewById(R.id.tv_answer_contents)
        val tvAnswerMeta: TextView = view.findViewById(R.id.tv_answer_meta)
        val ivSecretIcon: ImageView = view.findViewById(R.id.iv_secret_icon)
    }
}
