package dev.ragnarok.fenrir.fragment.navigation.sidedraweredit

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.checkbox.MaterialCheckBox
import dev.ragnarok.fenrir.R
import dev.ragnarok.fenrir.model.SideDrawerCategory

class SideDrawerCategotiesAdapter(private var data: List<SideDrawerCategory>) :
    RecyclerView.Adapter<SideDrawerCategotiesAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_drawer_category, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val category = data[position]
        holder.checkBox.setText(category.getTitle())
        holder.checkBox.setOnCheckedChangeListener(null)
        holder.checkBox.isChecked = category.isChecked()
        holder.checkBox.setOnCheckedChangeListener { _: CompoundButton?, isChecked: Boolean ->
            category.setChecked(isChecked)
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }

    fun setData(data: List<SideDrawerCategory>) {
        this.data = data
        notifyDataSetChanged()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val checkBox: MaterialCheckBox = itemView.findViewById(R.id.item_drawer_category_check)
    }
}