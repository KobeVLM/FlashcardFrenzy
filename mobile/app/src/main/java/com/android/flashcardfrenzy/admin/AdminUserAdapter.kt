package com.android.flashcardfrenzy.admin

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.android.flashcardfrenzy.databinding.ItemAdminUserBinding

/**
 * RecyclerView adapter for the admin user list.
 *
 * item_admin_user.xml expected IDs:
 *   - tv_full_name  : TextView
 *   - tv_email      : TextView
 *   - tv_role       : TextView
 *   - tv_created_at : TextView
 *   - btn_delete    : Button
 */
class AdminUserAdapter(
    private var users: List<AdminDtos.AdminUserResponse> = emptyList(),
    private val onDeleteClick: (AdminDtos.AdminUserResponse) -> Unit
) : RecyclerView.Adapter<AdminUserAdapter.UserViewHolder>() {

    inner class UserViewHolder(private val binding: ItemAdminUserBinding)
        : RecyclerView.ViewHolder(binding.root) {

        fun bind(user: AdminDtos.AdminUserResponse) {
            binding.tvFullName.text  = user.fullName
            binding.tvEmail.text     = user.email
            binding.tvRole.text      = user.role
            binding.tvCreatedAt.text = user.createdAt.take(10)
            binding.btnDelete.setOnClickListener { onDeleteClick(user) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = ItemAdminUserBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return UserViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) =
        holder.bind(users[position])

    override fun getItemCount() = users.size

    fun updateData(newUsers: List<AdminDtos.AdminUserResponse>) {
        users = newUsers
        notifyDataSetChanged()
    }

    fun removeUser(userId: Long) {
        val index = users.indexOfFirst { it.id == userId }
        if (index != -1) {
            users = users.toMutableList().also { it.removeAt(index) }
            notifyItemRemoved(index)
        }
    }
}
