package edu.vt.cs5254.dreamcatcher

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import edu.vt.cs5254.dreamcatcher.databinding.ListItemDreamBinding
import java.util.UUID


class DreamHolder(private val binding: ListItemDreamBinding): RecyclerView.ViewHolder(binding.root) {

    lateinit var boundDream: Dream
        private set

    fun bind(dream: Dream, onDreamClicked: (dreamId: UUID) -> Unit) {

        boundDream = dream

        val reflectionCount = dream.entries.count { it.kind == DreamEntryKind.REFLECTION }
        binding.listItemTitle.text = dream.title
        binding.listItemReflectionCount.text = "Reflections: $reflectionCount"


        if (dream.isDeferred) {
            binding.listItemTitle.visibility = View.VISIBLE
            binding.listItemImage.setImageResource(R.drawable.dream_deferred_icon)
        } else if (dream.isFulfilled) {
            binding.listItemImage.visibility = View.VISIBLE
            binding.listItemImage.setImageResource(R.drawable.dream_fulfilled_icon)
        } else {
            binding.listItemImage.visibility = View.GONE
        }

        binding.root.setOnClickListener {
            onDreamClicked(dream.id)
        }
    }
}



class DreamListAdapter(
    private val dreamList: List<Dream>,
    private val onDreamClicked: (dreamId: UUID) -> Unit
    ): RecyclerView.Adapter<DreamHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DreamHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding: ListItemDreamBinding = ListItemDreamBinding.inflate(inflater, parent, false)
        return DreamHolder(binding)

    }

    override fun getItemCount(): Int {
        return dreamList.size
    }

    override fun onBindViewHolder(holder: DreamHolder, position: Int) {
        holder.bind(dreamList[position], onDreamClicked)

    }



}